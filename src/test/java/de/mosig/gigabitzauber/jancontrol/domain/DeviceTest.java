package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
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

    private final Device underTest = new Device(NAME_EXAMPLE, SYS_FS_PATH_EXAMPLE);

    @Test
    void test_getSetName() {
        var expectedName = "expectedName";
        underTest.setName(expectedName);

        assertThat(underTest.getName()).isEqualTo(expectedName);
    }

    @Test
    void test_getSetSysFsPath() {
        var expectedRawPath = "expectedRawPath";
        underTest.setSysPath(expectedRawPath);

        assertThat(underTest.getSysPath()).isEqualTo(expectedRawPath);
    }

    @Test
    void test_getSafeSysFsPath_happy_path() throws Exception {
        var tmpPath = tempDir.resolve(SYS_FS_PATH_EXAMPLE);
        Files.createFile(tmpPath);
        var rawTmpPath = tmpPath.toString();
        underTest.setSysPath(rawTmpPath);

        assertThat(underTest.safeSysPath()).isEqualTo(tmpPath);
    }

    @Test
    void when_sysFsPath_exists_but_is_not_a_file_then_throw_exception() {
        var tmpPath = tempDir.toString();
        underTest.setSysPath(tmpPath);

        assertThatThrownBy(underTest::safeSysPath)
            .isInstanceOf(JcException.class)
            .hasMessage("Sys fs path is not a file: " + tmpPath);
    }

    @Test
    void when_sysFsPath_does_not_exist_then_throw_exception() {
        var notFound = "not_found";
        underTest.setSysPath(notFound);

        assertThatThrownBy(underTest::safeSysPath)
            .isInstanceOf(JcException.class)
            .hasMessage("Could not find sys fs path: " + notFound);
    }
}
