package dev.supachain.utilities

import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.javaMethod

/**
 * Creates a dynamic proxy instance for a Kotlin interface, delegating method calls to a custom handler.
 *
 * This function creates a dynamic proxy that intercepts method calls to an interface
 * and delegates them to the provided handler function. The handler receives the
 * qualified name of the interface's fully qualified name, the called method's name, the method's arguments,
 * and the expected return type of the method.
 *.
 *
 * @param T The interface type for which to create a proxy.
 * @param handler A lambda function handling method calls. It receives:
 *   - `parent`: The fully qualified name of the interface.
 *   - `method`: The name of the method being called.
 *   - `args`: An array of arguments passed to the method.
 *   - `returnType`: The `KType` representing the expected return type of the method.
 * @return A proxy instance of type `T` that delegates method calls to the `handler`.
 *
 * @throws IllegalStateException If a called method is not found in the interface definition.
 *
 * @since 0.1.0-alpha
 *
 * @sample
 * ```kotlin
 * interface Calculator {
 *     fun add(a: Int, b: Int): Int
 *     fun multiply(a: Int, b: Int): Int
 * }
 *
 * val proxy = Calculator::class by { parent, method, args, returnType ->
 *     println("Called '$method' in '$parent' with args: ${args.joinToString()}, expected type: $returnType")
 *     when (method) {
 *         "add" -> (args[0] as Int) + (args[1] as Int)  // Type-safe operations
 *         "multiply" -> (args[0] as Int) * (args[1] as Int)
 *         else -> throw IllegalStateException("Unknown method: $method")
 *     }
 * }
 *
 * println(proxy.add(5, 3))        // Output: 8
 * println(proxy.multiply(5, 3))  // Output: 15
 * ```
 */
infix
fun <T : Any> KClass<T>.by(handler: (parent: String, method: String, args: Array<Any?>, returnType: KType) -> Any?): T {
    val proxyInstance = Proxy.newProxyInstance(
        java.classLoader,
        arrayOf(java)
    ) { p, method, args ->

        // Handle methods of the Object class to avoid IllegalStateException
        when (method.name) {
            "toString" -> this@by.qualifiedName.toString()
            "hashCode" -> this@by.hashCode()
            "equals" -> this@by == args[0]
        }

        // Get the KFunction corresponding to the Java method
        val kFunction = this@by.declaredMemberFunctions.find { it.javaMethod == method }
            ?: throw IllegalStateException("Method ${method.name} not found in ${this@by.qualifiedName}")

        handler(this.qualifiedName!!, method.name, args ?: emptyArray(), kFunction.returnType)
    }

    return java.cast(proxyInstance)
}

/**
 * Creates an instance of the class represented by the reified type parameter [T].
 *
 * This function assumes that the class [T] has a no-argument constructor.
 *
 * @return An instance of the class [T].
 * @throws IllegalArgumentException If the class does not have a no-argument constructor.
 * @throws RuntimeException If there's an error during instance creation.
 */
internal inline fun <reified T : Any> createObjectFromNoArgClass(): T {
    val kClass = T::class
    return try {
        // Attempt to create an instance using the no-arg constructor
        kClass.createInstance()
    } catch (e: Exception) {
        throw RuntimeException("${kClass.simpleName} is not a class. " +
                "Error creating instance of ${kClass.simpleName}: ${e.message}", e)
    }
}