package de.gigabitzauber.jancontrol.cruise;

public record JcOp(Object parent, String name, Runnable code) {
    public JcOp(String name, Runnable code) {
        this(null, name, code);
    }
}
