package lsd.junit5.extension;

import com.lsd.LsdContext;
import lsd.junit5.LsdExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(LsdExtension.class)
public class LsdExtensionDemoTest {

    private LsdContext lsdContext = LsdContext.getInstance();
    
    @ParameterizedTest
    @ValueSource(strings = {"apple", "berry", "cat"})
    void aParameterisedTest(String input) {
        lsdContext.capture("a package from A to B", input);
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
            lsdContext.capture("running nested test from Outer to Inner", "");
            assertThat(true).isTrue();
        }
    }
}
