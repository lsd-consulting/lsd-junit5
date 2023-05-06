package io.lsdconsulting.junit5.extension

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder
import io.lsdconsulting.junit5.LsdExtension
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(LsdExtension::class)
class LsdExtensionDemoTest {
    private val lsd: LsdContext = LsdContext.instance
    @ParameterizedTest
    @ValueSource(strings = ["apple", "berry", "cat"])
    fun aParameterisedTest(input: String?) {
        lsd.capture(MessageBuilder.messageBuilder().from("A").to("B").data(input!!).build())
        Assertions.assertThat(listOf("apple", "berry", "cat")).contains(input)
    }

    @Test
    fun aRegularTest() {
        Assertions.assertThat(true).isTrue()
    }

    @Nested
    @DisplayName("A Nested test class")
    internal inner class NestedTestClass {
        @Test
        @DisplayName("This is a nested test!")
        fun nestedTest() {
            lsd.capture(MessageBuilder.messageBuilder().from("Outer").to("Inner").data("").build())
            Assertions.assertThat(true).isTrue()
        }

        @Nested
        @DisplayName("Nested twice test class")
        internal inner class NestedTwiceTestClass {
            @Test
            @DisplayName("This is a nested nested test!")
            fun nestedTest() {
                lsd.capture(
                    MessageBuilder.messageBuilder().from("Nester").to("InnerNester").label("running doubly nested test")
                        .data("").build()
                )
                Assertions.assertThat(true).isTrue()
            }
        }
    }
}
