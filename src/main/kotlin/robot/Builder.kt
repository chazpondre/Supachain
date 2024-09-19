@file:Suppress("unused")

package dev.supachain.robot

import dev.supachain.CompileTime
import dev.supachain.robot.provider.Provider
import dev.supachain.robot.director.RobotCore

object Robot {
    /**
     * Creates and configures a `Director` instance for interacting with a model.
     *
     * This inline operator function streamlines the process of setting up a `Director` object by:
     * 1. **Creating the Provider:** Instantiates a `RobotProvider` using its default constructor, handling potential errors.
     * 2. **Initializing the Director:** Creates a `Director` instance with the specified `RobotProvider`, `Interface`, and `Tools`.
     * 3. **Applying Modifications:** Applies the `modify` lambda function to the `Director`, allowing for custom configuration.
     * 4. **Setting up Tools and Directives:** Calls `setToolset` and `setUpDirectives` to register the provided tools and map the methods of the `Interface` to corresponding directives.
     *
     * @param Interface The interface defining the available interactions with the [Robot].
     * @param Tools The class containing the tools that the [Robot] can utilize.
     * @param modify A lambda function to customize the configuration of the `Director`.
     * @return An instance of the specified `Interface` type, acting as a proxy for interacting with the [Robot].
     *
     * @since 0.1.0-alpha

     */
    @JvmStatic
    @CompileTime
    inline operator
    fun <reified RobotProvider : Provider<*, RobotProvider>, reified Interface : Any, reified Tools : Any>
            invoke(modify: RobotCore<RobotProvider, Interface, Tools>.() -> Unit = {}): Interface {
        val provider = try {
            RobotProvider::class.java.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Error creating instance of ${RobotProvider::class.simpleName}: ${e.message}", e)
        }
        return RobotCore<RobotProvider, Interface, Tools>(provider)
            .apply(modify)
            .setUpToolset<Tools>()
            .setUpDirectives<Interface>()
    }
}