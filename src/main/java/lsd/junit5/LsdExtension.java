package lsd.junit5;

import com.lsd.IdGenerator;
import com.lsd.LsdContext;
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

import static com.lsd.report.model.PopupContent.popupDiv;
import static j2html.TagCreator.*;
import static java.util.Arrays.stream;

public class LsdExtension implements TestWatcher, AfterTestExecutionCallback, AfterAllCallback {

    private final LsdContext lsdContext = LsdContext.getInstance();
    private final IdGenerator idGenerator = lsdContext.getIdGenerator();

    @Override
    public void testSuccessful(ExtensionContext context) {
        lsdContext.completeScenario(context.getDisplayName(), p(
                h4().with(new UnescapedText("&#127808; Test Passed")).withClass("info")
        ).render());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        lsdContext.completeScenario(context.getDisplayName(), p(
                h4("Test Disabled").withClass("warn")
        ).render());
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        var description = createErrorDescription(cause, "Test Aborted!");
        lsdContext.completeScenario(context.getDisplayName(), description);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        var description = createErrorDescription(cause, "&#10060; Failed!");
        lsdContext.completeScenario(context.getDisplayName(), description);
    }

    private String createErrorDescription(Throwable cause, String header) {
        var contentId = idGenerator.next();
        return p(
                h4().with(new UnescapedText(header)).withClass("error"),
                a().withHref("#" + contentId).withText(extractMessage(cause)),
                popupDiv(contentId, "Stacktrace", readStackTrace(cause))
        ).render();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        lsdContext.completeReport(context.getDisplayName());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Object testInstance = context.getRequiredTestInstance();
        additionalProcessing(testInstance, LsdPostTestProcessing.class);
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
                .map(ExceptionUtils::readStackTrace)
                .orElse("");
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