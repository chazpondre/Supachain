@file:Suppress("unused")

package dev.supachain.robot.provider.tools

import dev.supachain.robot.messenger.messaging.ToolCall
import dev.supachain.robot.tool.ToolConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class ToolListReceive<In> : KSerializer<List<ToolCall>> {
    // Method to deserialize from the target format (In) to ToolConfig
    abstract fun toolIn(tool: In): ToolCall

    // Serializer for the target format for deserialization (JSON B -> In)
    abstract fun inSerializer(): KSerializer<In>

    override val descriptor: SerialDescriptor by lazy { ListSerializer(inSerializer()).descriptor }

    override fun deserialize(decoder: Decoder): List<ToolCall> {
        // Decode the serialized data as a List of IN and map it back to ToolConfig
        val serializedList = decoder.decodeSerializableValue(ListSerializer(inSerializer()))
        return serializedList.map { toolIn(it) }
    }

    override fun serialize(encoder: Encoder, value: List<ToolCall>) =
        throw IllegalStateException("Class can not be used for serialization")
}

abstract class ToolListSend<Out>: KSerializer<List<ToolConfig>> {
    // Method to serialize ToolConfig into the target format (Out)
    abstract fun toolOut(toolConfig: ToolConfig): Out

    // Serializer for the target format for serialization (TOut -> JSON A)
    abstract fun outSerializer(): KSerializer<Out>

    // The descriptor should match the one used for deserialization
    override val descriptor: SerialDescriptor by lazy { ListSerializer(outSerializer()).descriptor }

    override fun deserialize(decoder: Decoder): List<ToolConfig> =
        throw IllegalStateException("Class can not be used for deserialization")

    override fun serialize(encoder: Encoder, value: List<ToolConfig>) {
        val serializedList = value.map { toolOut(it) }
        encoder.encodeSerializableValue(ListSerializer(outSerializer()), serializedList)
    }
}