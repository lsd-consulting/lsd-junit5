package lsd.junit5;

import com.lsd.LsdContext;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Arrays.stream;

public class LsdExtension implements TestWatcher, AfterTestExecutionCallback, AfterAllCallback {

    private final LsdContext lsdContext = LsdContext.getInstance();

    private String description = "";

    @Override
    public void testSuccessful(ExtensionContext context) {
        lsdContext.completeScenario(context.getDisplayName(), description.concat("Passed!"));
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        lsdContext.completeScenario(context.getDisplayName(), description.concat("Disabled!"));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        lsdContext.completeScenario(context.getDisplayName(), description.concat("Aborted!"));
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        lsdContext.completeScenario(context.getDisplayName(), description.concat("Failed!"));
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