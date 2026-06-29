package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceTest {
    private static final String NAME_EXAMPLE = "nameExample";
    private static final String SYS_FS_PATH_EXAMPLE = "sysFsPathExample";

    @TempDir
    private Path tempDir;
    private String rawSysFsPath;
    private Path sysFsPath;

    private Device underTest;

    @BeforeEach
    void setUp() throws Exception {
        sysFsPath = tempDir.resolve(SYS_FS_PATH_EXAMPLE);
        Files.createFile(sysFsPath);
        rawSysFsPath = sysFsPath.toString();
        underTest = new Device(NAME_EXAMPLE, rawSysFsPath) {
        };
    }

    @Test
    void test_getName() {
        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
    }

    @Test
    void test_getSysFsPath() {
        assertThat(underTest.getSysPath()).isEqualTo(rawSysFsPath);
    }

    @Test
    void test_getSafeSysFsPath_happy_path() throws Exception {
        assertThat(underTest.safeSysPath()).isEqualTo(sysFsPath);
    }

    @Test
    void when_sysFsPath_exists_but_is_not_a_file_then_throw_exception() {
        var tmpPath = tempDir.toString();
        var localUnderTest = new Device(NAME_EXAMPLE, tmpPath) {
        };

        assertThatThrownBy(localUnderTest::safeSysPath)
            .isInstanceOf(JcException.class)
            .hasMessage("Sys fs path is not a file: " + tmpPath);
    }

    @Test
    void when_sysFsPath_does_not_exist_then_throw_exception() {
        var notFound = "not_found";
        var localUnderTest = new Device(NAME_EXAMPLE, notFound) {
        };

        assertThatThrownBy(localUnderTest::safeSysPath)
            .isInstanceOf(JcException.class)
            .hasMessage("Could not find sys fs path: " + notFound);
    }
}
