package de.gigabitzauber.jancontrol.util;

import de.gigabitzauber.jancontrol.error.JcException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

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

    public static String readString(Resource resource) {
        requireNonNull(resource, "resource must not be null");
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new JcException("Could not read value from resource", e);
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

    public static Path assertIsReadableFile(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingFile(path);
        assertReadable(path);

        return path;
    }

    public static Path assertIsReadableDir(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingDir(path);
        assertReadable(path);

        return path;
    }

    public static Path assertIsWritableFile(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingFile(path);
        assertWritable(path);

        return path;
    }

    public static Path assertIsWritableDir(Path path) {
        requireNonNull(path, "path must not be null");

        assertExistingDir(path);
        assertWritable(path);

        return path;
    }

    private static void assertReadable(Path path) {
        if (!Files.isReadable(path)) {
            throw new JcException("Path is not readable: " + path);
        }
    }

    private static void assertWritable(Path path) {
        if (!Files.isWritable(path)) {
            throw new JcException("Path is not writable: " + path);
        }
    }

    private static void assertExists(Path path) {
        if (!Files.exists(path)) {
            throw new JcException("Path does not exist: " + path);
        }
    }

    private static void assertExistingFile(Path path) {
        assertExists(path);
        if (Files.isDirectory(path)) {
            throw new JcException("Path is not a file: " + path);
        }
    }

    private static void assertExistingDir(Path path) {
        assertExists(path);
        if (!Files.isDirectory(path)) {
            throw new JcException("Path is not a directory: " + path);
        }
    }

    public static Path findHwmonDir(Path hwmonClassDir, String sysName) {
        try (var hwmonDirStream = Files.find(hwmonClassDir, 1,
            (path, attr) ->
                attr.isDirectory() && Optional.ofNullable(path.getFileName()).orElse(Path.of("nullPath")).toString().startsWith("hwmon"),
            FileVisitOption.FOLLOW_LINKS)) {
            return hwmonDirStream.filter(path -> {
                    var nameFile = path.resolve("name");
                    if (Files.isReadable(nameFile)) {
                        try {
                            return StringUtils.stripToEmpty(Files.readString(nameFile)).equals(sysName);
                        } catch (IOException e) {
                            handleResolveException(e);
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new JcException("Could not find HWMON dir for sysName: " + sysName));
        } catch (IOException e) {
            handleResolveException(e);
        }

        throw new JcException("Implementation error: This location should have not been reached.");
    }

    private static void handleResolveException(Exception e) {
        var msg = JcErrorUtil.safeGetMessage(e);
        throw new JcException("Error while resolving device: " + msg, e);
    }
}
