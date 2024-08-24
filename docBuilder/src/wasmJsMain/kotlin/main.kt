import kotlinx.browser.document
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

fun main() {
    document.body?.makeComponents()
}

fun Element.makeComponents() {
    hero()
}

fun Element.hero() {
    appendElement("div") {
        className = "header"
        this as HTMLDivElement
        style.apply {
            top = "40px"
            width = "90%"
            height = "60vh"
            borderRadius = "50px"
            backgroundColor = "rgb(48, 4, 48)"
            position = "relative"
            left = "50%"
            transform = "translateX(-50%)"
            boxSizing = "border-box"
        }

        appendElement("div") {
            this as HTMLDivElement

            appendElement("span") {
                this as HTMLSpanElement

                appendText("The Foundational Layer".uppercase())
                style.apply {
                    background =
                        "linear-gradient(270deg, rgb(145 56 255) 4.25%, rgb(255 56 169) 51.61%, rgb(245 119 102) 80.43%, rgb(255 250 229) 93.03%) text"
                    backgroundClip = "text"
                    color = "transparent"
                }
            }

            appendText(" For AI Application Development".uppercase())

            style.apply {
                padding = "0 20"
                width = "100%"
                position = "absolute"
                bottom = "30px"
                padding = "0 30px"
                boxSizing = "border-box"
                maxWidth = "900px"
            }
        }
    }
}

operator fun String.invoke() {

}