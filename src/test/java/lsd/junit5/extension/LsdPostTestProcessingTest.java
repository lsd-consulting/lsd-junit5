package lsd.junit5.extension;

import lsd.junit5.LsdExtension;
import lsd.junit5.LsdPostTestProcessing;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(LsdExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class LsdPostTestProcessingTest {

    private static final AtomicInteger atomicInteger = new AtomicInteger();

    @Test
    @Order(1)
    void annotatedMethodNotInvokedBeforeFirstTest() {
        assertThat(atomicInteger.get()).isEqualTo(0);
    }

    @Test
    @Order(2)
    void annotatedMethodWasInvokedAfterPreviousTest() {
        assertThat(atomicInteger.get()).isEqualTo(1);
    }

    @LsdPostTestProcessing
    public void postTestProcessing() {
        atomicInteger.incrementAndGet();
    }
}
