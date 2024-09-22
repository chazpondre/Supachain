package dev.supachain.robot.director.directive

import dev.supachain.robot.answer.Answer
import dev.supachain.robot.answer.formatRules
import dev.supachain.robot.provider.Feature
import dev.supachain.robot.provider.models.TextMessage
import dev.supachain.robot.provider.models.asSystemMessage
import dev.supachain.robot.provider.models.asUserMessage
import dev.supachain.utilities.KTypeSerializer
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJson
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure


class Objective(val data: Pair<Directive, Array<Any?>>) : AbstractDirective by data.first {
    constructor(directive: Directive, arguments: Array<Any?>) : this(Pair(directive, arguments))

    private val directive: Directive get() = data.first
    val arguments: Array<Any?> get() = data.second


    /**
     * Converts method arguments of a method into a list of messages.
     *
     * @param directiveArguments The arguments passed to the method.
     * @return A list of messages representing the arguments.
     */
    fun argumentMessages(usePrimer: Boolean): List<TextMessage> =
        List(parameters.size) { index -> arguments[index] }.mapIndexed { index, arg ->
            val givenName = parameters[index].description
            val localName = parameters[index].name
            val name = givenName.ifBlank { localName }

            if (usePrimer) "Your task is to answer question in $name. $name=`$arg`".asUserMessage()
            else "$arg".asUserMessage()
        }


    override fun toString(): String {
        return "↓↓↓↓↓↓↓  Objective  ↓↓↓↓↓↓↓\n - Directive: $directive\n - Arguments: $arguments\n - Feature: $feature"
    }
}

interface AbstractDirective {
    val name: String
    val parameters: List<Parameter>

    @Serializable(with = KTypeSerializer::class)
    val returnType: KType
    val messages: List<TextMessage>
    val feature: Feature
    val rankedConfigMessages get() = messages.sortedBy { it.role.ordinal }


    /**
     * Determines the expected output format based on a method's return type.
     *
     * This function analyzes the return-type of a given `Directive` to provide instructions on how the Robot model
     * should format its response.
     * It supports common types like numbers, booleans, collections, dates, and enums. If an unsupported type is
     * encountered, an exception is thrown.
     *
     * @return A string containing instructions on how to format the output.
     * @throws IllegalArgumentException If the return type is `Unit` or unsupported.
     *
     * @since 0.1.0-alpha
     */
    fun formattingMessage(): TextMessage {
        // Extract the KClass representing the return type (handling Deferred)
        val returnClass = when (returnType.classifier) {
            Answer::class -> returnType.arguments[0].type?.classifier as? KClass<*>
            else -> throw IllegalArgumentException(
                "Unexpected return type: $returnType, $name(...): Answer<$returnType>"
            )
        } ?: throw IllegalArgumentException("Unexpected return type: $returnType. Expecting Answer<SomeType>")

        // Handle Single and Multiple
        return ("** Required Format **: You must format your answer as follows:  " +
                (if (returnClass.isSubclassOf(Collection::class))
                    collectionFormatRules() else returnClass.formatRules())
                )
            .asSystemMessage()
    }

    /**
     * Determines the format rules for collections based on the method's return type.
     *
     * This function provides specific instructions for formatting collection types such as List or Set.
     * The response is in a CSV-like format with each item separated by a newline character.
     *
     * @receiver The MethodAPI instance containing the return type information.
     * @return A string containing the format rules for the collection.
     * @throws IllegalArgumentException If the expected generic type for the collection is not found.
     */
    fun collectionFormatRules(): String {
        val subClass = returnType.arguments.firstOrNull()?.type?.arguments?.firstOrNull()?.type?.jvmErasure
            ?: throw IllegalStateException("Expected a Answer type for Directive to be Collection type, received $this")
        return "\nYou must use CSV format. Do not number lines. Put every item separated by newline " +
                "character instead of comma. " +
                "For each item: " + subClass.formatRules()
    }
}

/**
 * Represents a directive for a robot, encapsulating method details and associated information.
 *
 * This data class describes a command or instruction to be executed by a robot. It includes the
 * method's name, its parameters, the expected return type, any relevant messages, and the associated feature.
 *
 * @property name The name of the method to be executed by the robot.
 * @property parameters A list of [Parameter] objects detailing the method's input parameters.
 * @property returnType The [KType] representing the expected return type of the method.
 * @property messages A list of [TextMessage] objects containing additional information or context relevant to the directive.
 * @property feature The [Feature] associated with this directive, providing context for its execution.
 *
 * @since 0.1.0-alpha

 */
@Serializable
data class Directive(
    override val name: String,
    override val parameters: List<Parameter>,
    @Serializable(with = KTypeSerializer::class)
    override val returnType: KType,
    override val messages: List<TextMessage>,
    override val feature: Feature,
) : AbstractDirective {
    override fun toString(): String = this.toJson()
}