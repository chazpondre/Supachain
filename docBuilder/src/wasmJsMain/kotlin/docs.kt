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

                "div" {
                    this as HTMLDivElement
                    docsStyle()
                    className = "Docs"
                    innerHTML = html
                }
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