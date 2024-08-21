@file:Suppress("unused")

package dev.supachain.utilities

/**
 * Represents a string with embedded template expressions.
 *
 * This value class encapsulates a pair of lists:
 *  - A list of extracted template expressions (e.g., function calls within curly braces).
 *  - The original string with template expressions replaced by a placeholder character.
 *
 * It provides convenient access to these components and supports filling the template with results.
 *
 * @property data A Pair containing the list of expressions and the modified string.
 *
 * @since 0.1.0-alpha
 */
@JvmInline
value class TemplateString(private val data: Pair<List<String>, String>) {
    /**
     * Constructs a `TemplateString` from a list of expressions and the modified string.
     *
     * @param templates The list of extracted template expressions.
     * @param string The original string with template expressions replaced by a placeholder.
     */
    constructor(templates: List<String>, string: String) : this(Pair(templates, string))

    val expressions get() = data.first
    val string get() = data.second

    fun fill(results: List<String>) = results.fold(string) { template, replacement ->
        template.replaceFirst("�", replacement)
    }

    infix fun isTheSameAs(templateString: TemplateString): Boolean =
        expressions.containsAll(templateString.expressions)
                && templateString.expressions.containsAll(expressions)
                && string == templateString.string

    companion object {
        /**
         * Extracts template expressions from a string and creates a `TemplateString`.
         *
         * This operator function parses the input string, identifies template expressions delimited by
         * `startPoint`, `readPoint`, and `endPoint`, and replaces them with the `replacement` character.
         * It handles nested expressions, escaped characters, and string literals.
         *
         * @param string The input string potentially containing template expressions.
         * @param startPoint The character indicating the start of a template expression (default: U+200B ZERO WIDTH SPACE).
         * @param readPoint The character indicating the opening delimiter of a template expression (default: `{`).
         * @param endPoint The character indicating the closing delimiter of a template expression (default: `}`).
         * @param replacement The character to replace template expressions with in the modified string (default: U+001A SUBSTITUTE CHARACTER).
         * @param escape The escape character used to escape special characters within template expressions (default: `\`).
         * @return A `TemplateString` object containing the extracted expressions and the modified string.
         * @throws IllegalArgumentException If there are unmatched curly braces in the template expressions.
         */
        operator fun invoke(
            string: String,
            startPoint: Char = '\u200B',
            readPoint: Char = '{',
            endPoint: Char = '}',
            replacement: Char = '�',
            escape: Char = '\\'
        ): TemplateString = with(string) {
            val templateString = StringBuilder()
            val templates = mutableListOf<String>()
            val currentTemplate = StringBuilder()
            var inTemplate = false
            var braceCount = 0
            var isEscaped = false
            var inStringLiteral = false // Track if we are inside a string literal
            var i = 0

            while (i < length) {
                val next = i + 1
                val char = this[i]

                if (isEscaped) {
                    currentTemplate.append(char)
                    templateString.append(char)
                    isEscaped = false
                } else {
                    when (char) {
                        escape -> isEscaped = true
                        startPoint -> {
                            if (inTemplate) currentTemplate.append(char)
                            else if (next < length && this[next] == readPoint) {
                                inTemplate = true
                                braceCount = 1
                                i++ // Skip '{'
                            } else templateString.append(char)
                        }

                        readPoint -> {
                            if (inTemplate) {
                                if (!inStringLiteral) { // Only increment braceCount if not in a string literal
                                    braceCount++
                                }
                                currentTemplate.append(char)
                            } else templateString.append(char)
                        }

                        endPoint -> {
                            if (inTemplate) {
                                if (!inStringLiteral) {
                                    braceCount--
                                    if (braceCount == 0) {
                                        // End of the outermost template expression
                                        if (currentTemplate.isNotEmpty()) {
                                            // Only add if the template is not empty
                                            templates.add(currentTemplate.toString())
                                            templateString.append(replacement)
                                        }
                                        currentTemplate.clear()
                                        inTemplate = false
                                    } else {
                                        // Closing brace of a nested expression
                                        currentTemplate.append(char)
                                    }
                                } else {
                                    // Closing brace within a string literal
                                    currentTemplate.append(char)
                                }
                            } else {
                                templateString.append(char)
                            }
                        }

                        '"' -> {
                            // Toggle inStringLiteral when encountering a quote (assuming properly escaped quotes)
                            inStringLiteral = !inStringLiteral
                            if (inTemplate) currentTemplate.append(char)
                            else templateString.append(char)
                        }

                        else -> {
                            if (inTemplate) currentTemplate.append(char)
                            else templateString.append(char)
                        }
                    }
                }
                i++
            }

            if (braceCount != 0) {
                throw IllegalArgumentException("Unmatched curly braces in template expression")
            }

            return TemplateString(templates, templateString.toString())
        }
    }
}

/**
 * Extracts Kotlin string template expressions from an input string.
 *
 * This function parses the input string to identify and extract function calls within Kotlin string templates.
 * It handles nested template expressions, escaped characters, and provides robust error handling.
 *
 * @receiver The input string potentially containing template expressions.
 * @return A `TemplateString` object containing the extracted expressions and the modified string
 * @throws IllegalArgumentException If the input string contains malformed template expressions.
 */
internal fun String.templates(): TemplateString = TemplateString(this)