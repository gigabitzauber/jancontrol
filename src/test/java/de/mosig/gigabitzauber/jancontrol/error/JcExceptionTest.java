package de.mosig.gigabitzauber.jancontrol.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JcExceptionTest {

    private static final IllegalArgumentException CAUSE_EXAMPLE = new IllegalArgumentException("cause message");

    @Test
    void when_constructed_with_message_then_message_is_available() {
        var message = "expected message";

        var exception = new JcException(message);

        assertThat(exception).hasMessage(message);
    }

    @Test
    void msg_constructor_does_not_support_null_message() {
        // False positive
        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> new JcException(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("message must not be null");
    }

    @Test
    void msg_cause_constructor_does_not_support_null_message() {
        // False positive
        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> new JcException(null, CAUSE_EXAMPLE))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("message must not be null");
    }

    @Test
    void msg_cause_constructor_does_not_support_null_cause() {
        // False positive
        //noinspection ThrowableNotThrown
        assertThatThrownBy(() -> new JcException("msg", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("cause must not be null");
    }


    @Test
    void when_constructed_with_message_and_cause_then_cause_is_available() {
        var message = "expected message";

        var exception = new JcException(message, CAUSE_EXAMPLE);

        assertThat(exception).hasMessage(message);
        assertThat(exception).hasCause(CAUSE_EXAMPLE);
    }

    @Test
    void test_JcException_is_a_runtimeException() {
        assertThat(new JcException()).isInstanceOf(RuntimeException.class);
    }

    @Test
    void no_args_constructor_should_set_default_message() {
        var localUnderTest = new JcException();
        assertThat(localUnderTest.getMessage()).isEqualTo("Unexpected error");
    }
}
