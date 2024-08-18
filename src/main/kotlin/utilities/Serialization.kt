package dev.supachain.utilities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
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
inline fun <reified T> T.toJson(): String {
    val json = Json {
        encodeDefaults = false // Don't include default values in JSON
        prettyPrint = true     // Format the JSON for readability (optional)
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
 * @since 0.1.0-alpha
 */
interface SerializeWrite<T> : KSerializer<T> {
    override fun deserialize(decoder: Decoder): T =
        throw NotImplementedError(
            "Library does not support deserializer of Parameter List (ai.anything.data.ParameterListSerializer)"
        )
}

/**
 * Converts a Kotlin type representation (String) to its corresponding JSON Schema type.
 *
 * This function takes a string representation of a Kotlin type and returns its equivalent type in JSON Schema. It handles
 * various primitive types, collections, and custom types, mapping them to appropriate JSON Schema types based on their characteristics.
 *
 * @param type The string representation of the Kotlin type (e.g., "String", "Int?", "List<MyClass>").
 * @return The corresponding JSON Schema type string (e.g., "string", "integer", "array").
 *
 * @throws IllegalStateException If the input type is not recognized or supported.
 * @since 0.1.0-alpha
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