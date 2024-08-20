package dev.supachain.robot.provider

/**
 * Abstract base class for model providers.
 *
 * This class provides a foundation for implementing different providers,
 * each with its own capabilities and configurations. It manages features,
 * handles requests, and encapsulates error handling.
 *
 * @param T The specific type of the `Provider` subclass, enabling fluent configuration using the `invoke` operator.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
abstract class Provider<T : Provider<T>>