package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTest {
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
        underTest = new Device(rawSysFsPath) {
        };
    }

    @Test
    void test_getSysFsPath() {
        assertThat(underTest.getSysPath()).isEqualTo(rawSysFsPath);
    }

    @Test
    void test_safeReadableSysPath_happy_path() {
        assertThat(underTest.safeReadableSysPath()).isEqualTo(sysFsPath);
    }

    @Test
    void test_safeWritableSysPath_happy_path() {
        assertThat(underTest.safeWritableSysPath()).isEqualTo(sysFsPath);
    }

    @Test
    void test_safeReadableSysPath_calls_assertReadable() {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            underTest.safeReadableSysPath();

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertReadable(sysFsPath));
        }
    }

    @Test
    void test_safeWritableSysPath_calls_assertReadable() {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            underTest.safeWritableSysPath();

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertWritable(sysFsPath));
        }
    }
}
