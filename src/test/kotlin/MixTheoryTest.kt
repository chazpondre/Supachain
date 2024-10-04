import dev.supachain.mixer.Generator
import dev.supachain.mixer.concept
import dev.supachain.mixer.mix
import kotlin.test.Test
import kotlin.test.assertEquals

class TestGenerator : Generator<String> {
    override fun generator(input: String): String = input
}

class MixTheoryTest {

    @Test
    fun testBasicMixing() {
        // Define the mixing concepts
        val pros = concept { "pros($input)" }
        val cons = concept { "cons($input)" }
        val summary = mix { "$input -> $outputs -> remix" }

        // Use the TestGenerator to mix
        val mixer = (pros + cons to summary) using TestGenerator()

        // Perform the mixing operation
        val result = mixer("hello")

        // Assert the expected result
        val expected = "hello -> [pros(hello), cons(hello)] -> remix"
        assertEquals(expected, result)
    }

    @Test
    fun testMixingWithRepeater() {
        // Define a concept with repetition
        val pros = concept { "pros($input)" }
        val repeater = pros * 2
        val cons = concept { "cons($input)" }
        val summary = mix { "$input -> $outputs -> remix" }

        // Use the TestGenerator to mix
        val mixer = (repeater + cons to summary) using TestGenerator()

        // Perform the mixing operation
        val result = mixer("world")

        // Assert the expected result
        val expected = "world -> [pros(pros(world)), cons(world)] -> remix"
        assertEquals(expected, result)
    }

    @Test
    fun testMultiTrackMixing() {
        // Define multiple tracks
        val pros = concept { "pros($input)" }
        val cons = concept { "cons($input)" }
        val betterPros = concept { "better($input)" }
        val summary = mix { "$input -> $outputs -> remix" }

        // Create a multi-track mixer
        val mixer = ((pros * betterPros + cons) to summary) using TestGenerator()

        // Perform the mixing operation
        val result = mixer("data")

        // Assert the expected result
        val expected = "data -> [better(pros(data)), cons(data)] -> remix"
        assertEquals(expected, result)
    }

    @Test
    fun testNestedMixing() {
        // Define nested mixable concepts
        val pros = concept { "pros($input)" }
        val cons = concept { "cons($input)" }
        val innerMixer = (pros + cons to mix { "$input -> inner" })
        val outerMixer = (innerMixer to mix { "$input -> $outputs -> outer" }) using TestGenerator()

        // Perform the mixing operation
        val result = outerMixer("test")

        // Assert the expected result
        val expected = "test -> [test -> inner] -> outer"
        assertEquals(expected, result)
    }
}
