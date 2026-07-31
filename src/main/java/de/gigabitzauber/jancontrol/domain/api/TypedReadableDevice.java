package de.gigabitzauber.jancontrol.domain.api;

/**
 * @param <T> - Type of read value
 */
@FunctionalInterface
public interface TypedReadableDevice<T> {
    T read();
}
