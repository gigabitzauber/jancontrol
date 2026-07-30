package de.gigabitzauber.jancontrol.domain;

import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import nl.jqno.equalsverifier.EqualsVerifier;
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
    private static final boolean ALLOW_IDLE_EXAMPLE = true;
    private static final int ACTIVATION_THRESHOLD_EXAMPLE = 11;

    private final RpmDevice underTest = new RpmDevice(NAME_EXAMPLE, SYS_FILE_EXAMPLE, ALLOW_IDLE_EXAMPLE, ACTIVATION_THRESHOLD_EXAMPLE);

    @Test
    void test_no_args_constructor() {
        var localUnderTest = new RpmDevice();
        assertThat(localUnderTest.getName()).isNull();
        assertThat(localUnderTest.getSysPath()).isNull();
        assertThat(localUnderTest.isAllowIdle()).isFalse();
        assertThat(localUnderTest.getActivationThreshold()).isEqualTo(RpmDevice.DEFAULT_ACTIVATION_THRESHOLD_PERCENT);
        assertThat(localUnderTest.safetyMargin()).isEqualTo(Range.closed(RpmDevice.DEFAULT_ACTIVATION_THRESHOLD_PERCENT, 100));
    }

    @Test
    void should_inherit_from_proper_parents() {
        assertThat(this.underTest).isInstanceOf(NamedDevice.class);
        assertThat(this.underTest).isInstanceOf(TypedReadableDevice.class);
        assertThat(this.underTest).isInstanceOf(TypedWriteableDevice.class);
    }

    @Test
    void when_constructed_with_name_and_sys_path_then_properties_are_set() {
        var localUnderTest = new RpmDevice(NAME_EXAMPLE, SYS_FILE_EXAMPLE);
        assertThat(localUnderTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(localUnderTest.getSysPath()).isEqualTo(SYS_FILE_EXAMPLE);
        assertThat(localUnderTest.isAllowIdle()).isFalse();
        assertThat(localUnderTest.getActivationThreshold()).isEqualTo(RpmDevice.DEFAULT_ACTIVATION_THRESHOLD_PERCENT);
        assertThat(localUnderTest.safetyMargin()).isEqualTo(Range.closed(RpmDevice.DEFAULT_ACTIVATION_THRESHOLD_PERCENT, 100));
    }

    @Test
    void when_constructed_with_name_and_sys_path_and_allowIdle_and_activation_threshold_then_properties_are_set() {
        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(underTest.getSysPath()).isEqualTo(SYS_FILE_EXAMPLE);
        assertThat(underTest.isAllowIdle()).isEqualTo(ALLOW_IDLE_EXAMPLE);
        assertThat(underTest.getActivationThreshold()).isEqualTo(ACTIVATION_THRESHOLD_EXAMPLE);
        assertThat(underTest.safetyMargin()).isEqualTo(Range.closed(ACTIVATION_THRESHOLD_EXAMPLE, 100));
    }

    @ParameterizedTest
    @MethodSource("writeSuccessCombinations")
    void test_write_happy_path(int inputPercentage, String expectedRawValue) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, expectedRawValue))
                .thenAnswer(_ -> null);

            var actuallyWrittenValue = underTest.write(inputPercentage);

            var expectedPercentage = expectedRawValue.equals("0") ? 0 : inputPercentage;
            assertThat(actuallyWrittenValue).isEqualTo(expectedPercentage);
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
            .hasMessage("rpm targetValue out of range [0, 100]: " + percentage);
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

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(RpmDevice.class).withRedefinedSuperclass().verify();
    }

    // percentage, raw value
    private static Stream<Arguments> writeSuccessCombinations() {
        return Stream.of(
            arguments(0, "0"),
            arguments(ACTIVATION_THRESHOLD_EXAMPLE - 1, "0"),
            arguments(ACTIVATION_THRESHOLD_EXAMPLE, "29"),
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
