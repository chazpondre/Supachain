/*
░░░░░░░░░░░        ░░░      ░░░       ░░░  ░░░░  ░░░      ░░░        ░░        ░░        ░░   ░░░  ░░░      ░░░░░░░░░░░░
▒▒▒▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒  ▒▒  ▒▒▒▒  ▒▒   ▒▒   ▒▒  ▒▒▒▒  ▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒▒▒▒  ▒▒▒▒▒    ▒▒  ▒▒  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒
▓▓▓▓▓▓▓▓▓▓▓      ▓▓▓▓  ▓▓▓▓  ▓▓       ▓▓▓        ▓▓  ▓▓▓▓  ▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓  ▓▓▓▓▓  ▓  ▓  ▓▓  ▓▓▓   ▓▓▓▓▓▓▓▓▓▓▓
███████████  ████████  ████  ██  ███  ███  █  █  ██        █████  ████████  ████████  █████  ██    ██  ████  ███████████
███████████  █████████      ███  ████  ██  ████  ██  ████  █████  ████████  █████        ██  ███   ███      ████████████
 */

package dev.supachain.robot.answer

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import dev.supachain.utilities.reinforceRange
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//Numbers
private const val NumberRulesHeader =
    """Do not include any other words, symbols, or explanations. **No Words or Symbols:** Do not include any words, explanations, symbols, or letters (like "e", "PI", "euler", etc.).If you answer is a the result of some operation, write only the the numeric answer. for instance a + b = c, write Only c.
If you answer is a the result of some function, write only the the numeric answer. for instance x + y = z, write Only z.
The numbers must be in the format of """

private const val IntegerRulesHeader =
    """* **Integer Only:** The answer must be a whole number (no decimals).* 
        |**Examples:**"""

private const val FloatingRulesHeader =
    "* **Floating-point Only:** The answer can contain decimals." +
            "* **Examples:**"

private const val ChoiceRules = "You cannot say any thing more than that. Choose One. Only say what you chose"

// Enum
private const val EnumRulesHeader = " Answer strictly in the following format: one of"
private const val BooleanRulesHeader = "$EnumRulesHeader [true, false]. $ChoiceRules"

// Date Time
private const val LocalDateRules = "Answer strictly in the following format `yyyy-MM-dd`. Write formatted answer only."
private const val LocalDateTimeRules =
    "Answer strictly in the following format as yyyy-MM-ddTHH:mm:ss. Write formatted answer only."
private const val LocalTimeRules = "Answer strictly in the following format as HH:mm:ss. Write formatted answer only."

// Guidelines
private fun guideline(it: Number) = "Do NOT write `the result is $it`\n\n Write instead ONLY `$it`"
private fun guideline(it: String) = "Do NOT write `the result is $it`\n\n Write instead ONLY `$it`"

/**
 * Determines the expected output format rules based on the KClass.
 *
 * This function analyzes the KClass to provide detailed instructions on how the Robot model
 * should format its response for various types such as numbers, booleans, collections, dates, and enums.
 *
 * @receiver The KClass representing the type to format.
 * @return A string containing instructions on how to format the output.
 * @throws IllegalArgumentException If the type is unsupported.
 */
fun KClass<*>.formatRules(): String = when {
    isSubclassOf(String::class) -> "A string of words answering the question."

    // Handle numeric types
    isSubclassOf(Number::class) ->
        NumberRulesHeader + when {
            isSubclassOf(Float::class) || isSubclassOf(Double::class) || isSubclassOf(BigDecimal::class) -> {
                // BigDec Support TODO
                val min =
                    if (isSubclassOf(Float::class)) Float.MIN_VALUE else if (isSubclassOf(Double::class)) Double.MIN_VALUE else Double.MIN_VALUE
                val max =
                    if (isSubclassOf(Float::class)) Float.MAX_VALUE else if (isSubclassOf(Double::class)) Double.MAX_VALUE else Double.MAX_VALUE
                FloatingRulesHeader +
                        reinforceRange({ Random.nextFloat() }, min, max, 5) { guideline("%.5f".format(it)) }
            }

            isSubclassOf(Byte::class) -> IntegerRulesHeader + with(Byte) {
                reinforceRange(
                    { Random.nextInt(MIN_VALUE.toInt(), MAX_VALUE.toInt()) }, MIN_VALUE, MAX_VALUE, 5, ::guideline
                )
            }

            isSubclassOf(Short::class) -> IntegerRulesHeader + with(Short) {
                reinforceRange(
                    { Random.nextInt(MIN_VALUE.toInt(), MAX_VALUE.toInt()) }, MIN_VALUE, MAX_VALUE, 5, ::guideline
                )
            }

            else -> IntegerRulesHeader +
                    reinforceRange({ Random.nextInt(-1000, 1000) }, Int.MIN_VALUE, Int.MAX_VALUE, 5, ::guideline)
        }

    // Choice Types
    isSubclassOf(Boolean::class) -> BooleanRulesHeader
    isSubclassOf(Enum::class) -> buildString {
        append("\n $EnumRulesHeader [")
        this@formatRules.java.enumConstants.joinTo(this, ", ") { it.toString() }
        append("]. $ChoiceRules")
    }

    // Handle date/time types
    isSubclassOf(LocalDate::class) -> LocalDateRules
    isSubclassOf(LocalDateTime::class) -> LocalDateTimeRules
    isSubclassOf(LocalTime::class) -> LocalTimeRules

    // Handle unsupported types
    else -> throw IllegalArgumentException("Unsupported return type: $this")
}


