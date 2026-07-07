package de.mosig.gigabitzauber.jancontrol.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

class JcSpringConfigTest {

    private final JcSpringConfig underTest = new JcSpringConfig();

    @Test
    void test_fanCruiseExecutor_isSingleton() {
        assertMethodProducesSingleton("fanCruiseExecutor", SCOPE_SINGLETON);
    }

    @Test
    void test_logger_isPrototype() {
        assertMethodProducesSingleton("log", SCOPE_PROTOTYPE);
    }

    @Test
    void test_logger_is_attached_to_expected_class() {
        var expectedClass = this.getClass();
        var memberMock = mock(Member.class);
        var injectionPointMock = mock(InjectionPoint.class);
        when(injectionPointMock.getMember()).thenReturn(memberMock);
        when(memberMock.getDeclaringClass()).thenAnswer(_ -> expectedClass);

        try (var staticLoggerMock = mockStatic(LoggerFactory.class)) {
            underTest.log(injectionPointMock);
            verify(injectionPointMock).getMember();
            verify(memberMock).getDeclaringClass();
            staticLoggerMock.verify(() -> LoggerFactory.getLogger(expectedClass));
        }

    }

    private void assertMethodProducesSingleton(String methodName, String expectedScopeName) {
        var method = findMethod(methodName);
        var scopeAnnotation = method.getAnnotation(Scope.class);
        assertThat(scopeAnnotation).isNotNull();
        assertThat(scopeAnnotation.value()).isEqualTo(expectedScopeName);
    }

    private Method findMethod(String name) {
        return Arrays.stream(underTest.getClass().getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .findFirst()
            .orElseGet(() -> {
                Assertions.fail("Could not find method " + name);
                return null;
            });
    }
}
