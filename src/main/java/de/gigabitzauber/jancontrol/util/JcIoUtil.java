package de.gigabitzauber.jancontrol.util;

import de.gigabitzauber.jancontrol.error.JcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.util.Objects.requireNonNull;

public final class JcIoUtil {

    private JcIoUtil() {
    }

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
        requireNonNull(value, "value must not be null");
        try {
            Files.writeString(path, value,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC);
        } catch (IOException e) {
            throw new JcException("Could not write to file", e);
        }
    }

    public static Path assertReadable(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingFile(path);

        if (!Files.isReadable(path)) {
            throw new JcException("Path is not readable: " + path);
        }

        return path;
    }

    public static Path assertWritable(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingFile(path);

        if (!Files.isWritable(path)) {
            throw new JcException("Path is not writable: " + path);
        }

        return path;
    }

    private static void assertExistingFile(Path path) {
        if (!Files.exists(path)) {
            throw new JcException("Path does not exist: " + path);
        } else if (Files.isDirectory(path)) {
            throw new JcException("Path is not a file: " + path);
        }
    }
}
