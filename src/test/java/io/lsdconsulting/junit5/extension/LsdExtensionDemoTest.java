package io.lsdconsulting.junit5.extension;

import com.lsd.core.LsdContext;
import io.lsdconsulting.junit5.LsdExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(LsdExtension.class)
public class LsdExtensionDemoTest {

    private final LsdContext lsd = LsdContext.getInstance();

    @ParameterizedTest
    @ValueSource(strings = {"apple", "berry", "cat"})
    void aParameterisedTest(String input) {
        lsd.capture(messageBuilder().from("A").to("B").data(input).build());
        assertThat(List.of("apple", "berry", "cat")).contains(input);
    }

    @Test
    void aRegularTest() {
        assertThat(true).isTrue();
    }

    @Nested
    @DisplayName("A Nested test class")
    class NestedTestClass {
        @Test
        @DisplayName("This is a nested test!")
        void nestedTest() {
            lsd.capture(messageBuilder().from("Outer").to("Inner=").data("").build());
            assertThat(true).isTrue();
        }

        @Nested
        @DisplayName("Nested twice test class")
        class NestedTwiceTestClass {
            @Test
            @DisplayName("This is a nested nested test!")
            void nestedTest() {
                lsd.capture(messageBuilder().from("Nester").to("InnerNester").label("running doubly nested test").data("").build());
                assertThat(true).isTrue();
            }
        }
    }
}
