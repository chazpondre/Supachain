import kotlinx.browser.window
import kotlinx.dom.appendText
import org.w3c.dom.*

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
    zIndex = "1"
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

fun HTMLDivElement.logo() = style.apply {
    position = "absolute"
    left = "40px"
    top = "30px"
    zIndex = "2"
}

fun HTMLDivElement.githubLogo() = style.apply {
    position = "absolute"
    right = "40px"
    top = "30px"
    zIndex = "2"
    height = "${logoSize * 0.75}px"
    width = "${logoSize * 0.75}px"
//    backgroundColor = "black"

    borderRadius = "10px"
    backgroundImage = "url(github_white.png)"
    backgroundSize = "cover"
    backgroundPosition = "center"
    backgroundRepeat = "none"

    cursor = "pointer"
}


fun HTMLDivElement.logoImg() = style.apply {
    backgroundImage = "url(icon.png)"
    backgroundSize = "cover"
    backgroundPosition = "center"
    backgroundRepeat = "none"
    logoElement()
}

const val logoSize = 35

fun HTMLDivElement.logoElement() = style.apply {
    height = "${logoSize}px"
    cssFloat = "left"
    width = "${logoSize}px"
}


fun HTMLDivElement.logoText() = style.apply {
    logoElement()
    verticalAlign = "middle"
    lineHeight = "${logoSize}px"
    fontSize = "3rem"
    display = "inline"
    paddingLeft = "${logoSize / 4}px"
    fontFamily = "Electrolize, sans-serif"
}

fun Element.hero() {
    div {
        heroRoundedBox()
        id = "hero"

        div {
            "span" {
                this as HTMLSpanElement
                appendText("The Foundational Layer".uppercase())
                gradientText()
            }

            appendText(" For AI Application Development".uppercase())
            heroTextBox()
        }

        div {
            logo()
            div {
                logoImg()
            }
            div {
                +"SUPACHAIN"
                logoText()
            }
        }

        div {
            githubLogo()
        }.addEventListener("click", {
            window.open("https://github.com/anythingdev/Supachain", "_blank")
        })
    }
}