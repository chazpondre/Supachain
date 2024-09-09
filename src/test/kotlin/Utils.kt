import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// Helper function to compare JSON objects ignoring order
fun JsonElement.equalsJsonUnordered(other: JsonElement): Boolean = when {
        this is JsonObject && other is JsonObject -> {
            this.size == other.size && this.all { (key, value) ->
                other[key]?.equalsJsonUnordered(value) == true
            }
        }
        this is JsonArray && other is JsonArray -> {
            this.size == other.size && this.zip(other).all { (element1, element2) ->
                element1.equalsJsonUnordered(element2)
            }
        }
        else -> this == other
    }