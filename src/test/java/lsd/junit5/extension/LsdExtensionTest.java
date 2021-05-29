package lsd.junit5.extension;

import com.lsd.LsdContext;
import com.lsd.events.Message;
import lsd.junit5.LsdExtension;
import lsd.junit5.LsdPostTestProcessing;
import lsd.junit5.LsdPreTestProcessing;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LsdExtensionTest {
    private ExtensionContext mockContext = mock(ExtensionContext.class, Mockito.RETURNS_DEEP_STUBS);

    private LsdContext lsdContext = LsdContext.getInstance();
    private LsdExtension lsdExtension = new LsdExtension();

    @Test
    void createsScenarioWhenInformedOfTestFailure() {
        when(mockContext.getRequiredTestInstance()).thenReturn(this);
        when(mockContext.getDisplayName())
                .thenReturn("The failed test name")
                .thenReturn("The successful test name")
                .thenReturn("The aborted test name")
                .thenReturn("The disabled test name");

        lsdExtension.testFailed(mockContext, new RuntimeException("Failed for testing purposes"));
        lsdExtension.testSuccessful(mockContext);
        lsdExtension.testAborted(mockContext, new RuntimeException("Aborted for testing reasons"));
        lsdExtension.testDisabled(mockContext, Optional.of("Aborted reason"));

        Approvals.verify(lsdContext.completeReport("LsdExtension Test Report").toFile());
    }

    @LsdPreTestProcessing
    public void preTestProcessing() {
        lsdContext.capture(Message.builder()
                .id("abc123")
                .from("LsdExtension")
                .to("LsdExtensionTest")
                .data("preTestProcessing()")
                .label("pre test processing")
                .build());
    }

    @LsdPostTestProcessing
    public void postTestProcessing() {
        lsdContext.capture(Message.builder()
                .id("abc456")
                .from("LsdExtension")
                .to("LsdExtensionTest")
                .data("postTestProcessing()")
                .label("post test processing")
                .build());
    }
}
