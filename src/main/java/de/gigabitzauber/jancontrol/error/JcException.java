package de.gigabitzauber.jancontrol.error;

import java.util.Objects;

public class JcException extends RuntimeException {
    public JcException() {
        super("Unexpected error");
    }

    public JcException(String msg) {
        super(Objects.requireNonNull(msg, "message must not be null"));
    }

    public JcException(String msg, final Throwable cause) {
        super(Objects.requireNonNull(msg, "message must not be null"),
            Objects.requireNonNull(cause, "cause must not be null"));
    }
}
