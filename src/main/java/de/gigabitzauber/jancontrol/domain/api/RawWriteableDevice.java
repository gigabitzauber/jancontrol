package de.gigabitzauber.jancontrol.domain.api;

@FunctionalInterface
public interface RawWriteableDevice {
    void writeRaw(String rawValue);
}
