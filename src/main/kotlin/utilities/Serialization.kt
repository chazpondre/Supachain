@file:Suppress("unused")

package dev.supachain.utilities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Serializes an object of type `T` into a JSON string.
 *
 * This inline function uses Kotlin serialization with the following configuration:
 *
 * - `encodeDefaults = false`: Default values are not included in the JSON output.
 * - `prettyPrint = true`: The JSON is formatted for readability (optional, can be removed for more compact output).
 *
 * @param T The type of the object to serialize. Must be a class annotated with `@Serializable`.
 * @receiver The object to serialize.
 * @return A JSON string representation of the object.
 */
inline fun <reified T> T.toJson(pretty: Boolean = true): String {
    val json = Json {
        encodeDefaults = false // Don't include default values in JSON
        prettyPrint = pretty     // Format the JSON for readability (optional)
    }

    return try {
        json.encodeToString(this)
    } catch (e: SerializationException) {
        throw IllegalArgumentException("Object is not serializable", e)
    }
}

/**
 * Interface for serializers that only require serialization (encoding) functionality.
 *
 * This interface simplifies the creation of custom serializers when deserialization is not needed.
 * It provides a default implementation for the `deserialize` function that throws a `NotImplementedError`
 * to indicate that deserialization is not supported.
 *
 * @param T The type of object to be serialized.
 *
 * @since 0.1.0
 */
interface SerializeWrite<T> : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T =
        throw NotImplementedError(
            "Library does not support deserializer of Parameter List (ai.anything.data.ParameterListSerializer)"
        )
}

/**
 * Determines the JSON Schema type corresponding to a given Kotlin type.
 *
 * This function parses a Kotlin type string and maps it to its equivalent JSON Schema type. It handles primitive types, collections, and custom types.
 *
 * **Parameters:**
 *  - `this`: The Kotlin type string to be converted.
 *
 * **Returns:**
 *  - A string representing the corresponding JSON Schema type.
 *
 * **Mapping:**
 *  - `Int`, `Byte`, `Short`, `Long`: `integer`
 *  - `Float`, `Double`: `number`
 *  - `Boolean`: `boolean`
 *  - `String`, `Char`: `string`
 *  - `Unit`: `null`
 *  - `Any`: `any`
 *  - `Array`, `List`, `Set`, `Collection`: `array`
 *  - `Map`: `object`
 *  - Other types starting with `kotlin.collections.`: `array`
 *  - Other types: `object`
 *
 * **Example:**
 *  ```kotlin
 *  val typeString = "kotlin.Int"
 *  val jsonSchemaType = typeString.toJSONSchemaType() // "integer"
 *  ```
 *
 * @since 0.1.0
 */
fun KType.toJSONSchemaType(): String = with(this.toString().substringAfterLast('.')) {
    when (removeSuffix("?")) {
        "Int", "Byte", "Short", "Long" -> "integer"
        "Float", "Double" -> "number"
        "Boolean" -> "boolean"
        "String", "Char" -> "string"
        "Unit" -> "null"
        "Any" -> "any"
        "Array", "List", "Set", "Collection" -> "array"
        "Map" -> "object"
        else -> {
            if (startsWith("kotlin.collections.")) {
                "array"
            } else {
                "object"
            }
        }
    }
}

/**
 * Formats a JSON string containing function arguments into a list of typed values based on parameter metadata.
 *
 * This function parses the input JSON string, extracts argument values based on the provided parameter map,
 * and converts them into the appropriate Kotlin types. It also validates that all required parameters are present
 * and throws an exception if any are missing or if the input is not valid JSON.
 *
 * @param keyRankParameterMap A map containing the parameter names as keys and their corresponding indices and
 *                           [Parameter] objects as values.
 * @return A list of formatted argument values, where each element corresponds to the parameter at the same index in
 *         the `keyRankParameterMap`.
 * @throws IllegalStateException If the input string is not a valid JSON object.
 * @throws IllegalArgumentException If an unknown parameter is provided, a parameter has an illegal type,
 *                                  a required parameter is missing, or the JSON value type is not a primitive.
 *
 * @since 0.1.0
 */
fun String.formatArgumentsFromJson(keyRankParameterMap: Map<String, Pair<Int, Parameter>>): Array<Any?> {
    // 1. Parse JSON: parse the arguments string into a JsonElement.
    val json = Json.parseToJsonElement(this) as? JsonObject
        ?: throw IllegalStateException("The provided parameters are not an JSON object. Given parameter: $this")

    // Prepare result
    val result = MutableList<Any?>(keyRankParameterMap.size) { null }

    // 2. Extract Parameters: Iterates through the JSON object.
    json.entries.forEach { (name, value) ->
        // a. Get parameter index and parameter from keyRankParameterMap
        val (index, parameter) = keyRankParameterMap[name]
            ?: throw IllegalArgumentException(
                "Provider gave an illegal parameter: $name. Available parameters: ${keyRankParameterMap.toJson()}"
            )

        // b. Get the format of the parameter
        val kClass = parameter.type.classifier as? KClass<*>
            ?: throw IllegalArgumentException("Parameter $name has an illegal type in: ${keyRankParameterMap.toJson()}")

        // c. Get JSON value in the entry and cast as the specific to specific type
        if (value is JsonPrimitive) {
            result[index] = kClass.castFormat(value.content)
        } else {
            throw IllegalArgumentException("Invalid value type for parameter '$name': Expected primitive, but got ${value::class}")
        }
    }

    // Verify Result
    keyRankParameterMap.forEach { (_, rankParameter) ->  // Iterate and check each parameter
        val (index, parameter) = rankParameter
        if (parameter.required && result[index] == null) {
            throw IllegalArgumentException("Missing required parameter: ${parameter.name}")
        }
    }

    return result.toTypedArray()
}

fun Map<String, JsonElement>.mapToFunctionCall(): String = values.joinToString()