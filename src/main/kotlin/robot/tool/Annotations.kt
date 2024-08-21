package dev.supachain.robot.tool

/**
 * Annotation to describe a tool method.
 *
 * @property description A brief description of the tool method.
 * @since 0.1.0-alpha

 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Tool(val description: String = "")

/**
 * Annotation to mark a class as a collection of tool methods.
 *
 * This annotation designates a class as a container for multiple tool functions, often used in conjunction with
 * the `@Tool` annotation on individual methods within the class.
 *
 * @since 0.1.0-alpha

 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ToolSet

/**
 * Annotation to specify parameter names for a method.
 *
 * @property description An array of parameter names.
 * @since 0.1.0-alpha

 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Parameters(val description: Array<String>)