package io.lsdconsulting.junit5.extension;

import com.lsd.LsdContext;
import com.lsd.properties.LsdProperties;
import io.lsdconsulting.junit5.LsdExtension;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.util.Optional;

import static com.lsd.properties.LsdProperties.OUTPUT_DIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LsdExtensionTest {
    private final ExtensionContext mockTestContext = mock(ExtensionContext.class);
    private final LsdContext lsdContext = LsdContext.getInstance();
    private final LsdExtension lsdExtension = new LsdExtension();
    private final File indexFile = new File(LsdProperties.get(OUTPUT_DIR), "lsd-index.html");

    @BeforeEach
    public void setup() {
        lsdContext.clear();
    }

    @Test
    void approvalTestForTestFailedCallback() {
        configureMockContextToReturnDisplayNames();

        lsdExtension.testFailed(mockTestContext, new AssertionError("Expected <true> but was <false>"));

        Approvals.verify(lsdContext.completeReport("Failure Test Report").toFile());
    }
    
    @Test
    void approvalTestForTestSuccessfulCallback() {
        configureMockContextToReturnDisplayNames();

        lsdExtension.testSuccessful(mockTestContext);

        Approvals.verify(lsdContext.completeReport("Successful Test Report").toFile());
    }    
    
    @Test
    void approvalTestForTestAbortedCallback() {
        configureMockContextToReturnDisplayNames();

        lsdExtension.testAborted(mockTestContext, new RuntimeException("Aborted for testing reasons"));

        Approvals.verify(lsdContext.completeReport("Aborted Test Report").toFile());
    }

    @Test
    void approvalTestForTestDisabledCallback() {
        configureMockContextToReturnDisplayNames();

        lsdExtension.testDisabled(mockTestContext, Optional.of("Disabled reason"));

        Approvals.verify(lsdContext.completeReport("Disabled Test Report").toFile());
    }

    @Test
    void generateIndexAfterAllTestsInTestClass() {
        indexFile.delete();
        when(mockTestContext.getParent()).thenReturn(Optional.empty());
        when(mockTestContext.getDisplayName()).thenReturn("a display name");

        lsdExtension.afterAll(mockTestContext);

        assertThat(indexFile).exists();
    }

    @Test
    void doesNotGenerateIndexWhenTestIsNested() {
        indexFile.delete();
        when(mockTestContext.getParent()).thenReturn(Optional.of(mockTestContext));
        when(mockTestContext.getDisplayName()).thenReturn("a nested display name");

        lsdExtension.afterAll(mockTestContext);

        assertThat(indexFile).doesNotExist();
    }

    private void configureMockContextToReturnDisplayNames() {
        when(mockTestContext.getRequiredTestInstance()).thenReturn(this);
        when(mockTestContext.getDisplayName()).thenReturn("The test name");
        when(mockTestContext.getParent())
                .thenReturn(Optional.of(mockTestContext))
                .thenReturn(Optional.empty());
    }
}
