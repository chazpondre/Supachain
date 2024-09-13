package dev.supachain.utilities

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

/**
 * Extension function to format a string value into the appropriate type based on the KClass.
 *
 * @receiver The KClass representing the type to format the value into.
 * @param value The string value to format.
 * @return The formatted value as the appropriate type.
 * @throws IllegalArgumentException If the type is unsupported.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<T>.castFormat(value: String): T = when {
    this == String::class -> value as T

    // Numeric Types (with decimal handling)
    this.isSubclassOf(Number::class) -> {
        val numericValue = value.toDoubleOrNull() ?: throw NumberFormatException("`$value` is not a number")
        when (this) {
            Byte::class -> numericValue.toInt().toByte()
            Short::class -> numericValue.toInt().toShort()
            Int::class -> numericValue.toInt()
            Long::class -> numericValue.toLong()
            Float::class -> numericValue.toFloat()
            Double::class -> numericValue
            BigDecimal::class -> numericValue.toBigDecimal()
            BigInteger::class -> numericValue.toBigDecimal().toBigInteger()
            // TODO ULong: Handle separately due to potential overflow from negative decimals
            ULong::class -> numericValue.toULong()
            else -> throw IllegalArgumentException("Unsupported numeric type: $this")
        } as T
    }

    // Boolean Type
    this == Boolean::class -> value.toBooleanStrictOrNull() as T// Use strict conversion to avoid parsing "true"/"false" variations

    // Date/Time Types
    this == LocalDate::class -> LocalDate.parse(value) as T
    this == LocalTime::class -> LocalTime.parse(value) as T
    this == LocalDateTime::class -> LocalDateTime.parse(value) as T

    else -> throw IllegalArgumentException("Unsupported type: $this")
}

/**
 * Collects all functions from the given class and its superinterfaces.
 *
 * This extension function recursively traverses the given class and its superinterfaces,
 * adding all declared member functions to the mutable list.
 *
 * @receiver MutableList<KFunction<*>> The list to collect functions into.
 * @return The receiver list, populated with functions from the class and its superinterfaces.
 *
 * @since 0.1.0-alpha
 */
fun KClass<*>.getFunctions(result: MutableList<KFunction<*>> = mutableListOf()): MutableList<KFunction<*>> {
    result.addAll(declaredMemberFunctions)
    superclasses.forEach { it.getFunctions(result) }
    return result
}

/**
 * Invokes the Kotlin function represented by this `KFunction` with the provided arguments.
 *
 * This operator function allows you to call a function reflectively using the familiar function call syntax.
 * It handles the conversion of arguments and provides a convenient way to execute functions dynamically at runtime.
 *
 * @receiver The `KFunction` object representing the function to be invoked.
 * @param args The arguments to be passed to the function. These can be of any type,
 *             and they will be automatically converted to match the function's parameter types if possible.
 * @return The result of the function invocation, or `null` if the function's return type is `Unit`.
 *
 * @throws IllegalArgumentException If the number or types of arguments do not match the function's parameters,
 *                                  or if any argument conversion fails.
 * @throws IllegalAccessException If the function is not accessible (e.g., private).
 */
operator fun KFunction<*>.invoke(vararg args: Any?) = call(* args)

/**
 * Sealed interface representing the possible types of arguments in a function call.
 */
sealed interface ArgumentType {
    sealed interface Literal : ArgumentType
    data object Number : Literal
    data object String : Literal
    data object Boolean : Literal
    data object FunctionCall : ArgumentType
    data object VarArg : ArgumentType
    data object Value : ArgumentType
}

/**
 * Determines the type of argument represented as a string.
 *
 * This function analyzes the input string and categorizes it into one of the following `ArgumentType`s:
 * - `Number`: If the string represents a valid number (integer or floating-point).
 * - `Boolean`: If the string is "true" or "false" (case-insensitive).
 * - `String`: If the string is enclosed in double quotes ("").
 * - `FunctionCall`: If the string represents a function call (e.g., "myFunction(arg1, arg2)").
 * - `Value`: If the string doesn't match any of the above, it's assumed to be a variable or expression.
 *
 * @receiver The string representing the argument.
 * @return The corresponding `ArgumentType` for the input string.
 */
fun String.argumentType(): ArgumentType {
    // Check for literals (numbers, booleans, strings)
    return when {
        matches("-?\\d+(\\.\\d+)?".toRegex()) -> ArgumentType.Number
        equals("true", ignoreCase = true) || equals("false", ignoreCase = true) -> ArgumentType.Boolean
        startsWith("\"") && endsWith("\"") -> ArgumentType.String

        // Check for function calls, including those with nested function calls in arguments
        "^\\w+\\(".toRegex().find(this) != null && endsWith(')') -> {
            ArgumentType.FunctionCall
        }

        // If not a literal or function call, consider it a value (variable or expression)
        else -> ArgumentType.Value
    }
}

/**
 * Extracts the short name of a Kotlin type represented by a `KType`.
 *
 * This function retrieves the simple name (without package) of the type and removes any nullability
 * indicator ("?").
 *
 * @receiver The `KType` representing the Kotlin type.
 * @return The short name of the type (e.g., "String", "Int", "List").
 */
fun KType.getShortName(): String {
    val typeName = this.toString()
    return typeName.substringAfterLast('.').replace("?", "")
}

/**
 * Represents a parameter within a function or tool, capturing its metadata.
 *
 * This data class encapsulates information about a parameter, including its:
 *
 * - **Kotlin Parameter Reference (Internal):** A `KParameter` object providing reflection-based access to the parameter's details.
 *   This is marked as `@Transient` and is not serialized, as it's primarily for internal use.
 * - **Description:** A human-readable description of the parameter's purpose or expected input, often derived from annotations.
 * - **Type:** The Kotlin `KType` representing the parameter's data type. This is serialized using a custom serializer (`KTypeSerializer`).
 * - **Name:** The name of the parameter as it appears in the code.
 * - **Required:**  Indicates whether the parameter is mandatory (true by default) or optional.
 *
 * @property kParameter The underlying `KParameter` object (not serialized).
 * @property description A description of the parameter.
 * @property type The Kotlin type of the parameter.
 * @property name The name of the parameter.
 * @property required Whether the parameter is required (default: true).
 *
 * @since 0.1.0-alpha
 * @version 1.0.0
 */
@Serializable
data class Parameter(
    @Transient val kParameter: KParameter =
        throw IllegalArgumentException(
            "A KParameter must be passed, since this value is @Transient there " +
                    "is a default throw expression"
        ),
    val description: String,
    @Serializable(with = KTypeSerializer::class)
    val type: KType,
    val name: String,
    val required: Boolean = true
) {
    override fun toString(): String = this.toJson()
}

object KTypeSerializer : KSerializer<KType> {
    override val descriptor = PrimitiveSerialDescriptor("KType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: KType) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): KType {
        throw NotImplementedError("Library does not support deserializer of KTypes")
    }
}

/**
 * Retrieves a list of annotation names applied to a Kotlin function, excluding specified annotations.
 *
 * This function extracts the simple names of all annotations present on a given `KFunction`,
 * filtering out any annotations whose simple names match the provided exclusion list.
 *
 * @param excluding A variable number of annotation simple names (Strings) to exclude from the result.
 * @return A list of annotation simple names (Strings) applied to the function, excluding the specified annotations.
 *
 * @since 0.1.0-alpha
 */
fun KFunction<*>.getAnnotationsByNameExcluding(vararg excluding: String) =
    annotations.mapNotNull { it.annotationClass.simpleName }.filterNot { it in excluding.toSet() }

fun KType.isEnumType(): Boolean {
    val kClass = classifier as? KClass<*> ?: return false
    return kClass.isSubclassOf(Enum::class)
}

fun KType.enumConstants(): List<String> =
    (classifier as? KClass<*>)?.java?.enumConstants?.map { it.toString() } ?: emptyList()

