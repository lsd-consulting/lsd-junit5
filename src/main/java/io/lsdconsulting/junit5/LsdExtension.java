package io.lsdconsulting.junit5;

import com.lsd.core.IdGenerator;
import com.lsd.core.LsdContext;
import com.lsd.core.properties.LsdProperties;
import j2html.tags.UnescapedText;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.util.ExceptionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import static com.lsd.core.domain.Status.*;
import static com.lsd.core.report.PopupContent.popupDiv;
import static j2html.TagCreator.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static org.junit.platform.commons.util.StringUtils.isBlank;

/**
 * Junit 5 extension to create LSD reports based on test results.
 * (Not that this extension does not capture any events for the LSD diagrams)
 */
public class LsdExtension implements TestWatcher, AfterTestExecutionCallback, AfterAllCallback {

    private final LsdContext lsdContext = LsdContext.getInstance();
    private final IdGenerator idGenerator = lsdContext.getIdGenerator();
    private final boolean hideStacktrace = LsdProperties.getBoolean("lsd.junit5.hideStacktrace");

    @Override
    public void testSuccessful(ExtensionContext context) {
        lsdContext.completeScenario(prefixParentDisplayName(context), p(
                h4().with(new UnescapedText("&#127808; Test Passed")).withClass("success")
        ).render(), SUCCESS);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        lsdContext.completeScenario(prefixParentDisplayName(context), p(
                h4("Test Disabled").withClass("warn")
        ).render(), FAILURE);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        var description = createErrorDescription(cause, "Test Aborted!");
        lsdContext.completeScenario(prefixParentDisplayName(context), description, FAILURE);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        var description = createErrorDescription(cause, "&#10060; Failed!");
        lsdContext.completeScenario(prefixParentDisplayName(context), description, ERROR);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Object testInstance = context.getRequiredTestInstance();
        additionalProcessing(testInstance, LsdPostTestProcessing.class);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (isNested(context)) {
            return;
        }
        lsdContext.completeReport(context.getDisplayName());
        lsdContext.createIndex();
    }

    private boolean isNested(ExtensionContext context) {
        return context.getParent()
                .map(ExtensionContext::getParent)
                .map(Optional::isPresent)
                .orElse(false);
    }

    private String prefixParentDisplayName(ExtensionContext context) {
        var parent = context.getParent();
        if (parent.isPresent()) {
            var parentDisplayName = prefixParentDisplayName(parent.get());
            var separator = isBlank(parentDisplayName) ? "" : ": ";
            return parentDisplayName + separator + context.getDisplayName();
        }
        return "";
    }

    private String createErrorDescription(Throwable cause, String header) {
        var contentId = idGenerator.next();
        String exceptionMessage = extractMessage(cause);
        return p(
                h4().with(new UnescapedText(header)).withClass("error"),
                a().withHref("#" + contentId).withText(exceptionMessage),
                popupDiv(contentId, "Stacktrace", readStackTrace(cause))
        ).render();
    }

    private void additionalProcessing(final Object instance, Class<? extends Annotation> annotation) {
        Class<?> klass = instance.getClass();
        while (klass != Object.class) {
            stream(klass.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(annotation))
                    .forEach(invokeMethodOn(instance));
            klass = klass.getSuperclass();
        }
    }

    private String extractMessage(Throwable cause) {
        return Optional.ofNullable(cause)
                .map(Throwable::getMessage)
                .orElse("");
    }

    private String readStackTrace(Throwable cause) {
        return Optional.ofNullable(cause)
                .filter(not(throwable -> hideStacktrace))
                .map(ExceptionUtils::readStackTrace)
                .orElse("[Displaying the stacktrace was disabled or no cause was provided]");
    }

    private Consumer<Method> invokeMethodOn(Object instance) {
        return method -> {
            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }
}