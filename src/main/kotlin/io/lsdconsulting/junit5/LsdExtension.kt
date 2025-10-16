package io.lsdconsulting.junit5

import com.lsd.core.LsdContext
import com.lsd.core.domain.Status
import com.lsd.core.escapeHtml
import com.lsd.core.properties.LsdProperties.getBoolean
import com.lsd.core.report.PopupContent.popupHyperlink
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import org.junit.platform.commons.util.ExceptionUtils
import org.junit.platform.commons.util.StringUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 * Junit 5 extension to create LSD reports based on test results.
 * (Not that this extension does not capture any events for the LSD diagrams)
 */
class LsdExtension : TestWatcher, AfterTestExecutionCallback, AfterAllCallback {
    private val lsdContext: LsdContext = LsdContext.instance
    private val idGenerator = lsdContext.idGenerator
    private val hideStacktrace = getBoolean("lsd.junit5.hideStacktrace")
    override fun testSuccessful(context: ExtensionContext) {
        lsdContext.completeScenario(
            prefixParentDisplayName(context),
            "<p><h4 class=\"success\">&#10003; Test Passed</h4></p>", Status.SUCCESS
        )
    }

    override fun testDisabled(context: ExtensionContext, reason: Optional<String>) {
        lsdContext.completeScenario(
            prefixParentDisplayName(context),
            "<p><h4 class=\"warn\">Test Disabled</h4></p>", Status.FAILURE
        )
    }

    override fun testAborted(context: ExtensionContext, cause: Throwable?) {
        val description = createErrorDescription(cause, "Test Aborted!")
        lsdContext.completeScenario(prefixParentDisplayName(context), description, Status.FAILURE)
    }

    override fun testFailed(context: ExtensionContext, cause: Throwable?) {
        val description = createErrorDescription(cause, "&#10060; Failed!")
        lsdContext.completeScenario(prefixParentDisplayName(context), description, Status.ERROR)
    }

    override fun afterTestExecution(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance
        additionalProcessing(testInstance, LsdPostTestProcessing::class.java)
    }

    override fun afterAll(context: ExtensionContext) {
        if (isNested(context)) {
            return
        }
        lsdContext.completeReport(context.displayName)
        lsdContext.createIndex()
        lsdContext.completeComponentsReport("Combined Component Diagram")
    }

    private fun isNested(context: ExtensionContext): Boolean {
        return context.parent
            .map { obj: ExtensionContext -> obj.parent }
            .map { obj: Optional<ExtensionContext> -> obj.isPresent }
            .orElse(false)
    }

    private fun prefixParentDisplayName(context: ExtensionContext): String {
        val parent = context.parent
        if (parent.isPresent) {
            val parentDisplayName = prefixParentDisplayName(parent.get())
            val separator = if (StringUtils.isBlank(parentDisplayName)) "" else ": "
            return parentDisplayName + separator + context.displayName.deCamelCase()
        }
        return ""
    }

    private fun createErrorDescription(cause: Throwable?, header: String): String {
        val contentId = idGenerator.next()
        val exceptionMessage = extractMessage(cause)
        return "<p>" +
                "<h4 class=\"error\">" + header + "</h4>" +
                popupHyperlink(
                    contentId, "Stacktrace",
                    "<pre>" + exceptionMessage!!.escapeHtml() + "</pre>",
                    "<pre><code>" + readStackTrace(cause) + "</code></pre>"
                ) + "</p>"
    }

    private fun additionalProcessing(instance: Any, annotation: Class<out Annotation?>) {
        var klass: Class<*> = instance.javaClass
        while (klass != Any::class.java) {
            Arrays.stream(klass.declaredMethods)
                .filter { method: Method -> method.isAnnotationPresent(annotation) }
                .forEach(invokeMethodOn(instance))
            klass = klass.superclass
        }
    }

    private fun extractMessage(cause: Throwable?): String? {
        return Optional.ofNullable(cause)
            .map { obj: Throwable -> obj.message }
            .orElse("")
    }

    private fun readStackTrace(cause: Throwable?): String {
        return Optional.ofNullable(cause)
            .filter(Predicate.not { hideStacktrace })
            .map { throwable: Throwable? -> throwable?.let { ExceptionUtils.readStackTrace(it) } }
            .orElse("[Displaying the stacktrace was disabled or no cause was provided]")
    }

    private fun invokeMethodOn(instance: Any): Consumer<Method> {
        return Consumer { method: Method ->
            try {
                method.isAccessible = true
                method.invoke(instance)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            }
        }
    }
}

fun String.deCamelCase(): String {
    return replace(Pattern.compile("([a-z])([A-Z])").toRegex(), "$1 $2")
        .replace(Pattern.compile("([A-Z])([a-z])").toRegex(), " $1$2")
        .replace("  ", " ")
        .replace(Pattern.compile("[()]").toRegex(), "")
        .trim()
        .lowercase()
}