package dev.supachain.robot.director

import dev.supachain.robot.director.directive.Directive
import dev.supachain.robot.director.directive.FromSystem
import dev.supachain.robot.director.directive.FromUser
import dev.supachain.robot.director.directive.Use
import dev.supachain.robot.messenger.messaging.FunctionCall
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.messenger.messaging.asSystemMessage
import dev.supachain.robot.messenger.messaging.asUserMessage
import dev.supachain.robot.provider.Feature
import dev.supachain.robot.tool.Parameters
import dev.supachain.robot.tool.RobotTool
import dev.supachain.robot.tool.Tool
import dev.supachain.robot.tool.ToolSet
import dev.supachain.utilities.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

/**
 * Utility interface providing helper functions for message and method processing.
 */
interface Extensions

@Suppress("unused")
private val logger: Logger get() = LoggerFactory.getLogger(Extensions::class.java)

/**
 * Extension function to retrieve Robot methods from a Kotlin class.
 *
 * This function collects methods annotated with specific annotations, such as [FromSystem]
 * and [FromUser], and creates [Directive] objects representing these methods.
 *
 * @param T The type of the class.
 * @return A list of [Directive] objects representing the Robot methods.
 *
 * @since 0.1.0-alpha

 * @see Directive
 */
fun <T : Any> KClass<T>.getDirectives(): List<Directive> =
    this.declaredFunctions.map { function ->
        // Create and return a MethodAPI object
        Directive(
            function.name,
            function.parameters(),
            function.returnType,
            function.messages(),
            function.feature(),
        )
    }


/**
 * Collects messages from annotations on the current function.
 *
 * This function extracts messages from the following annotations:
 *
 * * **[FromSystem]:** Added as a system message (Role.SYSTEM).
 * * **[FromUser]:**  Added as a user message (Role.USER).
 *
 * @return A mutable list of [Message] objects derived from the found annotations. If no relevant annotations are present, an empty list is returned.
 *
 * @since 0.1.0-alpha

 */
private fun KFunction<*>.messages(): MutableList<Message> {
    val messages = mutableListOf<Message>().apply {
        findAnnotation<FromSystem>()?.apply { add(message.asSystemMessage()) }
        findAnnotation<FromUser>()?.apply { add(message.asUserMessage()) }
    }
    return messages
}

/**
 * Extracts the `Feature` specified by the `@Use` annotation on this function.
 *
 * This extension function uses Kotlin reflection to inspect the function and retrieve the `Feature`
 * value from the `@Use` annotation, if present. If the annotation is not found, the default value
 * `Feature.Chat` is returned.
 *
 * @return The `Feature` associated with this function via the `@Use` annotation, or `Feature.Chat` if no annotation is found.
 *
 * @since 0.1.0-alpha

 */
private fun KFunction<*>.feature(): Feature =
    findAnnotation<Use>()?.feature ?: Feature.Chat

/**
 * Extracts parameter information from a Kotlin function using the `@Parameters` annotation.
 *
 * This extension function operates on a `KFunction` and attempts to retrieve a list of `Parameter` objects
 * representing the function's parameters. It does this by looking for the `@Parameters` annotation on the function.
 *
 * If the `@Parameters` annotation is present, the function combines the names specified in the annotation with
 * the function's `valueParameters` (the actual parameters in the function signature) to create a list of `Parameter`
 * objects. Each `Parameter` object contains:
 * - The name specified in the `@Parameters` annotation (or the parameter name from the signature if no name is provided in the annotation).
 * - The type of the parameter (as a String representation).
 * - The parameter name from the function signature (non-null assertion is used since parameters should always have a name).
 *
 * If the `@Parameters` annotation is not found on the function, the function returns `null`, indicating that
 * parameter information cannot be extracted.
 *
 * @receiver The Kotlin function (`KFunction`) to analyze.
 * @return A list of `Parameter` objects representing the function's parameters, or `null` if the `@Parameters` annotation is missing.
 *
 * @since 0.1.0-alpha

 */
private fun KFunction<*>.parameters(): List<Parameter> {
    val parametersAnnotation = findAnnotation<Parameters>()?.description ?: emptyArray()
    return valueParameters.mapIndexed { index, param ->
        val description = parametersAnnotation.getOrNull(index) ?: ""
        val typeName = param.type
        val isRequired = !param.isOptional && !param.type.isMarkedNullable
        Parameter(param, description, typeName, param.name!!, isRequired)
    }
}

/**
 * Extracts a list of `RobotTool` objects representing the configuration of tool-annotated functions within the provided Kotlin class.
 *
 * These `RobotTool` objects provide information about functions designated as tools for the Director to utilize when processing directives.
 *
 * **Parameters:**
 *  - **`this`**: The Kotlin class to be analyzed for tool-annotated functions.
 *
 * **Returns:**
 *  - A list of `RobotTool` objects, each encapsulating details about a tool function, or an empty list if no tool functions are found.
 *
 * **Process:**
 * 1. **Exclusion Handling:** Defines a list of excluded function names (`equals`, `hashCode`, `toString`).
 * 2. **`ToolSet` Annotation Check:** Verifies if the class itself is annotated with `@ToolSet`.
 * 3. **Function Iteration:** Iterates through all functions declared within the class.
 * 4. **`@Tool` Annotation Check:** For each function, searches for the presence of the `@Tool` annotation.
- If `@Tool` is not found and the class has `@ToolSet`, the function is skipped.
- If `@Tool` is not found and the class does not have `@ToolSet`, the function is excluded.
 * 5. **Excluded Function Check:** Skips functions whose names are present in the exclusion list.
 * 6. **Parameter Extraction:** Extracts a list of `Parameter` objects representing the function's parameters.
 * 7. **Other Annotations Gathering:** Retrieves a list of annotation names excluding `@Tool` and `@Parameters`.
 * 8. **`RobotTool` Creation:** Creates a `RobotTool` object encapsulating the function's name, tool description (if provided), parameters, other annotations, and a reference to the function itself.
 * 9. **Result Collection:** Adds the created `RobotTool` object to the result list.
 *
 * **By using this function, you can discover and extract tool functionalities declared within a Kotlin class for integration with the Director.**
 *
 * @since 0.1.0-alpha
 */
fun KClass<*>.getToolMethods(): List<RobotTool> {
    val exclusions = listOf("equals", "hashCode", "toString")
    val isToolSet = this.findAnnotation<ToolSet>() != null
    return getFunctions().mapNotNull { function ->
        val toolAnnotation: Tool? = function.findAnnotation<Tool>() ?: if (isToolSet) null else return@mapNotNull null
        if (function.name in exclusions) return@mapNotNull null
        val functionParameters: List<Parameter> = function.parameters()
        val otherAnnotations: List<String> =
            function.getAnnotationsByNameExcluding(Tool::class.simpleName!!, Parameters::class.simpleName!!)
        RobotTool(function.name, toolAnnotation?.description ?: "", functionParameters, otherAnnotations, function)
    }
}

/**
 * Parses a string representation of a function call into a `FunctionCall` object.
 *
 * This function extracts the function name and arguments from a string that follows
 * the format "functionName(arg1, arg2, ...)".
 *
 * @receiver The string representing the function call.
 * @return A `FunctionCall` object containing the extracted name and arguments.
 * @throws IllegalArgumentException If the input string does not have a valid function call format.
 */
fun String.asFunctionCall(): FunctionCall {
    val openingParenIndex = indexOf('(')
    require(openingParenIndex != -1) { "Missing opening parenthesis in function call: $this" }

    val name = substring(0, openingParenIndex).trim()
    val args = substring(openingParenIndex + 1, lastIndexOf(')')).trim()
    return FunctionCall(args, name)
}

/**
 * Converts a string representation of an argument into its corresponding Kotlin type based on a given parameter.
 *
 * This function uses the type information from the `Parameter` object to correctly parse the string and
 * return a value of the appropriate type.
 *
 * @receiver The string representation of the argument.
 * @param parameter The `Parameter` object containing the expected type information.
 * @return The parsed argument value as an `Any` object.
 * @throws IllegalArgumentException If the parameter has an unsupported type or the string cannot be converted to the expected type.
 */
fun String.asParameterType(parameter: Parameter): Any {
    val kClass = parameter.type.classifier as? KClass<*>
        ?: throw IllegalArgumentException("Parameter has an illegal type. Parameter: $parameter")
    return kClass.castFormat(this)
}

/**
 * Converts a string representation of a vararg argument into its corresponding Kotlin array type based on a given parameter.
 *
 * This function extracts the individual argument values from the string, parses them according to the
 * element type of the vararg parameter, and returns an array containing the parsed values.
 *
 * @receiver The string representation of the vararg argument.
 * @param parameter The `Parameter` object representing the vararg parameter.
 * @return An array of the parsed argument values.
 * @throws IllegalArgumentException If the parameter is not a vararg or has an unsupported type.
 */
fun String.asVarargType(parameter: Parameter): Any {
    val args = replaceBefore("(", "").replaceAfter(")", "")
        .replace(")", "")
        .replace("(", "").split(',').map(String::trim)

    val kType = parameter.kParameter.type
    val classifier = kType.classifier as KClass<*>

    return when (classifier) {
        IntArray::class -> IntArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toInt() } }
        LongArray::class -> LongArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toLong() } }
        FloatArray::class -> FloatArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toFloat() } }
        DoubleArray::class ->
            DoubleArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toDouble() } }


        ByteArray::class -> ByteArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toByte() } }
        ShortArray::class -> ShortArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toShort() } }
        CharArray::class -> CharArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.first() } }
        BooleanArray::class ->
            BooleanArray(args.size).apply { args.forEachIndexed { i, item -> this[i] = item.toBoolean() } }

        Array::class -> Array<Any>(args.size) { index ->
            when (args[index].argumentType()) {
                ArgumentType.Number -> args[index].toInt()
                ArgumentType.FunctionCall -> TODO()
                ArgumentType.VarArg -> TODO()
                ArgumentType.Value -> TODO()
                ArgumentType.String -> args[index]
                ArgumentType.Boolean -> args[index].toBoolean()
            }
        }

        else -> throw IllegalArgumentException("Unsupported type $kType")
    }
}

/**
 * Splits a string into a list of arguments while respecting nested parentheses and string literals.
 *
 * This function takes a string representing a sequence of arguments (potentially containing nested function calls
 * and string literals) and splits it into a list of individual argument strings.
 *
 * The splitting is performed based on commas (`,`) that are not within nested parentheses or string literals.
 * String literals enclosed in double quotes (`"`) are treated as a single argument, even if they contain commas.
 *
 * @receiver The string containing the arguments to be split.
 * @return A list of strings representing the individual arguments.
 *
 * @since 0.1.0-alpha

 */
fun String.splitArguments(): List<String> {
    val args = mutableListOf<String>()
    val currentArg = StringBuilder()
    var parenthesesCount = 0
    var inStringLiteral = false

    for (char in this) {
        when {
            char == '"' -> {
                inStringLiteral = !inStringLiteral
                currentArg.append(char)
            }

            inStringLiteral -> currentArg.append(char)

            !inStringLiteral && char == '(' -> {
                currentArg.append(char)
                parenthesesCount++
            }

            !inStringLiteral && char == ')' -> {
                currentArg.append(char)
                parenthesesCount--
            }

            char == ',' && parenthesesCount == 0 && !inStringLiteral -> {
                if (currentArg.isNotEmpty()) {
                    args.add(currentArg.toString().trim())
                    currentArg.clear()
                }
            }

            else -> currentArg.append(char)
        }
    }

    if (currentArg.isNotEmpty()) args.add(currentArg.toString().trim())

    return args.map(String::trim)
}
