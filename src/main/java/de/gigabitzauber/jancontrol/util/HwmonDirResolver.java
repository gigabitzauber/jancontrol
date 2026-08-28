package de.gigabitzauber.jancontrol.util;

import de.gigabitzauber.jancontrol.error.JcException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class HwmonDirResolver implements Serializable {
    private final String rootDir;

    public HwmonDirResolver(Path rootDir) {
        this.rootDir = JcIoUtil.assertIsReadableDir(rootDir).toString();
    }

    public Path findHwmonDir(String sysName) {
        requireNonNull(sysName, "sysName must not be null");

        var localRootDir = Path.of(this.rootDir);
        try (var hwmonDirStream = Files.find(localRootDir, 1,
            (path, attr) ->
                attr.isDirectory() && Optional.ofNullable(path.getFileName()).orElse(Path.of("nullPath")).toString().startsWith("hwmon"),
            FileVisitOption.FOLLOW_LINKS)) {
            return hwmonDirStream.filter(path -> {
                    var nameFile = path.resolve("name");
                    if (Files.isReadable(nameFile)) {
                        try {
                            return StringUtils.stripToEmpty(Files.readString(nameFile)).equals(sysName);
                        } catch (IOException e) {
                            handleResolveProblem(e);
                        }
                    }
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new JcException("Could not find HWMON dir for sysName: " + sysName));
        } catch (IOException e) {
            handleResolveProblem(e);
        }

        throw new JcException("Implementation error: This location should have not been reached.");
    }

    private static void handleResolveProblem(Exception e) {
        var msg = JcErrorUtil.safeGetMessage(e);
        throw new JcException("Error while resolving hwmon dir: " + msg, e);
    }
}
