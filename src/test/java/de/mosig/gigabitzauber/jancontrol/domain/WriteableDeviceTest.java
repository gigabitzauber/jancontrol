package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WriteableDeviceTest {
    private static final String NAME_EXAMPLE = "readOnlyDeviceExample";
    private static final int VALUE_EXAMPLE = 123;

    @TempDir
    private Path tempDir;
    private String sysFileExample;
    private Path sysFileExamplePath;

    @BeforeEach
    void setUp() {
        sysFileExamplePath = tempDir.resolve(NAME_EXAMPLE);
        sysFileExample = sysFileExamplePath.toString();
        try {
            Files.createFile(sysFileExamplePath);
        } catch (IOException e) {
            Assertions.fail("Could not create temp device file.", e);
        }
    }

    @Test
    void when_constructed_with_name_and_sys_path_then_properties_are_set() {
        var underTest = createUnderTest(sysFileExample);

        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(underTest.getSysPath()).isEqualTo(sysFileExample);
    }

    @Test
    void test_write_happy_path() throws Exception {
        var underTest = createUnderTest(sysFileExample);

        underTest.write(VALUE_EXAMPLE);

        var actualValue = Integer.parseInt(Files.readString(sysFileExamplePath));
        assertThat(actualValue).isEqualTo(VALUE_EXAMPLE);
    }

    @Test
    void when_file_does_not_exist_then_throw_exception() {
        String fileDoesNotExist = "fileDoesNotExist";
        var underTest = createUnderTest(fileDoesNotExist);

        assertThatThrownBy(() -> underTest.write(VALUE_EXAMPLE))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not find sys fs path: " + fileDoesNotExist);
    }

    @Test
    void when_file_is_a_directory_then_throw_exception() {
        var underTest = createUnderTest(tempDir.toString());

        assertThatThrownBy(() -> underTest.write(VALUE_EXAMPLE))
            .isInstanceOf(JcException.class)
            .hasMessage("Sys fs path is not a file: " + tempDir);
    }

    @Test
    void when_writing_fails_then_throw_exception() {
        var underTest = createUnderTest(sysFileExample);

        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            var expectedException = new IOException("expected exception");
            staticFilesMock.when(() -> Files.writeString(sysFileExamplePath, VALUE_EXAMPLE + "",
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC)).thenThrow(expectedException);
            staticFilesMock.when(() -> Files.exists(sysFileExamplePath)).thenCallRealMethod();
            staticFilesMock.when(() -> Files.isDirectory(sysFileExamplePath)).thenCallRealMethod();
            assertThatThrownBy(() -> underTest.write(VALUE_EXAMPLE))
                .isInstanceOf(JcException.class)
                .hasMessage("Could not write to device " + NAME_EXAMPLE)
                .hasRootCause(expectedException);
        }
    }

    private WriteableDevice createUnderTest(String devicePath) {
        return new WriteableDevice(NAME_EXAMPLE, devicePath);
    }
}
