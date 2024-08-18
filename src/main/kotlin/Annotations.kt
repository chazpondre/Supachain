/*
░░░░░░░      ░░░   ░░░  ░░   ░░░  ░░░      ░░░        ░░░      ░░░        ░░        ░░░      ░░░   ░░░  ░░░      ░░░░░░░
▒▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓  ▓  ▓  ▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓  ▓▓  ▓  ▓  ▓▓▓      ▓▓▓▓▓▓▓
██████        ██  ██    ██  ██    ██  ████  █████  █████        █████  ████████  █████  ████  ██  ██    ████████  ██████
██████  ████  ██  ███   ██  ███   ███      ██████  █████  ████  █████  █████        ███      ███  ███   ███      ███████
 */
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
