package de.gigabitzauber.jancontrol.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JcErrorUtilTest {

    @ParameterizedTest
    @MethodSource("safeGetMessageCombinations")
    void test_safeGetMessage_combinations(String msgCandidate, String expectedMessage) {
        var throwableExample = msgCandidate == null ? new IOException() : new IOException(msgCandidate);

        var actualMessage = JcErrorUtil.safeGetMessage(throwableExample);

        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @Test
    void safeGetMessage_does_not_accept_null() {
        assertThatThrownBy(() -> JcErrorUtil.safeGetMessage(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("t must not be null");
    }

    private static Stream<Arguments> safeGetMessageCombinations() {
        var noFurtherInformation = "No further information";
        String aMessage = "A message";
        return Stream.of(
            arguments(null, noFurtherInformation),
            arguments("", noFurtherInformation),
            arguments("   ", noFurtherInformation),
            arguments(aMessage, aMessage),
            arguments(" A message\n ", aMessage));
    }
}
