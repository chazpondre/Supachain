import kotlinx.browser.document
import org.w3c.dom.*

fun main() {
    val body = document.body!!
    body.build()
}

fun Element.build() {
    hero()
    aboutSupachain()
    docs()
}

fun HTMLElement.bodyStyle() = style.apply {
    pageBoundary()
    bodyFont()
}


