package io.lsdconsulting.junit5.extension

import com.lsd.core.LsdContext
import com.lsd.core.properties.LsdProperties.OUTPUT_DIR
import com.lsd.core.properties.LsdProperties.get
import io.lsdconsulting.junit5.LsdExtension
import io.lsdconsulting.junit5.deCamelCase
import org.approvaltests.Approvals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*

internal class LsdExtensionTest {
    private val mockTestContext = mock(ExtensionContext::class.java)
    private val lsdContext: LsdContext = LsdContext.instance
    private val lsdExtension = LsdExtension()
    private val indexFile = File(get(OUTPUT_DIR), "lsd-index.html")

    @BeforeEach
    fun setup() {
        lsdContext.clear()
    }

    @Test
    fun approvalTestForTestFailedCallback() {
        configureMockContextToReturnDisplayNames()
        lsdExtension.testFailed(mockTestContext, AssertionError("Expected <true> but was <false>"))
        Approvals.verify(lsdContext.completeReport("Failure Test Report").toFile())
    }

    @Test
    fun approvalTestForTestSuccessfulCallback() {
        configureMockContextToReturnDisplayNames()
        lsdExtension.testSuccessful(mockTestContext)
        Approvals.verify(copyOfFile(lsdContext.completeReport("Successful Test Report")))
    }

    @Test
    fun approvalTestForTestAbortedCallback() {
        configureMockContextToReturnDisplayNames()
        lsdExtension.testAborted(mockTestContext, RuntimeException("Aborted for testing reasons"))
        Approvals.verify(copyOfFile(lsdContext.completeReport("Aborted Test Report")))
    }

    @Test
    fun approvalTestForTestDisabledCallback() {
        configureMockContextToReturnDisplayNames()
        lsdExtension.testDisabled(mockTestContext, Optional.of("Disabled reason"))
        Approvals.verify(copyOfFile(lsdContext.completeReport("Disabled Test Report")))
    }

    @Test
    fun generateIndexAfterAllTestsInTestClass() {
        indexFile.delete()
        `when`(mockTestContext.parent).thenReturn(Optional.empty())
        `when`(mockTestContext.displayName).thenReturn("a display name")
        lsdExtension.afterAll(mockTestContext)
        assertThat(indexFile).exists()
    }

    @Test
    fun doesNotGenerateIndexWhenTestIsNested() {
        indexFile.delete()
        `when`(mockTestContext.parent).thenReturn(Optional.of(mockTestContext))
        `when`(mockTestContext.displayName).thenReturn("a nested display name")
        lsdExtension.afterAll(mockTestContext)
        assertThat(indexFile).doesNotExist()
    }

    @ParameterizedTest
    @CsvSource(
        "aCamelCaseExample,a camel case example",
        "CamelCase,camel case",
        "anITSolution,an it solution",
        "myClassUnderTestShouldDoSomethingNice(),my class under test should do something nice",
    )
    fun deCamelCase(input: String, expected: String) {
        assertThat(input.deCamelCase()).isEqualTo(expected)
    }

    private fun configureMockContextToReturnDisplayNames() {
        `when`(mockTestContext.requiredTestInstance).thenReturn(this)
        `when`(mockTestContext.displayName).thenReturn("aTestName()")
        `when`(mockTestContext.parent)
            .thenReturn(Optional.of(mockTestContext))
            .thenReturn(Optional.empty())
    }

    private fun copyOfFile(original: Path): File {
        val copy = Path.of(original.parent.toString(), "copy_" + original.fileName)
        Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING)
        return copy.toFile()
    }
}
