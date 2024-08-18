/*
░░░░░░░      ░░░   ░░░  ░░   ░░░  ░░░      ░░░        ░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░
▒▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓  ▓  ▓  ▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓
██████        ██  ██    ██  ██    ██  ████  █████  █████        █████  ████████  █████  ████  ██  ██    ████████  ██████
██████  ████  ██  ███   ██  ███   ███      ██████  █████  ████  █████  █████        ███      ███  ███   ███      ███████
 */
@file:Suppress("unused")
package dev.supachain
/**
 * Indicates that an API is experimental and might change in future releases.
 *
 * Using an experimental API requires an explicit opt-in using the `@OptIn` annotation.
 *
 * @since 0.1.0-alpha
 */
@RequiresOptIn(message = "This API is experimental and might change in future releases.")
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalAPI

/**
 * Marks an annotation for compile-time processing only. Annotations with this retention policy
 * are not included in the class files or accessible at runtime.
 *
 * @since 0.1.0-alpha
 */
@Retention(AnnotationRetention.SOURCE)
annotation class CompileTime

