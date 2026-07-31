package de.gigabitzauber.jancontrol.domain;

import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.domain.api.FanMode;
import de.gigabitzauber.jancontrol.domain.api.JcHwmonDriver;
import de.gigabitzauber.jancontrol.domain.api.NamedDevice;
import de.gigabitzauber.jancontrol.domain.api.TypedReadableDevice;
import de.gigabitzauber.jancontrol.domain.api.TypedWriteableDevice;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RpmDeviceTest {
    private static final String NAME_EXAMPLE = "readOnlyDeviceExample";
    private static final String SYS_FILE_EXAMPLE = "sysFileExample";
    private static final Path SYS_FILE_PATH_EXAMPLE = Paths.get(SYS_FILE_EXAMPLE);
    private static final JcHwmonDriver DRIVER_EXAMPLE = JcHwmonDrivers.NCT6775;
    private static final boolean ALLOW_IDLE_EXAMPLE = true;
    private static final int ACTIVATION_THRESHOLD_EXAMPLE = 11;

    @TempDir
    private Path tempDir;

    private RpmDevice underTest;

    @BeforeEach
    void setUp() {
        underTest = createUnderTest(DRIVER_EXAMPLE, SYS_FILE_PATH_EXAMPLE);
    }

    @Test
    void test_no_args_builder() {
        var localUnderTest = RpmDevice.builder().build();
        assertThat(localUnderTest.getName()).isNull();
        assertThat(localUnderTest.getSysPath()).isNull();
        assertThat(localUnderTest.getDriver()).isEqualTo(JcHwmonDrivers.NCT6775);
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

    @Test
    void getMode_should_use_configured_driver() throws Exception {
        String serializedFanMode = "serializedFanMode";
        var expectedFanMode = mock(FanMode.class);
        var driverMock = mock(JcHwmonDriver.class);
        when(driverMock.toFanMode(serializedFanMode)).thenReturn(expectedFanMode);
        var sysFilePath = tempDir.resolve(SYS_FILE_EXAMPLE);
        var sysModeFilePath = tempDir.resolve(SYS_FILE_EXAMPLE + "_enable");
        Files.writeString(sysModeFilePath, serializedFanMode);

        var localUnderTest = createUnderTest(driverMock, sysFilePath);

        assertThat(localUnderTest.getMode()).isEqualTo(expectedFanMode);
    }

    @Test
    void getMode_should_throw_if_mode_is_unknown() throws Exception {
        String serializedFanMode = "serializedFanMode";
        var driverMock = mock(JcHwmonDriver.class);
        when(driverMock.toFanMode(serializedFanMode)).thenReturn(null);
        var sysFilePath = tempDir.resolve(SYS_FILE_EXAMPLE);
        var sysModeFilePath = tempDir.resolve(SYS_FILE_EXAMPLE + "_enable");
        Files.writeString(sysModeFilePath, serializedFanMode);

        var localUnderTest = createUnderTest(driverMock, sysFilePath);

        assertThatThrownBy(localUnderTest::getMode)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("contains fan mode unknown to configured driver '%s': %s"
                .formatted(driverMock.name(), serializedFanMode));
    }

    @Test
    void activateManualMode_should_use_configured_driver() throws Exception {
        var driverMock = mock(JcHwmonDriver.class);
        var manualModeMock = mock(FanMode.class);
        when(manualModeMock.rawValue()).thenReturn("manualModeMockRawValue");
        when(driverMock.manualMode()).thenReturn(manualModeMock);
        var sysFilePath = tempDir.resolve(SYS_FILE_EXAMPLE);
        var sysModeFilePath = tempDir.resolve(SYS_FILE_EXAMPLE + "_enable");
        Files.writeString(sysModeFilePath, "otherMode");

        var localUnderTest = spy(createUnderTest(driverMock, sysFilePath));

        var activatedMode = localUnderTest.activateManualMode();

        verify(localUnderTest).setMode(manualModeMock);
        assertThat(activatedMode).isEqualTo(manualModeMock);
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

    private RpmDevice createUnderTest(JcHwmonDriver driver, Path sysFilePath) {
        return RpmDevice.builder()
            .name(NAME_EXAMPLE)
            .sysPath(sysFilePath.toString())
            .activationThreshold(ACTIVATION_THRESHOLD_EXAMPLE)
            .allowIdle(ALLOW_IDLE_EXAMPLE)
            .driver(driver)
            .build();
    }
}
