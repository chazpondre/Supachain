package dev.supachain.mixer
/*
░░░░░░░░░░░░░  ░░░░  ░░        ░░  ░░░░  ░░░░░░░░        ░░  ░░░░  ░░        ░░░      ░░░       ░░░  ░░░░  ░░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒▒▒   ▒▒   ▒▒▒▒▒  ▒▒▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓▓▓        ▓▓▓▓▓  ▓▓▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓        ▓▓      ▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓
█████████████  █  █  █████  ██████  ██  ████████████  █████  ████  ██  ████████  ████  ██  ███  ██████  ████████████████
█████████████  ████  ██        ██  ████  ███████████  █████  ████  ██        ███      ███  ████  █████  ████████████████

 */

/**
 * Represents a space in which a value exists and can be transformed. The Sample
 * class is used to encapsulate a value within a specific context or "space," enabling
 * transformations and operations within that context.
 *
 * Note:
 * This class is decorative and does not introduce boxing. It gives the compiler
 * semantic understand so that type inference can work when paired with the mix and concept
 * functions
 *
 * @param T the type of the value contained within the `Sample`
 * @property value the actual value encapsulated within the `Ssmple`
 */
@JvmInline
value class Sample<T>(val value: T)

/**
 * A `Transformation` is a function that garauntees that its input in some space of T and outputs.
 * The input of Space<T> may not be needed in a future release of Kotlin if inference abilities
 * improve. In the meantime, this method allows for auto inferencing the f function.
 *
 * @param T the type of the value produced by the transformation
 */
typealias MixConcept<T> = Input<T>.() -> T

typealias MixConcepts<T> = InputAndResults<T>.() -> T
//List<SampleSpace<T>>.() -> T

/**
 * Encapsulates an input value within a Space. This class provides type safety and
 * ensures that transformations are applied within the correct context.
 */
@JvmInline
value class Input<T>(private val sample: Sample<T>){
    val inputted get() = sample.value
}

/**
 * Represents both an input value and a list of results from previous transformations.
 * This class is used when a series of transformations are applied, and the intermediate
 * results need to be tracked.
 */
@JvmInline
value class InputAndResults<T>(private val value: Pair<T, List<Sample<T>>>) {
    constructor(input: T, results: List<Sample<T>>) : this(Pair(input, results))
    val inputted: T get() = value.first
    val results: List<Sample<T>> get() = value.second
}

/**
 * A `Mixer` is responsible for combining multiple `Sample` inputs into a single output of type [T].
 * It applies a transformation function (`MixConcept`) and manages how many times the function should be
 * applied (`remixCount`).
 *
 * @param T the type of value being mixed
 * @property conceptualize the mixing function that combines inputs into a single output
 * @property remixCount the number of times the mixing operation should be applied
 */
@JvmInline
value class Mixer<T>(val data: Pair<MixConcepts<T>, Int>) {
    val conceptualize: MixConcepts<T> get() = data.first
    internal val remixCount: Int get() = data.second

    fun copyIncreased(): Mixer<T> = Mixer(Pair(conceptualize, remixCount + 1))
    fun copyTo(value: Int): Mixer<T> = Mixer(Pair(conceptualize, value))

    internal fun downMix(input: T, samples: List<Sample<T>>, producer: (T) -> T): List<Sample<T>> {
        val mainMix = producer(InputAndResults(input, samples).conceptualize())

        // Reapplications of mixer
        var subInput = listOf(Sample(mainMix))
        repeat(remixCount) { subInput = listOf(Sample(producer(InputAndResults(input, subInput).conceptualize()))) }

        return subInput
    }
}

/**
 * A SingleTrack represents a single transformation applied to a Sample. It
 * ensures that the transformation is type-safe and correctly aligned within the
 * context of the Mix.
 *
 * @param T the type of space the transform occurs
 * @property alignment the transformation function applied to get the new transformed sample
 */
@JvmInline
value class SingleTrack<T>(private val alignment: MixConcept<T>) : Mix<T> {
    override fun invoke(input: T, producer: (T) -> T): T =
        producer(alignment(Input(Sample(input))))
}

/**
 * A MultiTrack is a type of Mix that combines multiple Sample instances into one,
 * applying a series of transformations defined by a list of mixers.
 *
 * @param T the type of space the mix occurs
 * @property samples a list of `Sample` instances to be mixed
 * @property mixers a mutable list of `Mixer` instances that will be applied sequentially to the samples
 */
@JvmInline
value class MultiTrack<T>(val data: Triple<List<Mix<T>>, MutableList<Mixer<T>>, Int>) : Mix<T> {
    constructor(mixes: List<Mix<T>>, mixer: Mixer<T>, memLimit: Int = 10) : this(
        Triple(
            mixes,
            mutableListOf(mixer),
            memLimit
        )
    )

    val alignments: List<Mix<T>> get() = data.first
    val mixers: MutableList<Mixer<T>> get() = data.second
    val memoryLimit: Int get() = data.third

    internal fun expandLast() = mixers.apply {
        this[lastIndex] = last().copyIncreased()
    }

    internal fun add(mixer: Mixer<T>) = mixers.add(mixer)

    override fun invoke(input: T, producer: (T) -> T): T {
        val initialInput: List<Sample<T>> = alignments.map { Sample(it(input, producer)) }
        return mixers.fold(initialInput) { acc, mixer -> mixer.downMix(input, acc, producer) }.last().value
    }
}


/**
 * The Mix interface defines a contract for any type of transformation or mixing operation
 * within Mix Theory. Implementations of Mix must provide a way to apply the transformation
 * to an input value and produce an output.
 *
 * @param T the type of value contained within the `Sample`
 */
sealed interface Mix<T> {
    operator fun invoke(input: T, producer: (T) -> T): T

    /**
     * Sequentially mixes a `Sample` using a `Mix` function, allowing for the application of multiple `Mixers`.
     * If the same `Mix` function is applied consecutively, the count of applications is incremented,
     * ensuring the correct flow of data.
     *
     * @param S the type of the values within the samples
     * @param mix the mixing function to apply
     * @return a `SampleMix` representing the mixed result of the sample
     */
    infix fun to(mixer: Mixer<T>): Mix<T> = when (this) {
        is MultiTrack ->
            if (mixers.last().conceptualize == mixer.conceptualize) apply { expandLast() }
            else apply { add(mixer) }

        is SingleTrack -> MultiTrack(listOf(this), mixer)
    }
}

/**
 * Combines two `Mixtures` into an `Group`. This operation creates a disjoint list of independent mixes
 * that operate within the same type.
 *
 * @param S the type of the values within the samples
 * @param other the sample to combine with
 * @return an `Unmixed` collection containing both samples
 */
operator fun <T> Mix<T>.plus(other: Mix<T>): Group<T> = Group(this, other)

/**
 * Represents a collection of Mix instances that are considered disjoint but operate
 * within the same type space. A Group allows you to combine multiple independent
 * mixes into one, maintaining the flow of data.
 *
 * @param T the type of the values within the samples
 * @property list a list of grouped samples
 */
@JvmInline
value class Group<T>(val list: List<Mix<T>>) {
    constructor(vararg mixes: Mix<T>) : this(listOf(*mixes))

    operator fun plus(other: Mix<T>): Group<T> = Group(* list.toTypedArray(), other)

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
    infix fun to(mix: Mixer<T>): Mix<T> = MultiTrack(this.list, mix)
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
fun <T> concept(fn: MixConcept<T>) = SingleTrack(fn)

/**
 * Creates a `Transform` object from a given transformation function.
 * This encapsulates a single transformation in the flow of data within a `Space`.
 *
 * @param S the type of value the transformation produces
 * @param fn the transformation function to be applied to a `Space`
 * @return a `Transform` object encapsulating the transformation function
 */
fun <S> mix(fn: MixConcepts<S>) = Mixer(Pair(fn, 0))


infix fun <T> Mix<T>.using(producer: (T) -> T) =
    Producer(this, producer)

/**
 * The Producer class is used to execute a Mix operation with a specific producer function.
 */
@JvmInline
value class Producer<T>(val data: Pair<Mix<T>, (T) -> T>) {
    constructor(mix: Mix<T>, producer: (T) -> T) : this(Pair(mix, producer))

    val mixture get() = data.first
    val producer get() = data.second

    fun produce(input: T) = mixture(input, producer)
}

