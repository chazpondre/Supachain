@file:Suppress("unused")

package ai.anything.examples.toolCalling

import dev.supachain.robot.tool.ToolSet
import dev.supachain.robot.Robot
import dev.supachain.robot.provider.models.LocalAI
import dev.supachain.robot.tool.strategies.FillInTheBlank
import dev.supachain.robot.Defaults.Chat
import dev.supachain.utilities.Debug
import kotlin.math.*
import kotlin.time.Duration.Companion.minutes

interface ArithmeticMath {
    fun add(left: Double, right: Double): Double = left + right
    fun subtract(left: Double, right: Double): Double = left - right
    fun multiply(left: Double, right: Double): Double = left * right

    fun divide(left: Double, right: Double): Double {
        require(right != 0.0) { "Division by zero is not allowed" }
        return left / right
    }

    fun modulo(left: Double, right: Double): Double {
        require(right != 0.0) { "Modulo by zero is not allowed" }
        return left % right
    }

    fun sqrt(a: Double): Double {
        require(a >= 0.0) { "Cannot calculate square root of a negative number" }
        return kotlin.math.sqrt(a)
    }

    fun power(a: Double, b: Double): Double = a.pow(b)
}

// Logarithms
interface Logarithms {
    fun ln(a: Double): Double {
        require(a > 0.0) { "Cannot calculate natural logarithm of a non-positive number" }
        return kotlin.math.ln(a)
    }

    fun logBase10(a: Double): Double {
        require(a > 0.0) { "Cannot calculate logarithm (base 10) of a non-positive number" }
        return log10(a)
    }

    fun logBase2(a: Double): Double {
        require(a > 0.0) { "Cannot calculate logarithm (base 10) of a non-positive number" }
        return log2(a)
    }

    fun logBase(a: Double, base: Double): Double {
        require(a > 0.0) { "Cannot calculate logarithm (base 10) of a non-positive number" }
        return log(a, base)
    }

    fun factorial(n: Int): Long {
        require(n >= 0) { "Factorial is not defined for negative numbers" }
        return factorialTailRec(n, 1)
    }
}

interface Trigonometry {
    fun sine(a: Double): Double = sin(a)
    fun cosine(a: Double): Double = cos(a)
    fun tangent(a: Double): Double = tan(a)
    fun weather(city: String): Double = 27.0

    fun hypotenuse(leg1: Double, leg2: Double): Double {
        require(leg1 > 0 && leg2 > 0) { "Leg lengths must be positive" }
        return sqrt(leg1.pow(2) + leg2.pow(2))
    }

    fun calculateRemainingLeg(hypotenuse: Double, knownLeg: Double): Double {
        require(hypotenuse > 0 && knownLeg > 0) { "Hypotenuse and leg lengths must be positive" }
        require(hypotenuse > knownLeg) { "Hypotenuse must be longer than the leg" }
        return sqrt(hypotenuse.pow(2) - knownLeg.pow(2))
    }

    fun hypotenuseFromAdjacentAndAdjacentAngle(adjacent: Double, angle: Double): Double {
        require(adjacent > 0) { "Adjacent side length must be positive" }
        require(angle > 0 && angle < 90) { "Angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(angle)
        return adjacent / cos(angleInRadians)
    }

    fun hypotenuseFromOppositeAndOppositeAngle(opposite: Double, oppositeAngle: Double): Double {
        require(opposite > 0) { "Opposite side length must be positive" }
        require(oppositeAngle > 0 && oppositeAngle < 90) { "Opposite angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(oppositeAngle)
        return opposite / sin(angleInRadians)
    }

    fun adjacentFromHypotenuseAndHypotenuseAngle(hypotenuse: Double, angle: Double): Double {
        require(hypotenuse > 0) { "Hypotenuse length must be positive" }
        require(angle > 0 && angle < 90) { "Angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(angle)
        return hypotenuse * cos(angleInRadians)
    }

    fun adjacentFromOppositeAndAngle(opposite: Double, adjacentAngle: Double): Double {
        require(opposite > 0) { "Opposite side length must be positive" }
        require(adjacentAngle > 0 && adjacentAngle < 90) { "Adjacent angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(adjacentAngle)
        return opposite / tan(angleInRadians)
    }

    fun adjacentFromOppositeAndHypotenuse(opposite: Double, hypotenuse: Double): Double {
        require(opposite > 0 && hypotenuse > 0) { "Opposite side and hypotenuse lengths must be positive" }
        require(hypotenuse > opposite) { "Hypotenuse must be longer than the opposite side" }
        return sqrt(hypotenuse.pow(2) - opposite.pow(2))
    }

    fun oppositeFromHypotenuseAndAdjacentAngle(hypotenuse: Double, adjacentAngle: Double): Double {
        require(hypotenuse > 0) { "Hypotenuse length must be positive" }
        require(adjacentAngle > 0 && adjacentAngle < 90) { "Adjacent angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(adjacentAngle)
        return hypotenuse * sin(angleInRadians)
    }

    fun oppositeFromHypotenuseAndOppositeAngle(hypotenuse: Double, oppositeAngle: Double): Double {
        require(hypotenuse > 0) { "Hypotenuse length must be positive" }
        require(oppositeAngle > 0 && oppositeAngle < 90) { "Opposite angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(oppositeAngle)
        return hypotenuse * sin(angleInRadians)
    }

    fun oppositeFromAdjacentAndAngle(adjacent: Double, adjacentAngle: Double): Double {
        require(adjacent > 0) { "Adjacent side length must be positive" }
        require(adjacentAngle > 0 && adjacentAngle < 90) { "Adjacent angle must be between 0 and 90 degrees" }
        val angleInRadians = Math.toRadians(adjacentAngle)
        return adjacent * tan(angleInRadians)
    }

    fun angleFromOppositeAndHypotenuse(opposite: Double, hypotenuse: Double): Double {
        require(opposite > 0 && hypotenuse > 0) { "Opposite side and hypotenuse lengths must be positive" }
        require(hypotenuse > opposite) { "Hypotenuse must be longer than the opposite side" }
        val angleInRadians = asin(opposite / hypotenuse)
        return Math.toDegrees(angleInRadians)
    }

    fun angleFromAdjacentAndHypotenuse(adjacent: Double, hypotenuse: Double): Double {
        require(adjacent > 0 && hypotenuse > 0) { "Adjacent side and hypotenuse lengths must be positive" }
        require(hypotenuse > adjacent) { "Hypotenuse must be longer than the adjacent side" }
        val angleInRadians = acos(adjacent / hypotenuse)
        return Math.toDegrees(angleInRadians)
    }
}

interface NumberTheory {
    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b) // Euclidean Algorithm
    fun lcm(a: Int, b: Int): Int = abs(a * b) / gcd(a, b)
    fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0 || n % 3 == 0) return false
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        return true
    }
}

interface StatisticsMath {
    fun mean(vararg numbers: Double): Double = numbers.average()
    fun median(vararg numbers: Double): Double {
        require(numbers.isNotEmpty()) { "Cannot calculate median of an empty list" }
        val sorted = numbers.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[middle - 1] + sorted[middle]) / 2 else sorted[middle]
    }

    fun mode(vararg numbers: Int): List<Int> {
        require(numbers.isNotEmpty()) { "Cannot calculate mode of an empty list" }
        val frequencyMap = numbers.toList().groupingBy { it }.eachCount()
        val maxFrequency = frequencyMap.values.maxOrNull() ?: 0
        return frequencyMap.filterValues { it == maxFrequency }.keys.toList()
    }

    fun modeOfAny(vararg items: Any): List<Any> {
        require(items.isNotEmpty()) { "Cannot calculate mode of an empty list" }
        val frequencyMap = items.toList().groupingBy { it }.eachCount()
        val maxFrequency = frequencyMap.values.maxOrNull() ?: 0
        return frequencyMap.filterValues { it == maxFrequency }.keys.toList()
    }

    fun variance(vararg numbers: Double): Double {
        require(numbers.size > 1) { "Cannot calculate variance of a list with less than two elements" }
        val mean = mean(* numbers)
        return numbers.sumOf { (it - mean).pow(2) } / (numbers.size - 1)
    }

    fun standardDeviation(vararg numbers: Double): Double {
        return sqrt(variance(* numbers))
    }
}


@ToolSet
class MathWithoutAnnotations : ArithmeticMath, Trigonometry, NumberTheory, StatisticsMath, Logarithms

private tailrec fun factorialTailRec(n: Int, accumulator: Long): Long {
    return if (n == 0) accumulator else factorialTailRec(n - 1, n * accumulator)
}

fun main() {
    Debug show "Messenger"

    val robot = Robot<LocalAI, Chat, MathWithoutAnnotations> {
        defaultProvider {
            url = "http://localhost:8888"
            temperature = 0.0
            chatModel = "meta-llama-3.1-8b-instruct"
            toolStrategy = FillInTheBlank

            network {
                socketTimeout = 5.minutes
                requestTimeout = 5.minutes
            }
        }
    }

    val problem =
        """
        If the question is `what is a + b`, state clearly the question with the answer ie. `a + b = to some answer`.
        Please format your answers for clarity, answer all of the following:
        
        1. What is 11 * 717?
        2. What is 5 * 4?
        3. What is 14 * 3?
        4. What is 919191 + 80809?
        5. What's the square root of 2, 3 and 4?
        6. What's 8 - 7?
        7. What is 9 % 2?
        8. 84 / 7 ?
        9. 2^5?
        10. What is the natural logarithm of 10?
        11. What is the logarithm (base 10) of 1000?
        12. What is the sine of 3.1418 radians?
        13. What is the cosine of 0 radians?
        14. What is the tangent of 1.5707 radians?
        15. What is 5 * 4 + 1?
        16. 3 + 5 * 2 / 4 - 1
        17. In a survey, the following responses were collected: A, B, C, A, A, B. Which response is the mode?
        18. 5!
        19. What is √12321
        20. log₂ 8
        21. For a right angle triangle, what is the hypotenuse if the adjacent is 25, and
        the angle between adjacent and hypotenuse is 10 degrees.
        22. A surveyor wants to know the height of a skyscraper. He places his inclinometer on a tripod 1m
        from the ground. At a distance of 50m away from the skyscraper, he records an angle of elevation of 82
        degrees from the inclinometer. What is the height of the skyscraper?
        23. John's scores on his last 5 math tests were 85, 92, 78, 88, and 95. What is his average score?
        24. A dataset contains the following values: 3.2, 5.1, 4.7, 6.0, 2.9. Calculate the mean.
        25. Find the median of the following numbers: 12, 7, 15, 3, 9, 11.
        26. The ages of a group of people are: 25, 30, 30, 32, 35, 40. What is the median age?
        27. Determine the mode of this set of data: 2, 5, 3, 2, 8, 5, 2."
        28. What is 5 * 4 + 1 and whats the weather in Toronto?
        29. Calculate the variance and standard deviation for the following sample: 10, 12, 15, 17, 20.
        30. A set of data has a mean of 50 and a variance of 100. What is its standard deviation?
        31. 3 + 5 * 2 / (1 + 3) - 1?
        
""".trimIndent()
    val answer = robot.chat(problem).await()
    println(answer)
}

