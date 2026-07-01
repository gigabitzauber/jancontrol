package de.mosig.gigabitzauber.jancontrol.domain;

/**
 * @param <T> - Type of read value
 */
@FunctionalInterface
public interface TypedReadableDevice<T> {
    T read();
}
