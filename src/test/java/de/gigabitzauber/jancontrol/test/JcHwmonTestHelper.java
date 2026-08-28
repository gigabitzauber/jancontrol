package de.gigabitzauber.jancontrol.test;

import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class JcHwmonTestHelper {
    private JcHwmonTestHelper() {
    }

    public static Path createHwmonClassDir(Path testTempRoot, String... names) {
        var result = testTempRoot.resolve("sysClassHwmon");
        safeCreateTestDir(result);

        for (int i = 0; i < names.length; i++) {
            var currentHwmonDir = result.resolve("hwmon" + i);
            safeCreateTestDir(currentHwmonDir);
            var nameFile = currentHwmonDir.resolve("name");
            safeWrite(nameFile, names[i]);
        }

        return result;
    }

    public static void safeWrite(Path nameFile, String data) {
        try {
            Files.writeString(nameFile, data, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            Assertions.fail("Could not write test data.", e);
        }
    }

    private static void safeCreateTestDir(Path result) {
        try {
            Files.createDirectories(result);
        } catch (IOException e) {
            Assertions.fail("Could not create temp dir.", e);
        }
    }
}
