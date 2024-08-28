import kotlinx.browser.document
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.*

context(Element)
operator fun String.unaryPlus() = appendText(this)

context(HTMLUListElement)
operator fun String.unaryPlus() = "li" { appendText(this@unaryPlus) }

context(HTMLUListElement)
operator fun Element.unaryPlus() = "li" { append(this@unaryPlus) }

context(Element)
operator fun String.invoke(builder: Element.() -> Unit) = appendElement(this, builder)

fun Element.div(builder: HTMLDivElement.() -> Unit) = "div" {
    this as HTMLDivElement
    builder(this)
}

fun Element.span(builder: HTMLSpanElement.() -> Unit) = "span" {
    this as HTMLSpanElement
    builder(this)
}

fun Element.h2(builder: HTMLHeadingElement.() -> Unit) = "h2" {
    this as HTMLHeadingElement
    builder(this)
}

fun Element.p(builder: HTMLParagraphElement.() -> Unit) = "p" {
    this as HTMLParagraphElement
    builder(this)
}

fun Element.ul(builder: HTMLUListElement.() -> Unit) = "ul" {
    this as HTMLUListElement
    builder(this)
}

fun Element.li(builder: HTMLLIElement.() -> Unit) = "li" {
    this as HTMLLIElement
    builder(this)
}

fun HTMLElement.pageBoundary() = style.apply {
    width = "90%"
    left = "50%"
    transform = "translateX(-50%)"
    position = "relative"
}

fun HTMLUListElement.example(point: String, explanations: String) =
    "li" {
        "strong" { appendText("$point: ") }
        appendText(explanations)

    }