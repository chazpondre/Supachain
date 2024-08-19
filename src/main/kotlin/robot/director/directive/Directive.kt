package dev.supachain.robot.director.directive

import dev.supachain.robot.answer.Answer
import dev.supachain.robot.answer.formatRules
import dev.supachain.robot.messenger.asSystemMessage
import dev.supachain.robot.messenger.asUserMessage
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.provider.Feature
import dev.supachain.utilities.KTypeSerializer
import dev.supachain.utilities.Parameter
import dev.supachain.utilities.toJson
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * Represents a directive for a robot, encapsulating method details and associated information.
 *
 * This data class describes a command or instruction to be executed by a robot. It includes the
 * method's name, its parameters, the expected return type, any relevant messages, and the associated feature.
 *
 * @property name The name of the method to be executed by the robot.
 * @property parameters A list of [Parameter] objects detailing the method's input parameters.
 * @property returnType The [KType] representing the expected return type of the method.
 * @property messages A list of [Message] objects containing additional information or context relevant to the directive.
 * @property feature The [Feature] associated with this directive, providing context for its execution.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
@Serializable
data class Directive(
    val name: String,
    val parameters: List<Parameter>,
    @Serializable(with = KTypeSerializer::class)
    val returnType: KType,
    val messages: List<Message>,
    val feature: Feature,
) {
    /**
     * Configures a list of chat messages for a conversational AI interaction.
     *
     * This function takes existing messages associated with a `RobotDirective` and
     * integrates them with additional messages derived from function arguments.  The key steps are:
     *
     * 1. **Sorting:** Orders the existing messages based on their `Role` (e.g., USER, ASSISTANT, SYSTEM)
     *    to ensure a logical flow in the conversation.
     * 2. **Argument Integration:** Extracts messages from the function arguments using `getArgumentMessages`.
     *    These messages typically contain information about the parameters of a function being called
     *    by the Robot to use a tool.
     * 3. **Adding to List:** The extracted argument messages are added to the end of the sorted list,
     *    creating a comprehensive set of messages ready for the Robot interaction.
     *
     * @param directiveArguments The arguments passed to the function that this directive is associated with.
     * @param messenger The `Messenger` instance responsible for managing the chat conversation. This parameter is currently unused.
     * @return A mutable list of messages, combining the original messages with the extracted argument messages.
     *
     * @since 0.1.0-alpha
     * @author Che Andre
     */
    fun getDirectiveMessages(directiveArguments: Array<Any?>): MutableList<Message> {
        return mutableListOf(formattingMessage()).apply {
            addAll(messages.sortedBy { it.role.ordinal })
            addAll(getArgumentMessages(directiveArguments))
        }
    }

    /**
     * Converts method arguments of a method into a list of messages.
     *
     * @param directiveArguments The arguments passed to the method.
     * @return A list of messages representing the arguments.
     */
    fun getArgumentMessages(directiveArguments: Array<Any?>): List<Message> =
        List(parameters.size) { index -> directiveArguments[index] }.mapIndexed { index, arg ->
            val givenName = parameters[index].description
            val localName = parameters[index].name
            val name = givenName.ifBlank { localName }

            "Your task is to answer question in $name. $name=`$arg`".asUserMessage()
        }

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
     * @author Che Andre
     */
    private fun formattingMessage(): Message {
        // Extract the KClass representing the return type (handling Deferred)
        val returnClass = when (returnType.classifier) {
            Answer::class -> returnType.arguments[0].type?.classifier as? KClass<*>
            else -> throw IllegalArgumentException(
                "Unexpected return type: $returnType, $name(...): Answer<$returnType>"
            )
        } ?: throw IllegalArgumentException("Unexpected return type: $returnType. Expecting Answer<SomeType>")

        // Handle Single and Multiple
        return ("[Required Format] You must format your answer as follows: \n\n# Format:  " +
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
    private fun collectionFormatRules(): String {
        val subClass = returnType.arguments.firstOrNull()?.type?.arguments?.firstOrNull()?.type?.jvmErasure
            ?: throw IllegalStateException("Expected a Answer type for Directive to be Collection type, received $this")
        return "\nYou must use CSV format. Do not number lines. Put every item separated by newline " +
                "character instead of comma. " +
                "For each item: " + subClass.formatRules()
    }

    override fun toString(): String = this.toJson()
}