package de.mosig.gigabitzauber.jancontrol.domain;

@FunctionalInterface
public interface RawWriteableDevice {
    void writeRaw(String rawValue);
}
