package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class RpmDeviceTest {
    private static final String NAME_EXAMPLE = "readOnlyDeviceExample";
    private static final String SYS_FILE_EXAMPLE = "sysFileExample";
    private static final Path SYS_FILE_PATH_EXAMPLE = Paths.get(SYS_FILE_EXAMPLE);

    private final RpmDevice underTest = new RpmDevice(NAME_EXAMPLE, SYS_FILE_EXAMPLE);

    @Test
    void should_inherit_from_proper_parents() {
        assertThat(this.underTest).isInstanceOf(NamedDevice.class);
        assertThat(this.underTest).isInstanceOf(TypedReadableDevice.class);
        assertThat(this.underTest).isInstanceOf(TypedWriteableDevice.class);
    }

    @Test
    void when_constructed_with_name_and_sys_path_then_properties_are_set() {
        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(underTest.getSysPath()).isEqualTo(SYS_FILE_EXAMPLE);
    }

    @ParameterizedTest
    @MethodSource("writeSuccessCombinations")
    void test_write_happy_path(int percentage, String expectedRawValue) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, expectedRawValue))
                .thenAnswer(_ -> null);

            underTest.write(percentage);

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verify(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, expectedRawValue));
        }
    }

    @Test
    void when_file_is_not_writable_then_write_throws_exception() {
        var expectedException = new JcException("expected exception");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE))
                .thenThrow(expectedException);

            assertThatThrownBy(() -> underTest.write(0)).isSameAs(expectedException);
        }
    }

    @Test
    void when_write_fails_then_exception_is_thrown() {
        var expectedException = new JcException("expected exception");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, "0"))
                .thenThrow(expectedException);

            assertThatThrownBy(() -> underTest.write(0)).isSameAs(expectedException);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void when_value_is_out_of_range_then_write_throws_exception(int percentage) {
        assertThatThrownBy(() -> underTest.write(percentage))
            .isInstanceOf(JcException.class)
            .hasMessage("rpm value out of range [0, 100]: " + percentage);
    }

    @ParameterizedTest
    @MethodSource("readSuccessCombinations")
    void test_read_happy_path(String rawValue, int expectedPercentage) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE)).thenReturn(rawValue);

            var actualPercentage = underTest.read();
            assertThat(actualPercentage).isEqualTo(expectedPercentage);

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verify(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE));
        }
    }

    @Test
    void when_file_is_not_readable_then_read_throws_exception() {
        var expectedException = new JcException("expected exception");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenThrow(expectedException);

            assertThatThrownBy(underTest::read).isSameAs(expectedException);
        }
    }

    @Test
    void when_read_fails_then_exception_is_thrown() {
        var expectedException = new JcException("expected exception");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE))
                .thenThrow(expectedException);

            assertThatThrownBy(underTest::read).isSameAs(expectedException);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "256"})
    void when_value_is_out_of_range_then_read_throws_exception(String rawValue) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(rawValue);

            assertThatThrownBy(underTest::read)
                .isInstanceOf(JcException.class)
                .hasMessage("rpm raw value out of range [0, 255]: " + rawValue);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "NaN"})
    @EmptySource
    void when_read_value_is_nan_then_exception_is_thrown(String rawValue) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(rawValue);

            assertThatThrownBy(underTest::read)
                .isInstanceOf(JcException.class)
                .hasMessage("Value of device '" + NAME_EXAMPLE + "' is not a number.");
        }
    }

    // percentage, raw value
    private static Stream<Arguments> writeSuccessCombinations() {
        return Stream.of(
            arguments(0, "0"),
            // 51% of 255 is 130,05. Since we always round up, the raw value is supposed to be 131.
            arguments(51, "131"),
            arguments(100, "255")
        );
    }

    // raw value, percentage
    private static Stream<Arguments> readSuccessCombinations() {
        return Stream.of(
            arguments("0", 0),
            // 51% of 255 is 130,05. Since we always round up, the raw value is supposed to be 131.
            arguments("131", 51),
            arguments("255", 100)
        );
    }
}
