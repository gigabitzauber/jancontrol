package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReadOnlyDeviceTest {
    private static final String NAME_EXAMPLE = "readOnlyDeviceExample";
    private static final int OFFSET_EXAMPLE = 11;

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
    void when_constructed_with_all_args_then_properties_are_set() {
        var underTest = createUnderTest(sysFileExample);

        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(underTest.getSysPath()).isEqualTo(sysFileExample);
        assertThat(underTest.getOffset()).isEqualTo(OFFSET_EXAMPLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123000", "  123000   ", "\t 123000\n  ", "123000\n"})
    void test_read_happy_path(String rawValueCandidate) throws Exception {
        Files.writeString(sysFileExamplePath, rawValueCandidate);
        var underTest = createUnderTest(sysFileExample);

        var actualValue = underTest.read();

        assertThat(actualValue).isEqualTo(123 + OFFSET_EXAMPLE);
    }

    @Test
    void when_read_contents_are_not_a_number_then_throw_jc_exception() throws Exception {
        Files.writeString(sysFileExamplePath, "not a number");
        var underTest = createUnderTest(sysFileExample);

        assertThatThrownBy(underTest::read)
            .isInstanceOf(JcException.class)
            .hasMessage("Value of device '" + NAME_EXAMPLE + "' is not a number.");
    }

    @Test
    void when_file_does_not_exist_then_throw_exception() {
        String fileDoesNotExist = "fileDoesNotExist";
        var underTest = createUnderTest(fileDoesNotExist);

        assertThatThrownBy(underTest::read)
            .isInstanceOf(JcException.class)
            .hasMessage("Could not find sys fs path: " + fileDoesNotExist);
    }

    @Test
    void when_file_is_a_directory_then_throw_exception() {
        var underTest = createUnderTest(tempDir.toString());

        assertThatThrownBy(underTest::read)
            .isInstanceOf(JcException.class)
            .hasMessage("Sys fs path is not a file: " + tempDir);
    }

    @Test
    void when_reading_fails_then_throw_exception() {
        var underTest = createUnderTest(sysFileExample);

        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            var expectedException = new IOException("expected exception");
            staticFilesMock.when(() -> Files.readString(sysFileExamplePath)).thenThrow(expectedException);
            staticFilesMock.when(() -> Files.exists(sysFileExamplePath)).thenCallRealMethod();
            staticFilesMock.when(() -> Files.isDirectory(sysFileExamplePath)).thenCallRealMethod();
            assertThatThrownBy(underTest::read)
                .isInstanceOf(JcException.class)
                .hasMessage("Could not read value of device '" + NAME_EXAMPLE + "'")
                .hasRootCause(expectedException);
        }
    }

    private ReadOnlyDevice createUnderTest(String devicePath) {
        return new ReadOnlyDevice(NAME_EXAMPLE, devicePath, OFFSET_EXAMPLE);
    }
}
