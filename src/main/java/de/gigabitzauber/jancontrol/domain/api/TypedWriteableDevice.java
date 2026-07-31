package de.gigabitzauber.jancontrol.domain.api;

/**
 * @param <T> - Type of value to write
 */
@FunctionalInterface
public interface TypedWriteableDevice<T> {
    T write(T value);
}
