package dev.supachain.robot.tool.stategies

import dev.supachain.robot.director.Director
import dev.supachain.robot.messenger.messaging.Message
import dev.supachain.robot.tool.ToolConfig
import dev.supachain.robot.tool.ToolMap

/**
 * Represents a strategy for using tools in AI interactions.
 *
 * This sealed interface defines the contract for different approaches to utilizing tools or functions
 * within the AI system. Concrete implementations of this interface will dictate how the AI decides
 * when and how to call external tools or functions to enhance its responses or capabilities.
 *
 * @since 0.1.0-alpha

 */
sealed interface ToolUseStrategy {
    fun message(director: Director<*, *, *>): Message?
    fun getTools(toolMap: ToolMap): List<ToolConfig>
}
