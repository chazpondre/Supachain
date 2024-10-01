package dev.supachain.robot.tool.strategies

import dev.supachain.robot.provider.models.CommonMessage
import dev.supachain.robot.tool.ToolConfig

/**
 * Represents a strategy for using tools in AI interactions.
 *
 * This sealed interface defines the contract for different approaches to utilizing tools or functions
 * within the AI system. Concrete implementations of this interface will dictate how the AI decides
 * when and how to call external tools or functions to enhance its responses or capabilities.
 *
 * @since 0.1.0

 */
sealed interface ToolUseStrategy {
    fun onRequestMessage(toolSet: List<ToolConfig>): CommonMessage?
    fun getTools(tools: List<ToolConfig>): List<ToolConfig>
}
