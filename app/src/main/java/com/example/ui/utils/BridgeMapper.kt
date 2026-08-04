package com.example.ui.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import androidx.webkit.JavaScriptReplyProxy
import androidx.webkit.WebMessageCompat
import androidx.webkit.WebViewCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject

import androidx.webkit.WebViewFeature

object BridgeMapper {
    const val BRIDGE_NAME = "AndroidWebMessageBridge"

    val injectionScript = """
        (function() {
            if (window.__bridgeMapperInjected) return;
            window.__bridgeMapperInjected = true;
            
            // 1. Hook Canvas text drawing
            window.__deepExtractionEnabled = false;
            const originalFillText = CanvasRenderingContext2D.prototype.fillText;
            CanvasRenderingContext2D.prototype.fillText = function(text, x, y, maxWidth) {
                try {
                    if (window.__deepExtractionEnabled && window.$BRIDGE_NAME && text && text.trim().length > 0) {
                        // Send text to Android layer
                        window.$BRIDGE_NAME.postMessage(JSON.stringify({ type: 'canvas', text: text.trim() }));
                    }
                } catch (e) {}
                return originalFillText.apply(this, arguments);
            };

            // 2. Cross-Frame DOM monitoring helper
            window.startDomExtraction = function(selector) {
                var targetNode = document.body;
                if (!targetNode) return;
                
                // Generalize the selector if it points to a specific list item
                var genSelector = selector.replace(/(:nth-of-type\(\d+\)|:nth-child\(\d+\))([^:]*)$/, "$2");
                if (document.querySelectorAll(genSelector).length > 1) {
                    selector = genSelector;
                }
                
                var lastHistory = "";
                
                function extractAndSend() {
                    var elements = document.querySelectorAll(selector);
                    var allText = [];
                    for (var i = 0; i < elements.length; i++) {
                        allText.push(elements[i].innerText.trim());
                    }
                    var items = allText.filter(Boolean);
                    var newHistory = items.join(',');
                    if (newHistory !== lastHistory && items.length > 0 && window.$BRIDGE_NAME) {
                        lastHistory = newHistory;
                        window.$BRIDGE_NAME.postMessage(JSON.stringify({ type: 'dom', data: newHistory }));
                    }
                }
                
                if (window.__activeDomObserver) {
                    window.__activeDomObserver.disconnect();
                }
                
                window.__activeDomObserver = new MutationObserver((mutationsList) => {
                    extractAndSend();
                });
                window.__activeDomObserver.observe(targetNode, { childList: true, subtree: true, characterData: true });
                extractAndSend(); // Initial check
            };
            
            // 3. Mapping Element Helper
            function getCssSelector(el) {
                if (!(el instanceof Element)) return;
                var path = [];
                while (el.nodeType === Node.ELEMENT_NODE) {
                    var selector = el.nodeName.toLowerCase();
                    if (el.id) {
                        selector += '#' + el.id;
                        path.unshift(selector);
                        break;
                    } else {
                        var sib = el, nth = 1;
                        while (sib = sib.previousElementSibling) {
                            if (sib.nodeName.toLowerCase() == selector) nth++;
                        }
                        if (nth != 1) selector += ":nth-of-type("+nth+")";
                    }
                    path.unshift(selector);
                    el = el.parentNode;
                }
                return path.join(" > ");
            }

            window.__mappingEnabled = false;
            document.addEventListener('click', function(e) {
                if (window.__mappingEnabled) {
                    e.preventDefault();
                    e.stopPropagation();
                    var target = e.target;
                    var selector = getCssSelector(target);
                    var text = target.innerText || target.textContent;
                    
                    var oldOutline = target.style.outline;
                    var oldBackgroundColor = target.style.backgroundColor;
                    target.style.outline = '3px solid #4CAF50';
                    target.style.backgroundColor = 'rgba(76, 175, 80, 0.3)';
                    setTimeout(function() {
                        target.style.outline = oldOutline;
                        target.style.backgroundColor = oldBackgroundColor;
                    }, 2000);
                    
                    if (window.$BRIDGE_NAME) {
                        window.$BRIDGE_NAME.postMessage(JSON.stringify({ type: 'map', selector: selector, text: text ? text.substring(0, 50) : '' }));
                    }
                }
            }, true);

            window.addEventListener('message', function(e) {
                try {
                    var msg = JSON.parse(e.data);
                    if (msg.type === 'startDomExtraction') {
                        window.startDomExtraction(msg.selector);
                    } else if (msg.type === 'stopDomExtraction') {
                        if (window.__activeDomObserver) {
                            window.__activeDomObserver.disconnect();
                        }
                    } else if (msg.type === 'setDeepExtraction') {
                        window.__deepExtractionEnabled = msg.enabled;
                    } else if (msg.type === 'setMapping') {
                        window.__mappingEnabled = msg.enabled;
                    }
                } catch(err) {}
            });
            
            console.log("BridgeMapper payload injected successfully.");
        })();
    """.trimIndent()

    @SuppressLint("RequiresFeature")
    fun setupWebMessageListener(
        webView: WebView,
        coroutineScope: CoroutineScope,
        onDataReceived: (String, String, String) -> Unit // (type, data1, data2)
    ) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(
                webView,
                injectionScript,
                setOf("*")
            )
        }
        
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            val listener = object : WebViewCompat.WebMessageListener {
                override fun onPostMessage(
                    view: WebView,
                    message: WebMessageCompat,
                    sourceOrigin: Uri,
                    isMainFrame: Boolean,
                    replyProxy: JavaScriptReplyProxy
                ) {
                    message.data?.let { dataStr ->
                        try {
                            val json = JSONObject(dataStr)
                            val type = json.optString("type")
                            if (type == "map") {
                                val selector = json.optString("selector")
                                val text = json.optString("text")
                                coroutineScope.launch {
                                    onDataReceived(type, selector, text)
                                }
                            } else {
                                val content = if (type == "canvas") json.optString("text") else json.optString("data")
                                if (content.isNotEmpty()) {
                                    // Switch context or handle carefully since this is called on a WebView thread
                                    coroutineScope.launch {
                                        onDataReceived(type, content, "")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore parsing errors
                        }
                    }
                }
            }
            WebViewCompat.addWebMessageListener(
                webView,
                BRIDGE_NAME,
                setOf("*"), // Allow all origins for complex iframe structures
                listener
            )
        }
    }
}
