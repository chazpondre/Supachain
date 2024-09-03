/*
░░░░░░░░░░░░░  ░░░░  ░░        ░░  ░░░░  ░░░░░░░░        ░░  ░░░░  ░░        ░░░      ░░░       ░░░  ░░░░  ░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒▒▒▒  ▒▒▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓▓▓▓  ▓▓▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓        ▓▓      ▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████  █  █  █████  ██████  ██  ████████████  █████  ████  ██  ████████  ████  ██  ███  ██████  ████████████████
█████████████  ████  ██        ██  ████  ███████████  █████  ████  ██        ███      ███  ████  █████  ████████████████

 Mix Theory (based on GraphIQ Paper)
 Rules:
 0. It is a theory of mixing functions
 1. All values exists in space and time
 2. Values can be sampled with samplers.
 3. When a value is sampled matters, because a value changes.
 4. Changes to values occur at some event in time
 5. Every value exists in some type of space
 6. Values in a certain spaces can be grouped
 7. Grouped spaces can be mixed into one space
 8. Samples can be timed, this timing is known as SampleTime.
 9. Sample Times are organized in the TimeGraph
 10. We can run disjoint samplers together.
 11. Changing type spaces for functions are possible with cross-functions
 12. Sampling can be limited with limiters

The following is an implementation of a small subset of Mix Theory
 */

/**
 * A Space stores relationship of a type of space and the value.
 * This class is decorative and does not introduce boxing. It gives the compiler
 * semantic understand so that type inference can work when paired with the f
 * function
 *
 * @param T the type of the value contained within the `Space`
 * @property value the actual value encapsulated within the `Space`
 */
@JvmInline
value class Space<T>(val value: T)

/**
 * A `Transformation` is a function that garauntees that its input in some space of T and outputs.
 * The input of Space<T> may not be needed in a future release of Kotlin if inference abilities
 * improve. In the meantime, this method allows for auto inferencing the f function.
 *
 * @param T the type of the value produced by the transformation
 */
typealias Transformation<T> = (Space<T>) -> T

typealias Mix<T> = (List<Space<T>>) -> T

/**
 * A `Mixer` is responsible for combining multiple `Space` inputs into a single output of type [T].
 * The `Mixer` can be applied multiple times to manage the flow and aggregation of data within a single type space.
 *
 * @param T the type of value being mixed
 * @property mix the mixing function that combines inputs into a single output
 * @property count the number of times the mixing operation should be applied
 */
@JvmInline
value class Mixer<T>(val data: Pair<Mix<T>, Int>) {
    val mix: (List<Space<T>>) -> T get() = data.first
    val count: Int get() = data.second

    fun copyIncreased(): Mixer<T> = Mixer(Pair(mix, count + 1))
    fun copyTo(value: Int): Mixer<T> = Mixer(Pair(mix, value))
}

/**
 * All `SampleEvent` operate within the same type space to maintain consistency in data flow.
 *
 * @param T the type of value contained within the `Sample`
 */
sealed interface Sampler<T>

/**
 * A `TransformEvent` is a type of `Sample` occurs when an output value from a transformation has been produce
 *
 * @param T the type of space the transform occurs
 * @property sampler the transformation function applied to get the new transformed sample
 */
@JvmInline
value class TransformEvent<T>(val sampler: Transformation<T>) : Sampler<T>

/**
 * A `MixEvent` is a type of `Sample` that occurs when multiple `Sample` are ready to be mixed into one `Sample`.
 *
 * @param T the type of space the mix occurs
 * @property samples a list of `Sample` instances to be mixed
 * @property mixers a mutable list of `Mixer` instances that will be applied sequentially to the samples
 */
@JvmInline
value class MixEvent<T>(val data: Pair<List<Sampler<T>>, MutableList<Mixer<T>>>) : Sampler<T> {
    constructor(samplers: List<Sampler<T>>, mixer: Mixer<T>) : this(Pair(samplers, mutableListOf(mixer)))

    val samplers: List<Sampler<T>> get() = data.first
    val mixers: MutableList<Mixer<T>> get() = data.second

    internal fun expandLast() = mixers.apply {
        this[lastIndex] = last().copyIncreased()
    }

    internal fun add(mixer: Mixer<T>) = mixers.add(mixer)
}

/**
 * Represents a collection of `Samples`.
 * These `Samples` are considered disjoint, meaning they are independent of each other but
 * still operate within the same type space.
 *
 * @param T the type of the values within the samples
 * @property list a list of grouped samples
 */
@JvmInline
value class Group<T>(val list: List<Sampler<T>>) {
    constructor(vararg samplers: Sampler<T>) : this(listOf(*samplers))
}

// DSL
// Grouping
/**
 * Combines two `Samples` into an `Group`. This operation creates a disjoint list of independent samplers
 * that operate within the same type.
 *
 * @param S the type of the values within the samples
 * @param other the sample to combine with
 * @return an `Unmixed` collection containing both samples
 */
operator fun <S> Sampler<S>.plus(other: Sampler<S>): Group<S> = Group(this, other)

// Mixing
/**
 * States intent to mix a `Group` of `Sampler's` using a `Mix` function.
 * This operation aggregates the outputs of the independent `Sampler` into a single output,
 * maintaining the flow of data from multiple inputs to a unified output.
 *
 * @param S the type of the values within the samples
 * @param mix the mixing function to apply
 * @return a `SampleMix` representing the mixed result of the samples
 */
infix fun <S> Group<S>.to(mix: Mixer<S>): Sampler<S> = MixEvent(this.list, mix)

/**
 * Sequentially mixes a `Sample` using a `Mix` function, allowing for the application of multiple `Mixers`.
 * If the same `Mix` function is applied consecutively, the count of applications is incremented,
 * ensuring the correct flow of data.
 *
 * @param S the type of the values within the samples
 * @param mix the mixing function to apply
 * @return a `SampleMix` representing the mixed result of the sample
 */
infix fun <S> Sampler<S>.to(mixer: Mixer<S>): Sampler<S> = when (this) {
    is MixEvent -> if (mixers.last().mix == mixer.mix) apply { expandLast() } else apply { add(mixer) }
    is TransformEvent -> MixEvent(listOf(this), mixer)
}

/**
 * Creates a `Mixer` by multiplying a `Mix` function with an integer, defining how many times the mix operation
 * should be applied.
 * This ensures that the flow of inputs is managed through repeated application of the mix.
 *
 * @param T the type of value being mixed
 * @param multiplier the number of times the `Mix` function should be applied
 * @return a `Mixer` initialized with the `Mix` function and the specified count
 */
operator fun <T> Mixer<T>.times(multiplier: Int) = copyTo(multiplier)

/**
 * Creates a `TransformEvent` object from a given transformation function.
 * This encapsulates a single transformation in the flow of data within a `Space`.
 *
 * @param T the type of value the transformation produces
 * @param fn the transformation function to be applied to a `Space`
 * @return a `Transform` object encapsulating the transformation function
 */
fun <T> f(fn: Transformation<T>) = TransformEvent(fn)

/**
 * Creates a `Transform` object from a given transformation function.
 * This encapsulates a single transformation in the flow of data within a `Space`.
 *
 * @param S the type of value the transformation produces
 * @param fn the transformation function to be applied to a `Space`
 * @return a `Transform` object encapsulating the transformation function
 */
fun <S> mix(fn: Mix<S>) = Mixer(Pair(fn, 1))


/// Example
fun main() {
    val summarizer = exampleSummarizer()
    val enhancer = exampleEnhancer()
    val enhancer2 = exampleManualEnhancer()
    val proCon = exampleProsVsCon()
}

private fun exampleSummarizer(): Sampler<String> {

    val philosophical = f { "Show the philosophical answer for question: $it" }

    val scientific = f { "Show the scientific answer for question: $it" }

    val summarize = mix { "Summarize the answers into one coherent answer. Answers: $it" }

    return philosophical + scientific to summarize
}

private fun exampleProsVsCon(): Sampler<String> {

    val pros = f { "Show me all the pros for this issue: $it" }

    val cons = f { "Show me all the cons for this issue: $it" }

    val summary = mix { "Show the pros and cons and weigh out the answers. Pros/Cons: $it" }

    return pros + cons to summary
}

private fun exampleEnhancer(): Sampler<String> {

    val scientific = f { "Show the scientific answer for question: $it" }

    val answer = f { "Give an answer to the following question: $it" }

    val betterAnswer = mix {
        "The last answer was ok, can you give a better answer than this: ${it.last()}"
    }

    return answer to betterAnswer * 4
}

private fun exampleManualEnhancer(): Sampler<String> {

    val scientific = f { "Show the scientific answer for question: $it" }

    val answer = f { "Give an answer to the following question: $it" }

    val betterAnswer = mix {
        "The last answer was ok, can you give a better answer than this: ${it.last()}"
    }

    return answer to betterAnswer to betterAnswer to betterAnswer to betterAnswer
}

