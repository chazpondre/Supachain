package dev.supachain.mixer

import dev.supachain.robot.answer.Answer

infix fun <T> Mixable<T>.usingInParallel(fn: (T) -> Answer<T>): Mixer<T> {
    val generator = object : Generator<T> {
        override val parallelExecutionAllowed: Boolean = true
        override fun generate(input: T): T = fn(input).await()
    }

    return this with generator
}

infix fun <T> Mixable<T>.using(fn: (T) -> Answer<T>): Mixer<T> {
    val generator = object : Generator<T> {
        override val parallelExecutionAllowed: Boolean = false
        override fun generate(input: T): T = fn(input).await()
    }

    return this with generator
}

