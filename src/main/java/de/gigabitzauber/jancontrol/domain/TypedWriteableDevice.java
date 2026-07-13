package de.gigabitzauber.jancontrol.domain;

/**
 * @param <T> - Type of value to write
 */
@FunctionalInterface
public interface TypedWriteableDevice<T> {
    void write(T value);
}
