package de.mosig.gigabitzauber.jancontrol.util;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JcIoUtilTest {

    private static final String VALUE_EXAMPLE = "valueExample";

    @TempDir
    private Path tempDir;
    private Path fileExamplePath;

    @BeforeEach
    void setUp() {
        fileExamplePath = tempDir.resolve(this.getClass().getSimpleName() + ".file");
        try {
            Files.createFile(fileExamplePath);
        } catch (IOException e) {
            Assertions.fail("Could not create temp file.", e);
        }
    }

    @Test
    void must_be_tool_class() {
        var modifiers = JcIoUtil.class.getModifiers();

        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isFinal(modifiers)).isTrue();

        var constructors = JcIoUtil.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterCount()).isZero();
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
    }

    @Test
    void test_write_happy_path() throws Exception {
        JcIoUtil.writeString(fileExamplePath, VALUE_EXAMPLE);

        var actualValue = Files.readString(fileExamplePath);
        assertThat(actualValue).isEqualTo(VALUE_EXAMPLE);
    }

    @Test
    void when_file_does_not_exist_then_write_throws_exception() {
        var fileDoesNotExist = Paths.get("fileDoesNotExist");

        assertThatThrownBy(() -> JcIoUtil.writeString(fileDoesNotExist, VALUE_EXAMPLE))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not write to file")
            .hasRootCauseInstanceOf(FileSystemException.class);
    }

    @Test
    void when_file_is_a_directory_then_write_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.writeString(tempDir, VALUE_EXAMPLE))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not write to file")
            .hasRootCauseInstanceOf(FileSystemException.class);
    }

    @Test
    void should_write_with_correct_options() {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            JcIoUtil.writeString(fileExamplePath, VALUE_EXAMPLE);
            staticFilesMock.verify(() -> Files.writeString(fileExamplePath, VALUE_EXAMPLE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.SYNC));
        }
    }

    @Test
    void when_path_is_null_then_write_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.writeString(null, VALUE_EXAMPLE))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @Test
    void when_value_is_null_then_write_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.writeString(fileExamplePath, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void test_read_happy_path() throws Exception {
        Files.writeString(fileExamplePath, VALUE_EXAMPLE);

        var actualValue = JcIoUtil.readString(fileExamplePath);

        assertThat(actualValue).isEqualTo(VALUE_EXAMPLE);
    }

    @Test
    void when_file_does_not_exist_then_read_throws_exception() {
        var fileDoesNotExist = Paths.get("fileDoesNotExist");

        assertThatThrownBy(() -> JcIoUtil.readString(fileDoesNotExist))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from file")
            .hasRootCauseInstanceOf(FileSystemException.class);
    }

    @Test
    void when_file_is_a_directory_then_read_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.readString(tempDir))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from file")
            .hasRootCauseInstanceOf(IOException.class);
    }

    @Test
    void when_path_is_null_then_read_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.readString(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @ParameterizedTest
    @MethodSource("assertReadableFailCombinations")
    void test_assertReadable_failure_scenarios(boolean existsFlag,
                                               boolean directoryFlag,
                                               boolean readableFlag,
                                               Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(fileExamplePath)).thenReturn(readableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertReadable(fileExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(fileExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertReadableSuccessCombinations")
    void test_assertReadable_success_scenarios(boolean existsFlag,
                                               boolean directoryFlag,
                                               boolean readableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(fileExamplePath)).thenReturn(readableFlag);
            assertThatNoException().isThrownBy(() -> JcIoUtil.assertReadable(fileExamplePath));
        }
    }

    @Test
    void when_path_is_null_then_assertReadable_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertReadable(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @ParameterizedTest
    @MethodSource("assertWritableFailCombinations")
    void test_assertWritable_failure_scenarios(boolean existsFlag,
                                               boolean directoryFlag,
                                               boolean writeableFlag,
                                               Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(fileExamplePath)).thenReturn(writeableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertWritable(fileExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(fileExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertWritableSuccessCombinations")
    void test_assertWritable_success_scenarios(boolean existsFlag,
                                               boolean directoryFlag,
                                               boolean writeableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isWritable(fileExamplePath)).thenReturn(writeableFlag);
            assertThatNoException().isThrownBy(() -> JcIoUtil.assertWritable(fileExamplePath));
        }
    }

    @Test
    void when_path_is_null_then_assertWriteable_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertWritable(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    // existsFlag, directoryFlag, readableFlag, errorMsgFunc
    private static Stream<Arguments> assertReadableFailCombinations() {
        Function<Path, String> pathDoesNotExistError = path -> "Path does not exist: " + path;
        Function<Path, String> pathIsNotAFileError = path -> "Path is not a file: " + path;
        Function<Path, String> pathIsNotReadableError = path -> "Path is not readable: " + path;
        return Stream.of(
            arguments(false, false, false, pathDoesNotExistError),
            arguments(false, false, true, pathDoesNotExistError),
            arguments(false, true, false, pathDoesNotExistError),
            arguments(false, true, true, pathDoesNotExistError),
            arguments(true, false, false, pathIsNotReadableError),
            arguments(true, true, false, pathIsNotAFileError),
            arguments(true, true, true, pathIsNotAFileError)
        );
    }

    // existsFlag, directoryFlag, readableFlag
    private static Stream<Arguments> assertReadableSuccessCombinations() {
        return Stream.of(
            arguments(true, false, true)
        );
    }

    // existsFlag, directoryFlag, readableFlag, errorMsgFunc
    private static Stream<Arguments> assertWritableFailCombinations() {
        Function<Path, String> pathDoesNotExistError = path -> "Path does not exist: " + path;
        Function<Path, String> pathIsNotAFileError = path -> "Path is not a file: " + path;
        Function<Path, String> pathIsNotWritableError = path -> "Path is not writable: " + path;
        return Stream.of(
            arguments(false, false, false, pathDoesNotExistError),
            arguments(false, false, true, pathDoesNotExistError),
            arguments(false, true, false, pathDoesNotExistError),
            arguments(false, true, true, pathDoesNotExistError),
            arguments(true, false, false, pathIsNotWritableError),
            arguments(true, true, false, pathIsNotAFileError),
            arguments(true, true, true, pathIsNotAFileError)
        );
    }

    // existsFlag, directoryFlag, readableFlag
    private static Stream<Arguments> assertWritableSuccessCombinations() {
        return Stream.of(
            arguments(true, false, true)
        );
    }
}
