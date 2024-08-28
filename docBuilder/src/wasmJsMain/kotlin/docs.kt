import kotlinx.browser.window
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun HTMLDivElement.docsStyle() = style.apply {
    pageBoundary()
    bodyFont()
    width = "55%"
    marginTop = "2em"
}

fun Element.docs() {
    window.fetch("0.md").then { response ->
        if (response.ok) {
            response.text().then { markdownText ->
                val src = markdownText.toString()
                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(src)
                val html = HtmlGenerator(src, parsedTree, flavour).generateHtml()

                div {
                    docsStyle()
                    className = "Docs"
                    innerHTML = html
                }.applyHighlighting()

                null
            }
        } else {
            println("Error: ${response.status}")
        }
        null
    }
        .catch { error ->
            println("Fetch error: $error")
            null
        }
}

fun Element.applyHighlighting() {
    val codeBlocks = getElementsByClassName("language-kotlin")
    for (i in 0 until codeBlocks.length) {
        val element = codeBlocks.item(i) as? HTMLElement ?: throw Error("Element not found")
        element.innerHTML = tokenizeKotlinCode(element.textContent ?: "")
    }
}

fun tokenizeKotlinCode(code: String): String {
    val keywordPattern = Regex("\\b(val|var|fun|if|else|for|while|return|import|package|interface|private|enum|class)\\b")
    val typePattern = Regex("\\b(String|Int|Boolean|List|Map|Set|Any|Unit)\\b")
    val stringPattern = Regex("\".*?\"")
    val commentPattern = Regex("\\w+:?//.*?$|/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)

    val singleLineCommentPattern = Regex("""(\w+:)?(//[^\n]+)""")

    var highlightedCode = code

    highlightedCode = stringPattern.replace(highlightedCode) {
        """<span class="string">${it.value}</span>"""
    }

    highlightedCode = keywordPattern.replace(highlightedCode) {
        """<span class="keyword">${it.value}</span>"""
    }

    highlightedCode = typePattern.replace(highlightedCode) {
        """<span class="type">${it.value}</span>"""
    }

    highlightedCode = singleLineCommentPattern.replace(highlightedCode) { matchResult ->
        if (matchResult.groups.mapNotNull { it }.any {
                it.value.contains("(\\w+:)".toRegex())
            }
        ) matchResult.value
        else """<span class="comment">${matchResult.value}</span>"""
    }

    return highlightedCode
}
