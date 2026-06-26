package de.mosig.gigabitzauber.jancontrol.error;

public final class JcException extends RuntimeException {
    public JcException(String msg) {
        super(msg);
    }
    
    public JcException(String msg, final Throwable cause) {
        super(msg, cause);
    }
}
