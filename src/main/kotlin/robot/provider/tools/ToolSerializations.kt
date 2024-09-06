@file:Suppress("unused")

package dev.supachain.robot.provider.tools

import dev.supachain.robot.tool.ToolConfig
import dev.supachain.utilities.SerializeWrite
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes a list of `ToolConfig` objects into a format compatible with OpenAI function calls.
 *
 * This serializer transforms a list of `ToolConfig` objects, which represent tool configurations, into
 * a list of `OpenAiFunctionSchema` objects. This conversion is necessary for proper communication with the
 * OpenAI API, which expects function descriptions in a specific format.
 *
 * @since 0.1.0-alpha

 */
abstract class SerialToolList<T> : SerializeWrite<List<ToolConfig>> {
    abstract fun serialTool(toolConfig: ToolConfig): T
    abstract fun toolSerializer(): KSerializer<T>

    override val descriptor: SerialDescriptor by lazy { ListSerializer(toolSerializer()).descriptor }

    // Converts List<ToolConfig> into the toolSerializer format
    override fun serialize(encoder: Encoder, value: List<ToolConfig>) {
        encoder.encodeSerializableValue(
            ListSerializer(toolSerializer()),
            value.map { serialTool(it) })
    }
}