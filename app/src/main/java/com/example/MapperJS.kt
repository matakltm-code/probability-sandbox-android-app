package com.example

object MapperJS {
    val enableScript = """
        (function() {
            window.predictiveMapperEnabled = true;
            if (window.predictiveMapperListenerAdded) return;
            window.predictiveMapperListenerAdded = true;
            
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
                            if (sib.nodeName.toLowerCase() == selector)
                               nth++;
                        }
                        if (nth != 1) selector += ":nth-of-type("+nth+")";
                    }
                    path.unshift(selector);
                    el = el.parentNode;
                }
                return path.join(" > ");
            }
            
            document.addEventListener('click', function(e) {
                if (window.predictiveMapperEnabled) {
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
                    
                    AndroidBridge.onElementSelected(selector, text ? text.substring(0, 50) : '');
                }
            }, true);
        })();
    """.trimIndent()

    val disableScript = """
        (function() {
            window.predictiveMapperEnabled = false;
        })();
    """.trimIndent()

    fun getObserverScript(selector: String): String = """
        (function() {
            var safeSelector = "${selector.replace("\"", "\\\"").replace("'", "\\'")}";
            var targetNode = document.querySelector(safeSelector);
            if (!targetNode) {
                // If the exact node is not found, try the parent of the matched elements, or observe body
                var firstEl = document.querySelectorAll(safeSelector)[0];
                targetNode = firstEl ? firstEl.parentNode : document.body;
            }
            if (window.activePredictiveObserver) {
                window.activePredictiveObserver.disconnect();
            }
            
            function extractAndSend() {
                var elements = document.querySelectorAll(safeSelector);
                var allText = '';
                for (var i = 0; i < elements.length; i++) {
                    allText += elements[i].innerText + ' ';
                }
                var items = allText.split(/\s+/).filter(Boolean);
                if (window.AndroidBridge && window.AndroidBridge.onNewDataExtracted) {
                    window.AndroidBridge.onNewDataExtracted(items.join(','));
                }
            }
            
            window.activePredictiveObserver = new MutationObserver((mutationsList) => {
                extractAndSend();
            });
            window.activePredictiveObserver.observe(targetNode, { childList: true, subtree: true, characterData: true });
            
            // Trigger initial
            extractAndSend();
            
            return 'Observer attached';
        })();
    """.trimIndent()
}
