package dev.supachain.robot.provider.models

import dev.supachain.utilities.Parameter
import dev.supachain.utilities.enumConstants
import dev.supachain.utilities.getShortName
import dev.supachain.utilities.isEnumType
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Property(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

fun List<Parameter>.toProperties(): Map<String, Property> {
    return associate {
        if (it.type.isEnumType())
            it.name to Property(
                "string",
                it.description.ifBlank { null },
                it.type.enumConstants()
            )
        else
            it.name to Property(
                it.type.getShortName().lowercase(Locale.US),
                it.description.ifBlank { null }
            )
    }
}