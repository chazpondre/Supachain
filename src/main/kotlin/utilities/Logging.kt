@file:Suppress("unused")

package dev.supachain.utilities

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * Marker used to explicitly mark log messages as visible, regardless of the current log level configuration.
 */
val visibilityMarker: Marker = MarkerFactory.getMarker("VISIBLE")
/**
 * Marker used to explicitly mark log messages as invisible, even if the current log level would normally allow them to be logged.
 */
val invisibilityMarker: Marker = MarkerFactory.getMarker("INVISIBLE")

/**
 * Utility object to control the visibility of log messages based on tags.
 *
 * This object provides a simple way to filter log messages by associating them with specific tags.
 * By default, all messages are visible (`displayAll = true`). You can selectively show messages
 * with certain tags using the `show` function, or hide all messages except those with specific tags.
 *
 * @property displayAll Flag indicating whether all messages should be visible (default: true).
 * @property displayTags A set of tags representing the messages that should be visible when `displayAll` is false.
 */
object Debug {
    private var displayAll = true
    private val displayTags = mutableSetOf<String>()

    /**
     * Configures the visibility of log messages based on a tag.
     *
     * If the tag is "all", all messages will be visible.
     * Otherwise, only messages with the specified tag (or any tags previously added) will be visible.
     *
     * @param tag The tag to control visibility for.
     */
    infix fun show(tag: String) {
        if (tag == "all") displayAll = true
        else {
            displayTags.add(tag.replaceFirstChar { it.uppercaseChar() })
            displayAll = false
        }
    }

    /**
     * Returns the appropriate marker for a log message based on its tag.
     *
     * If the tag is in the `displayTags` set or `displayAll` is true, the `visibilityMarker` is returned,
     * making the log message visible. Otherwise, the `invisibilityMarker` is returned, hiding the message.
     *
     * @param tag The tag associated with the log message.
     * @return The `Marker` to be attached to the log message, controlling its visibility.
     */
    operator fun invoke(tag: String) =
        if (tag in displayTags || displayAll) visibilityMarker else invisibilityMarker
}

/**
 * A Logback filter that controls the visibility of log messages based on markers.
 *
 * This filter examines the marker attached to each log event and decides whether to accept or deny it based on
 * the following rules:
 *
 * - If the event has the `visibilityMarker`, it's accepted.
 * - If the event has the `invisibilityMarker`, it's denied.
 * - If the event has no marker, it's neutral (the decision is left to the logger's level and other filters).
 */
class VisibilityFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        return when (event.marker) {
            visibilityMarker -> FilterReply.ACCEPT
            invisibilityMarker -> FilterReply.DENY
            else -> FilterReply.NEUTRAL // If no marker is present, let the logger's level decide
        }
    }
}