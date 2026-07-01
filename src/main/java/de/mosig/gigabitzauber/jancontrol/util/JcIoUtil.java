package de.mosig.gigabitzauber.jancontrol.util;

import de.mosig.gigabitzauber.jancontrol.error.JcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.util.Objects.requireNonNull;

public final class JcIoUtil {
    public static String readString(Path path) {
        requireNonNull(path, "path must not be null");
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new JcException("Could not read value from file", e);
        }
    }

    public static void writeString(Path path, String value) {
        requireNonNull(path, "path must not be null");
        requireNonNull(value, "rawValue must not be null");
        try {
            Files.writeString(path, value,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new JcException("Could not write to file", e);
        }
    }
}
