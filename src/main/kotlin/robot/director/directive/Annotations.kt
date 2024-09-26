package dev.supachain.robot.director.directive

import dev.supachain.robot.provider.Feature

/**
 * Annotation to provide a user message template for a directive function.
 *
 * When a directive function is called, the message specified in this annotation will be formatted
 * using the function arguments and added as a USER message to the conversation history.
 *
 * @property message The template for the user message.
 * @since 0.1.0

 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromUser(val message: String)

/**
 * Specifies the Provider feature to use for a directive function.
 *
 * This annotation indicates which capability of the Provider should be utilized when processing
 * this directive. For example, `Feature.Chat` might be used for conversational interactions,
 * while `Feature.CreateImage` could trigger image generation based on the function's input.
 *
 * @property feature The feature to be used for this function.
 * @since 0.1.0

 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Use(val feature: Feature)

/**
 * Annotation to provide a system message for a directive function or type.
 *
 * This annotation defines a message that will be added as a SYSTEM message to the conversation history
 * when the annotated function is called or the annotated type is used. System messages typically provide
 * instructions or context to guide the AI's behavior.
 *
 * @property message The content of the system message.
 * @since 0.1.0

 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class FromSystem(val message: String)