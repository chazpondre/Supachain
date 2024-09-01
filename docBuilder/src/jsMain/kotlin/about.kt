import org.w3c.dom.*

fun HTMLElement.bodyFont() = style.apply {
    fontSize = "2em"
    lineHeight = "1.7em"
    fontFamily = "Montserrat, sans-serif"
    fontWeight = "400"
    fontStyle = "normal"
}

fun HTMLDivElement.bodyFont() = style.apply {
    fontSize = "2em"
    lineHeight = "1.7em"
    fontFamily = "Montserrat, sans-serif"
    fontWeight = "400"
    fontStyle = "normal"
}


fun HTMLSpanElement.headingFont() = style.apply {
    fontSize = "4em"
    fontStyle = "bold"
    display = "block"
    padding = "0.25em 0px"
    bodyFont()
}

fun HTMLDivElement.aboutStyle() = style.apply {
    pageBoundary()
    bodyFont()
    width = "70%"
    marginTop = "2em"
}

fun Element.aboutSupachain() {
    div {
        aboutStyle()
        h2 { +"The Library" }
        p {
            +"""Supachain is foundational open-source framework for building AI applications with Kotlin. 
            | The library intends to make it easier for individuals and businesses to use Large Language Models (LLMs) 
            | and to make their businesses autonomous. Supachain trys create a new class of businesses, we call
            | mostly autonomous businesses (MABs) and aims to provide tools that help reduce workloads, while keeping
            | a human in the loop for certain workflows. 
         """.trimMargin()
        }

        p {
            +"""By leveraging Supachain, you can: """
        }

        ul {
            example(
                "Build context-aware, reasoning applications",
                "Create AI solutions that understand and respond to complex information."
            )

            example(
                "Leverage your company's data and APIs",
                "Integrate your existing assets into AI-powered workflows."
            )

            example(
                "Future-proof your applications",
                "Design LLM infrastructure that is adaptable to evolving technologies and vendors."
            )

            example(
                "Accelerate AI development",
                "Ship reliable generative AI apps faster with Supachain's streamlined framework."
            )

            example(
                "Harness the power of AI Robots",
                "Create intelligent agents that can automate tasks, summarize information, and " +
                        "perform complex workflows."
            )
        }

    }
}


