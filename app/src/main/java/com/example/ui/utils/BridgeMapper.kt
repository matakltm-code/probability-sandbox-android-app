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
            const originalFillText = CanvasRenderingContext2D.prototype.fillText;
            CanvasRenderingContext2D.prototype.fillText = function(text, x, y, maxWidth) {
                try {
                    if (window.$BRIDGE_NAME && text && text.trim().length > 0) {
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
                
                function extractAndSend() {
                    var elements = document.querySelectorAll(selector);
                    var allText = '';
                    for (var i = 0; i < elements.length; i++) {
                        allText += elements[i].innerText + ' ';
                    }
                    var items = allText.split(/\s+/).filter(Boolean);
                    if (items.length > 0 && window.$BRIDGE_NAME) {
                        window.$BRIDGE_NAME.postMessage(JSON.stringify({ type: 'dom', data: items.join(',') }));
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
            
            window.addEventListener('message', function(e) {
                try {
                    var msg = JSON.parse(e.data);
                    if (msg.type === 'startDomExtraction') {
                        window.startDomExtraction(msg.selector);
                    } else if (msg.type === 'stopDomExtraction') {
                        if (window.__activeDomObserver) {
                            window.__activeDomObserver.disconnect();
                        }
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
        onDataReceived: (String, String) -> Unit // (type, data)
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
                            val content = if (type == "canvas") json.optString("text") else json.optString("data")
                            if (content.isNotEmpty()) {
                                // Switch context or handle carefully since this is called on a WebView thread
                                coroutineScope.launch {
                                    onDataReceived(type, content)
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
