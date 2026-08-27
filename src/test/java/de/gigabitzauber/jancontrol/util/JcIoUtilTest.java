package de.gigabitzauber.jancontrol.util;

import de.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;
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
    private Resource fileExampleResource;
    private Path dirExamplePath;

    @BeforeEach
    void setUp() {
        fileExamplePath = tempDir.resolve(this.getClass().getSimpleName() + ".file");
        fileExampleResource = new FileSystemResource(fileExamplePath);
        dirExamplePath = tempDir.resolve(this.getClass().getSimpleName() + "_dir");

        try {
            Files.createFile(fileExamplePath);
        } catch (IOException e) {
            Assertions.fail("Could not create temp file.", e);
        }

        try {
            Files.createDirectory(dirExamplePath);
        } catch (IOException e) {
            Assertions.fail("Could not create temp dir.", e);
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
    void test_read_from_path_happy_path() throws Exception {
        Files.writeString(fileExamplePath, VALUE_EXAMPLE);

        var actualValue = JcIoUtil.readString(fileExamplePath);

        assertThat(actualValue).isEqualTo(VALUE_EXAMPLE);
    }

    @Test
    void test_read_from_resource_happy_path() throws Exception {
        Files.writeString(fileExamplePath, VALUE_EXAMPLE);

        var actualValue = JcIoUtil.readString(fileExampleResource);

        assertThat(actualValue).isEqualTo(VALUE_EXAMPLE);
    }

    @Test
    void when_file_does_not_exist_then_read_from_path_throws_exception() {
        var fileDoesNotExist = Paths.get("fileDoesNotExist");

        assertThatThrownBy(() -> JcIoUtil.readString(fileDoesNotExist))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from file")
            .hasRootCauseInstanceOf(NoSuchFileException.class);
    }

    @Test
    void when_file_does_not_exist_then_read_from_resource_throws_exception() {
        var fileDoesNotExist = Paths.get("fileDoesNotExist");
        var localResourceExample = new FileSystemResource(fileDoesNotExist);

        assertThatThrownBy(() -> JcIoUtil.readString(localResourceExample))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from resource")
            .hasRootCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void when_file_is_a_directory_then_read_from_path_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.readString(tempDir))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from file")
            .hasRootCauseInstanceOf(IOException.class);
    }

    @Test
    void when_file_is_a_directory_then_read_from_resource_throws_exception() {
        var localResourceExample = new FileSystemResource(tempDir);
        assertThatThrownBy(() -> JcIoUtil.readString(localResourceExample))
            .isInstanceOf(JcException.class)
            .hasMessage("Could not read value from resource")
            .hasRootCauseInstanceOf(IOException.class);
    }

    @Test
    void when_path_is_null_then_read_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.readString((Path) null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @Test
    void when_resource_is_null_then_read_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.readString((Resource) null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("resource must not be null");
    }

    @ParameterizedTest
    @MethodSource("assertReadableFileFailCombinations")
    void test_assertReadableFile_failure_scenarios(boolean existsFlag,
                                                   boolean directoryFlag,
                                                   boolean readableFlag,
                                                   Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(fileExamplePath)).thenReturn(readableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertIsReadableFile(fileExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(fileExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertReadableDirFailCombinations")
    void test_assertReadableDir_failure_scenarios(boolean existsFlag,
                                                  boolean directoryFlag,
                                                  boolean readableFlag,
                                                  Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(dirExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(dirExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(dirExamplePath)).thenReturn(readableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertIsReadableDir(dirExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(dirExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertReadableFileSuccessCombinations")
    void test_assertReadableFile_success_scenarios(boolean existsFlag,
                                                   boolean directoryFlag,
                                                   boolean readableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(fileExamplePath)).thenReturn(readableFlag);
            var actualResultRef = new AtomicReference<Path>();
            assertThatNoException().isThrownBy(() -> actualResultRef.set(JcIoUtil.assertIsReadableFile(fileExamplePath)));
            assertThat(actualResultRef.get()).isEqualTo(fileExamplePath);
        }
    }

    @ParameterizedTest
    @MethodSource("assertReadableDirSuccessCombinations")
    void test_assertReadableDir_success_scenarios(boolean existsFlag,
                                                  boolean directoryFlag,
                                                  boolean readableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(dirExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(dirExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isReadable(dirExamplePath)).thenReturn(readableFlag);
            var actualResultRef = new AtomicReference<Path>();
            assertThatNoException().isThrownBy(() -> actualResultRef.set(JcIoUtil.assertIsReadableDir(dirExamplePath)));
            assertThat(actualResultRef.get()).isEqualTo(dirExamplePath);
        }
    }

    @Test
    void when_path_is_null_then_assertReadableFile_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertIsReadableFile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @Test
    void when_path_is_null_then_assertReadableDir_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertIsReadableDir(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @ParameterizedTest
    @MethodSource("assertWritableFileFailCombinations")
    void test_assertWritableFile_failure_scenarios(boolean existsFlag,
                                                   boolean directoryFlag,
                                                   boolean writeableFlag,
                                                   Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isWritable(fileExamplePath)).thenReturn(writeableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertIsWritableFile(fileExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(fileExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertWritableDirFailCombinations")
    void test_assertWritableDir_failure_scenarios(boolean existsFlag,
                                                  boolean directoryFlag,
                                                  boolean writeableFlag,
                                                  Function<Path, String> errorMsgFunc) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(dirExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(dirExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isWritable(dirExamplePath)).thenReturn(writeableFlag);
            assertThatThrownBy(() -> JcIoUtil.assertIsWritableDir(dirExamplePath))
                .isInstanceOf(JcException.class)
                .hasMessage(errorMsgFunc.apply(dirExamplePath))
                .hasNoCause();
        }
    }

    @ParameterizedTest
    @MethodSource("assertWritableFileSuccessCombinations")
    void test_assertWritableFile_success_scenarios(boolean existsFlag,
                                                   boolean directoryFlag,
                                                   boolean writeableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(fileExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(fileExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isWritable(fileExamplePath)).thenReturn(writeableFlag);
            var resultRef = new AtomicReference<Path>();
            assertThatNoException().isThrownBy(() -> resultRef.set(JcIoUtil.assertIsWritableFile(fileExamplePath)));
            assertThat(resultRef.get()).isEqualTo(fileExamplePath);
        }
    }

    @ParameterizedTest
    @MethodSource("assertWritableDirSuccessCombinations")
    void test_assertWritableDir_success_scenarios(boolean existsFlag,
                                                  boolean directoryFlag,
                                                  boolean writeableFlag) {
        try (var staticFilesMock = Mockito.mockStatic(Files.class)) {
            staticFilesMock.when(() -> Files.exists(dirExamplePath)).thenReturn(existsFlag);
            staticFilesMock.when(() -> Files.isDirectory(dirExamplePath)).thenReturn(directoryFlag);
            staticFilesMock.when(() -> Files.isWritable(dirExamplePath)).thenReturn(writeableFlag);
            var resultRef = new AtomicReference<Path>();
            assertThatNoException().isThrownBy(() -> resultRef.set(JcIoUtil.assertIsWritableDir(dirExamplePath)));
            assertThat(resultRef.get()).isEqualTo(dirExamplePath);
        }
    }

    @Test
    void when_path_is_null_then_assertWriteableFile_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertIsWritableFile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    @Test
    void when_path_is_null_then_assertWriteableDir_throws_exception() {
        assertThatThrownBy(() -> JcIoUtil.assertIsWritableDir(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("path must not be null");
    }

    // existsFlag, directoryFlag, readableFlag, errorMsgFunc
    private static Stream<Arguments> assertReadableFileFailCombinations() {
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

    // existsFlag, directoryFlag, readableFlag, errorMsgFunc
    private static Stream<Arguments> assertReadableDirFailCombinations() {
        Function<Path, String> pathDoesNotExistError = path -> "Path does not exist: " + path;
        Function<Path, String> pathIsNotADirError = path -> "Path is not a directory: " + path;
        Function<Path, String> pathIsNotReadableError = path -> "Path is not readable: " + path;
        return Stream.of(
            arguments(false, false, false, pathDoesNotExistError),
            arguments(false, false, true, pathDoesNotExistError),
            arguments(false, true, false, pathDoesNotExistError),
            arguments(false, true, true, pathDoesNotExistError),
            arguments(true, false, false, pathIsNotADirError),
            arguments(true, true, false, pathIsNotReadableError),
            arguments(true, false, true, pathIsNotADirError)
        );
    }

    // existsFlag, directoryFlag, readableFlag
    private static Stream<Arguments> assertReadableFileSuccessCombinations() {
        return Stream.of(
            arguments(true, false, true)
        );
    }

    // existsFlag, directoryFlag, readableFlag
    private static Stream<Arguments> assertReadableDirSuccessCombinations() {
        return Stream.of(
            arguments(true, true, true)
        );
    }

    // existsFlag, directoryFlag, writableFlag, errorMsgFunc
    private static Stream<Arguments> assertWritableFileFailCombinations() {
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

    // existsFlag, directoryFlag, writableFlag, errorMsgFunc
    private static Stream<Arguments> assertWritableDirFailCombinations() {
        Function<Path, String> pathDoesNotExistError = path -> "Path does not exist: " + path;
        Function<Path, String> pathIsNotADirError = path -> "Path is not a directory: " + path;
        Function<Path, String> pathIsNotWritableError = path -> "Path is not writable: " + path;
        return Stream.of(
            arguments(false, false, false, pathDoesNotExistError),
            arguments(false, false, true, pathDoesNotExistError),
            arguments(false, true, false, pathDoesNotExistError),
            arguments(false, true, true, pathDoesNotExistError),
            arguments(true, false, false, pathIsNotADirError),
            arguments(true, true, false, pathIsNotWritableError),
            arguments(true, false, true, pathIsNotADirError)
        );
    }

    // existsFlag, directoryFlag, writableFlag
    private static Stream<Arguments> assertWritableFileSuccessCombinations() {
        return Stream.of(
            arguments(true, false, true)
        );
    }

    // existsFlag, directoryFlag, writableFlag
    private static Stream<Arguments> assertWritableDirSuccessCombinations() {
        return Stream.of(
            arguments(true, true, true)
        );
    }
}
