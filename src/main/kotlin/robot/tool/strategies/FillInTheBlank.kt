package dev.supachain.robot.tool.stategies

import dev.supachain.robot.tool.asKFunctionString
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.director.Director
import dev.supachain.robot.director.asFunctionCall
import dev.supachain.robot.messenger.messaging.CommonResponse
import dev.supachain.robot.messenger.asSystemMessage
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.ToolMap
import dev.supachain.utilities.templates

/**
 * Represents a tool-use strategy where the AI fills in placeholders within a template response.
 * It allows the AI to combine or chain multiple tool calls to achieve a complex task.
 *
 * This data object implements the `ToolUseStrategy` interface. It handles responses that contain
 * placeholders for function calls. The strategy directly processes these placeholders by executing the corresponding
 * functions and inserting their results into the response template.
 *
 * @since 0.1.0-alpha

 */
data object FillInTheBlank : ToolUseStrategy {
    /**
     * Constructs a system message listing available functions and providing usage instructions.
     *
     * This function generates a system message that informs the AI about the available functions
     * and how to use them within response templates.
     *
     * @param director The `Director` instance containing the list of available tools.
     * @return A `Message` object representing the system message.
     */
    override fun message(director: Director<*, *, *>): Message =
        if (director.allTools.isEmpty()) "Answer to the best of your ability".asSystemMessage()
        else
            ("Declared Functions: [${director.allTools.asKFunctionString()}, " +
                    "fun doubleArrayOf(vararg elements: Double): DoubleArray, " +
                    "fun floatArrayOf(vararg elements: Float): FloatArray, " +
                    "fun longArrayOf(vararg elements: Long): LongArray, " +
                    "fun intArrayOf(vararg elements: Int): IntArray, " +
                    "fun charArrayOf(vararg elements: Char): CharArray, " +
                    "fun shortArrayOf(vararg elements: Short): ShortArray, " +
                    "fun byteArrayOf(vararg elements: Byte): ByteArray, " +
                    "fun booleanArrayOf(vararg elements: Boolean): BooleanArray]. " +
                    "These functions are already declared for you, " +
                    "you can just call it in your template string if available. " +
                    "#Example:\n" +
                    "If this question requires you to add, " +
                    "and an add function is available do not show the results. " +
                    "You must write as a kotlin template string except $ is \u200B . " +
                    "For instance if the question was [can you add b + c?], you would say something like " +
                    "[The answer is \u200B{add(b, c)}]. If the function you want is !in Declared Functions, " +
                    "then just answer to your best ability what you know and show the results. " +
                    "Remember to add \u200B infront of `{` like \u200B{...} instead of \${...}. " +
                    "Sometimes you need nested function calls. " +
                    "For instance if the question was [can you add a * b + c?], " +
                    "you can write something like [The answer is \u200B{add(multiple(a, b), c)}]. " +
                    "For instance if the question was [can you add d + a * b + c - x?], " +
                    "you can write something like [The answer is \u200B{subtract(add(add(d, multiple(a, b)), c)}, x)]" +
                    "For instance if the question was [v + w * x / y - z], " +
                    "you can say something like [The answer to the equation is \u200B{minus(add(v, divide(multiplies(w, x), y)), z)}]. " +
                    "You must follows these instructions. " +
                    "1. If you knew the answer to a + b, but a function called `add` is in Declared Functions, " +
                    "you must always write you answer call function in template string. I.e [The answer to a + b is \u200B{add(a + b]}]. " +
                    "2. If you know the answer to something, but there is a function for that something. " +
                    "Use tha function in the template string." +
                    "3. In terms of arrays, say you had a, b, c in an array, " +
                    "never write [a, b, c] literal form, always write the most suitable function call " +
                    "like intArrayOf(a, b, c) or arrayOf(a, b, c) etc.\n" +
                    "4. You are not allowed to use kotlin named arguments. For instance for fun z(a: Int, b:Int), " +
                    "you cannot write `We have \u200B{z(a=0, b=1)}`. You can write `We have \u200B{z(0, 1)}`.\n" +
                    "5. Whenever you encounter an arithmetic operation or any calculation, " +
                    "instead of directly writing the expression (e.g., \u200B{11 * 71}), " +
                    "wrap the operation inside a descriptive function or method (e.g., \u200B{multiply(11 * 71)}). " +
                    "This approach should be applied not just for multiplication, but for any calculation, " +
                    "such as addition, subtraction, division, or more complex operations. " +
                    "The function names should clearly represent the operation or purpose " +
                    "(e.g., \u200B{add(5 + 3)}, \u200B{subtract(10 - 2)}, " +
                    "\u200B{calculateArea(length * width)}), " +
                    "enhancing the clarity and maintainability of the code.\n" +
                    "Remember: If a function is not in Declared Functions, " +
                    "Do not make up tool. For instance if were to sum a and b and " +
                    "there isn't some kind of sum/add function. Just guess the answer" +
                    "I.e `whats 2 - 1` you would say `The answer is 1` be guessing and not templating. " +
                    "In all Kotlin code, strictly adhere to using positional arguments when calling functions. " +
                    "Do not use named arguments (e.g., `myFunction(arg2 = \"value\")`)."
                    ).asSystemMessage()

    /**
     * Invokes the fill-in-the-blank tool use strategy.
     *
     * This function processes a response from the AI provider by executing any function calls
     * embedded within the response template and replacing them with their results.
     *
     * @param director The `Director` instance responsible for managing the AI interaction.
     * @param response The `CommonResponse` received from the AI provider.
     */
    operator fun invoke(director: Director<*, *, *>, response: CommonResponse) = with(director) {
        val template = response.message.content.templates()
        val results = template.expressions.map { it.asFunctionCall()().toString() }

        messenger.lastMessage().content = template.fill(results)
    }

    override fun getTools(toolMap: ToolMap): List<ToolConfig> = emptyList()
}