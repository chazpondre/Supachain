package providers

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

fun JsonElement.isContainedIn(other: JsonElement, path: String = "obj"): Boolean = when {
    this is JsonObject && other is JsonObject -> {
        entries.all { (key, value) ->
            val newPath = "$path.$key"
            if (!other.containsKey(key)) {
                println("Missing key at $newPath")
                false
            } else {
                value.isContainedIn(other[key]!!, newPath)
            }
        }
    }

    this is JsonArray && other is JsonArray -> {
        this.all { element ->
            other.any { otherElement ->
                element.isContainedIn(otherElement, path)
            }
        }
    }

    else -> (this == other).also { if(!it) println("$path: $this != $other") }
}


fun JsonElement.equalsJsonUnordered(
    other: JsonElement, path: String = "",
): Boolean = when {
    this is JsonObject && other is JsonObject -> {
        if (this.size != other.size) {
            println("Size mismatch at $path: Expected ${other.size} keys, found ${this.size}")
            false
        } else {
            val allKeys = (this.keys + other.keys).toSet()
            allKeys.all { key ->
                val newPath = "$path.$key"
                if (!this.containsKey(key)) {
                    println("Missing key at $newPath")
                    false
                } else if (!other.containsKey(key)) {
                    println("Missing key at $newPath")
                    false
                } else {
                    this[key]!!.equalsJsonUnordered(other[key]!!, newPath)
                }
            }
        }
    }

    this is JsonArray && other is JsonArray -> {
        if (this.size != other.size) {
            println("Array size mismatch at $path: Expected ${other.size}, found ${this.size}")
            false
        } else {
            this.zip(other).withIndex().all { (index, entry) ->
                val (element1, element2) = entry
                val newPath = "$path[$index]"
                element1.equalsJsonUnordered(element2, newPath)
            }
        }
    }

    else -> {
        if (this != other) {
            println("Value mismatch at $path: Expected $other, found $this")
            false
        } else true
    }
}