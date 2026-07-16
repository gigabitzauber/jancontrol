package de.gigabitzauber.jancontrol.error;

import de.gigabitzauber.jancontrol.cruise.JcSchedulable;
import lombok.Getter;

public final class JcSchedulableException extends JcException {

    @Getter
    private final JcSchedulable parent;

    public JcSchedulableException(String msg, JcSchedulable parent) {
        super(msg);
        this.parent = parent;
    }

    public JcSchedulableException(String msg, JcSchedulable parent, Throwable cause) {
        super(msg, cause);
        this.parent = parent;
    }
}
