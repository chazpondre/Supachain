package utilities

/**
 * Generates example responses to reinforce a numeric range for AI models.
 *
 * This function creates a specified number of examples within the given range, applying
 * a custom output function to each example. The resulting strings are joined into a single
 * string that can be used in system messages to guide AI behavior.
 *
 * @param T The type of numeric value (e.g., Int, Float, Double, etc.).
 * @param Result The type of output produced by the `output` function (e.g., String, Message).
 * @param generate A function that generates a random value of type `T`.
 * @param min The minimum value (inclusive) of the desired range.
 * @param max The maximum value (inclusive) of the desired range.
 * @param numExamples The number of examples to generate (default: 4).
 * @param output A function that takes a numeric value and returns a formatted output of type `Result`.
 *
 * @return A string containing the range limits and the generated examples with custom formatting.
 *
 * @throws IllegalArgumentException If `numExamples` is less than or equal to 0.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
fun <T : Number, Result> reinforceRange(
    generate: () -> T,
    min: T,
    max: T,
    numExamples: Int = 4,
    output: (T) -> Result
): String {
    require(numExamples > 0) { "Number of examples must be positive." }

    val examples = mutableSetOf<T>()
    while (examples.size < numExamples) {
        examples.add(generate())
    }

    return "The answer must be >= $min, The answer must be <= $max. \n" +
            examples.joinToString("\n") { output(it).toString() }
}

/**
 * Generates example responses to reinforce binary operations on the same type for AI models.
 *
 * This inline function creates a specified number of example pairs using the provided generator function.
 * It then applies a custom output function to each pair, generating formatted strings that can be used
 * to instruct the AI model on how to handle similar binary operations.
 *
 * @param T The type of value used for binary operations (e.g., Int, Float, Double).
 * @param Result The type of output produced by the `output` function (e.g., String, Message).
 * @param generator A function that generates a single value of type `T`.
 * @param numExamples The number of example pairs to generate (default: 4).
 * @param output A function that takes two values of type `T` and returns a formatted output of type `Result`.
 *
 * @return A string containing the generated examples with custom formatting.
 *
 * @throws IllegalArgumentException If `numExamples` is less than or equal to 0.
 *
 * @since 0.1.0-alpha
 * @author Che Andre
 */
inline fun <T, Result> reinforceBinarySameType(generator: () -> T, numExamples: Int = 4, crossinline output: (T, T) -> Result): String {
    require(numExamples > 0) { "Number of examples must be positive." }

    val examples = (1..numExamples).map { listOf(generator(), generator()) }

    return examples
        .joinToString("\n") { (left, right) -> output(left, right).toString() }
}