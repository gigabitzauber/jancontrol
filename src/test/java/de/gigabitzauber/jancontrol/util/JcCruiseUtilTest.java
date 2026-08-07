package de.gigabitzauber.jancontrol.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JcCruiseUtilTest {

    @Test
    void must_be_tool_class() {
        var modifiers = JcCruiseUtil.class.getModifiers();

        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isFinal(modifiers)).isTrue();

        var constructors = JcCruiseUtil.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterCount()).isZero();
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
    }

    @ParameterizedTest
    @MethodSource("nearestMultipleExamples")
    void when_get_nearest_multiple_then_rounds_to_expected_value(int input, int multiplier, int expectedResult) {
        var localUnderTest = JcCruiseUtil.getNearestMultiple(input, multiplier);

        assertThat(localUnderTest).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> nearestMultipleExamples() {
        return Stream.of(
            arguments(12, 5, 10),
            arguments(13, 5, 15),
            arguments(20, 5, 20),
            arguments(23, 3, 24)
        );
    }
}
