package lsd.junit5.extension;

import com.lsd.LsdContext;
import lsd.junit5.LsdExtension;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void setup() {
        lsdContext.clear();
    }

    @Test
    void testResultsAreIncludedInReport() {
        configureMockContextToReturnDisplayNames();

        lsdExtension.testFailed(mockContext, new AssertionError("Expected <true> but was <false>"));
        lsdExtension.testSuccessful(mockContext);
        lsdExtension.testAborted(mockContext, new RuntimeException("Aborted for testing reasons"));
        lsdExtension.testDisabled(mockContext, Optional.of("Aborted reason"));

        Approvals.verify(lsdContext.completeReport("LsdExtension Test Report").toFile());
    }

    private void configureMockContextToReturnDisplayNames() {
        when(mockContext.getRequiredTestInstance()).thenReturn(this);
        when(mockContext.getParent())
                .thenReturn(Optional.of(mockContext))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockContext))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockContext))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(mockContext))
                .thenReturn(Optional.empty())
        ;
        when(mockContext.getDisplayName())
                .thenReturn("The failed test name")
                .thenReturn("The successful test name")
                .thenReturn("The aborted test name")
                .thenReturn("The disabled test name");
    }
}
