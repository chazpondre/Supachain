import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.HTMLSpanElement

fun HTMLDivElement.heroRoundedBox() = style.apply {
    pageBoundary()
    marginTop = "40px"
    height = "60vh"
    borderRadius = "50px"
    backgroundColor = "rgb(48, 4, 48)"
    boxSizing = "border-box"
    fontFamily = "Dosis, sans-serif"
    fontWeight = "500"
    fontStyle = "normal"
    fontSize = "9em"
    lineHeight = "1.1em"
    color = "#dedede"
}

fun HTMLDivElement.heroTextBox() = style.apply {
    padding = "0 20"
    width = "100%"
    position = "absolute"
    bottom = "30px"
    padding = "0 30px"
    boxSizing = "border-box"
    maxWidth = "900px"
}

fun HTMLSpanElement.gradientText() = style.apply {
    background =
        "linear-gradient(270deg, rgb(145 56 255) 4.25%, rgb(255 56 169) 51.61%, rgb(245 119 102) 80.43%, rgb(255 250 229) 93.03%) text"
    backgroundClip = "text"
    color = "transparent"
}

fun Element.hero() {
    "div" {
        this as HTMLDivElement
        heroRoundedBox()
        id = "hero"

        "div" {
            this as HTMLDivElement

            "span" {
                this as HTMLSpanElement
                appendText("The Foundational Layer".uppercase())
                gradientText()
            }

            appendText(" For AI Application Development".uppercase())
            heroTextBox()
        }
    }
}