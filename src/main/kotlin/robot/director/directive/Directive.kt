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

/**
 * Represents an objective for the robot, combining a directive and its arguments.
 *
 * The `Objective` class encapsulates a [Directive] and the corresponding method arguments as a pair.
 * It provides utility functions to transform the method arguments into a list of messages and to
 * retrieve relevant information about the directive being executed.
 *
 * @property data A pair consisting of the [Directive] and the arguments used to execute it.
 * @property directive The directive that is being executed.
 * @property arguments The arguments passed to the method corresponding to the directive.
 *
 * @constructor Creates an objective from a [Directive] and an array of arguments.
 *
 * @since 0.1.0
 */
class Objective(val data: Pair<Directive, Array<Any?>>) : AbstractDirective by data.first {
    constructor(directive: Directive, arguments: Array<Any?>) : this(Pair(directive, arguments))

    private val directive: Directive get() = data.first
    val arguments: Array<Any?> get() = data.second


    /**
     * Converts method arguments into a list of user messages for the robot.
     *
     * This function takes the provided arguments and transforms them into [TextMessage] objects.
     * These messages represent the arguments as instructions for the robot to execute the directive.
     * If `usePrimer` is true, the message will provide context for the robot's task, otherwise it simply
     * displays the argument values.
     *
     * @param usePrimer Whether to include a primer or task description in the message.
     * @return A list of [TextMessage] objects representing the arguments.
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

/**
 * Represents an abstraction for directives.
 *
 * This interface defines the structure of a directive, which is a command or task for the robot.
 * It provides metadata about the directive, such as its name, the parameters it accepts, its expected return type,
 * and associated messages. The `AbstractDirective` interface is implemented by classes that represent
 * specific directives for the robot.
 *
 * @property name The name of the directive.
 * @property parameters A list of [Parameter] objects representing the input parameters for the directive.
 * @property returnType The [KType] indicating the return type of the directive.
 * @property messages A list of [TextMessage] objects that provide context or information related to the directive.
 * @property feature The [Feature] associated with this directive.
 *
 * @since 0.1.0
 */
interface AbstractDirective {
    val name: String
    val parameters: List<Parameter>

    @Serializable(with = KTypeSerializer::class)
    val returnType: KType
    val messages: List<TextMessage>
    val feature: Feature
    val rankedConfigMessages get() = messages.sortedBy { it.role.ordinal }


    /**
     * Generates a message describing the expected output format for the directive's return type.
     *
     * This function analyzes the return type of the directive and generates instructions on how the
     * robot should format its response. It supports various types including collections, enums,
     * and basic types.
     *
     * @return A [TextMessage] containing the expected format for the directive's output.
     * @throws IllegalArgumentException If the return type is unsupported or missing.
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
     * Determines the formatting rules for collection types such as List or Set.
     *
     * This method provides specific instructions for how the robot should format responses when the
     * return type is a collection. The output is formatted similarly to a CSV, where each item is
     * separated by a newline.
     *
     * @return A string representing the format rules for collections.
     * @throws IllegalStateException If the return type is not properly defined for collections.
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
 * Represents a directive containing method details and related information for execution.
 *
 * The `Directive` class encapsulates the essential components needed to describe and execute a method.
 * It includes the method's name, the parameters it accepts, its return type, any associated messages,
 * and the feature within which it operates. The directive provides context for the robot's decision-making
 * and action-taking.
 *
 * @property name The name of the method represented by this directive.
 * @property parameters A list of [Parameter] objects representing the method's input parameters.
 * @property returnType The [KType] representing the expected return type of the method.
 * @property messages A list of [TextMessage] objects containing additional information or context about the directive.
 * @property feature The [Feature] associated with this directive, providing context for its execution.
 *
 * @constructor Creates a `Directive` instance with the specified name, parameters, return type, messages, and feature.
 *
 * @since 0.1.0
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