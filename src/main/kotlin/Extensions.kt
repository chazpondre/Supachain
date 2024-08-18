package dev.supachain
/**
 * An interface providing a self-referential type.
 * Gets rid of bad parents. Parents should know their children.
 *
 * This interface defines a property `self` which returns an instance of the implementing type.
 * It allows for parents to know of some self, which allows for a tighter integration with higher child classes
 *
 * It serves two primary purposes:
 *
 *  1. **Fluent API Design:** By providing a way to reference the current instance within its own methods,
 *  it can allow a fluent or chainable API style. This is particularly useful when building APIs where multiple
 *  operations can be performed on the same object in sequence.
 *
 *  2. **Higher Self Pattern:** In inheritance hierarchies, it allows a parent class (or interface) to reference
 *  the specific type of its child class through the `Self` type parameter. This enables the parent class to
 *  call methods on the child class that are not defined in the parent, providing a bilateral delegate for
 *  certain responsibilities.
 *
 * @param Self The type of the implementing class or subtype of higher self.
 * @since 0.1.0-alpha
 *
 * @see Modifies
 */
interface Extension<Self> {
    /**
     * A function that normally returns a reference to the current instance of the implementing class or subtype.
     *
     * This property is essential for both fluent APIs or a way to give the parent a "higher self".
     */
    val self: () -> Self
}

/**
 * An interface enabling object modification and fluent builder pattern capabilities,
 * leveraging the "higher self" type.
 *
 * This interface extends `Extension<Self>` and introduces an `invoke` operator function that accepts a lambda function
 * to modify the object. By utilizing the `self` property from `Extension<Self>`, this interface provides a mechanism
 * for parent classes to access and modify child-specific properties or methods in a type-safe manner.
 *
 * Key Uses:
 *
 * - **Builder Pattern:**  The `invoke` operator enables a fluent builder pattern, allowing you to create and configure
 * objects in a concise and readable way.
 *
 * - **Higher Self Pattern:** In inheritance hierarchies, a parent class (or interface) can leverage the `Self` type parameter
 * to reference a specific child type. This allows the parent to call methods defined in the child class, providing greater flexibility
 * and type safety.
 *
 * @param Self The type of the implementing class or a subtype (in the higher self pattern).
 *
 * @sample
 * ```kotlin
 * // Example of builder pattern usage
 * class Example : Modifies<Example> {
 *     override val self = { this }
 *     var value: Int = 0
 * }
 *
 * val example = Example{ value = 42 }
 * println(example.value) // Prints: 42
 * ```
 *
 * @since 0.1.0-alpha
 * @see Extension
 */
interface Modifies<Self> : Extension<Self> {
    /**
     * Operator function to apply a modification lambda to the object.
     *
     * This function enables the builder pattern and allows customization of the object's properties or state.
     *
     * @param build A lambda function with receiver type `Self` that performs the modification.
     * @return The modified instance of `Self`.
     */
    operator fun invoke(build: Self.() -> Unit) = self().apply(build)
}

/**
 * Abstract class enabling self-referential typing and object modification through the higher-self pattern.
 *
 * This class implements the `Modifies<Self>` interface, providing a foundation for fluent APIs and
 * the builder pattern. It introduces the `self` property, which returns an instance of the implementing type
 * (or a subtype), facilitating method chaining and allowing parent classes to access child-specific methods.
 *
 * @param T The type of the implementing class (or a subtype in the "higher-self" pattern).
 * @param self A lambda function that returns the current instance (`this`) or a new instance of the class.
 *             This allows flexibility in how the self-reference is obtained.
 *
 * @see Modifies
 *
 * @since 0.1.0-alpha
 */
abstract class Modifiable<T>(override val self: () -> T) : Modifies<T>

operator fun <T : Any> T.plus(list: List<T>): List<T> = list.toMutableList().also { it.addFirst(this) }