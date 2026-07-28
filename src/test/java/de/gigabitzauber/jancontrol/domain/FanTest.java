package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import de.gigabitzauber.jancontrol.drivers.hwmon.Nct6775FanModes;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FanTest {

    @TempDir
    private Path tempDir;

    private final Fan underTest = Fan.builder().build();

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.interval()).isEqualTo(Fan.DEFAULT_INTERVAL);
        assertThat(underTest.device()).isNull();
        assertThat(underTest.curves()).isEmpty();
        assertThat(underTest.dependsOn()).isEmpty();
    }

    @Test
    void test_allArgsConstructor() {
        var interval = Duration.ofSeconds(3);
        var driver = mock(JcHwmonDriver.class);
        var device = mock(RpmDevice.class);
        var curves = Set.of(Curve.builder().build());
        var dependsOn = List.of(mock(TemperatureDevice.class));

        var localUnderTest = new Fan(interval, driver, true, device, curves, dependsOn);

        assertThat(localUnderTest.interval()).isEqualTo(interval);
        assertThat(localUnderTest.hwmonDriver()).isEqualTo(driver);
        assertThat(localUnderTest.allowIdle()).isTrue();
        assertThat(localUnderTest.device()).isEqualTo(device);
        assertThat(localUnderTest.curves()).isEqualTo(curves);
        assertThat(localUnderTest.dependsOn()).isEqualTo(dependsOn);
    }

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(Fan.class).verify();
    }

    @Test
    void should_set_a_driver_if_none_specified() {
        var localUnderTest = Fan.builder().build();

        assertThat(localUnderTest.hwmonDriver()).isNotNull();
    }

    @Test
    void should_use_nct6775_if_no_driver_specified() {
        var localUnderTest = Fan.builder().build();

        assertThat(localUnderTest.hwmonDriver()).isEqualTo(JcHwmonDrivers.NCT6775);
    }

    @Test
    void should_use_default_allowIdle_if_none_specified() {
        var localUnderTest = Fan.builder().build();

        assertThat(localUnderTest.allowIdle()).isFalse();
    }

    @Test
    void getMode_should_use_configured_driver() throws Exception {
        var expectedFanMode = mock(FanMode.class);
        var driverMock = mock(JcHwmonDriver.class);

        var localUnderTest = Fan.builder()
            .device(simulateRpmDevice(driverMock, expectedFanMode))
            .hwmonDriver(driverMock).build();

        assertThat(localUnderTest.getCurrentMode()).isEqualTo(expectedFanMode);
    }

    @Test
    void getMode_should_throw_if_mode_is_unknown() throws Exception {
        var configuredFanMode = mock(FanMode.class);
        var driverMock = mock(JcHwmonDriver.class);
        when(driverMock.name()).thenReturn("driverMock");
        var rpmDevice = simulateRpmDevice(driverMock, configuredFanMode);
        var unknownMode = Nct6775FanModes.SMART_FAN_IV;

        var localUnderTest = Fan.builder()
            .device(rpmDevice)
            .hwmonDriver(driverMock).build();

        localUnderTest.setMode(unknownMode);

        assertThatThrownBy(localUnderTest::getCurrentMode)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("contains fan mode unknown to configured driver '%s': %s"
                .formatted(driverMock.name(), unknownMode.rawValue()));
    }

    @Test
    void activateManualMode_should_use_configured_driver() throws Exception {
        var configuredFanMode = mock(FanMode.class);
        var driverMock = mock(JcHwmonDriver.class);
        var manualModeMock = mock(FanMode.class);
        when(manualModeMock.rawValue()).thenReturn("manualModeMockRawValue");
        when(driverMock.manualMode()).thenReturn(manualModeMock);
        var rpmDevice = simulateRpmDevice(driverMock, configuredFanMode);
        var localUnderTest = spy(Fan.builder()
            .device(rpmDevice)
            .hwmonDriver(driverMock).build());

        localUnderTest.activateManualMode();

        verify(localUnderTest).setMode(manualModeMock);
    }

    @Test
    void when_allowIdle_is_true_then_use_min_safety_margin() {
        var localUnderTest = Fan.builder().allowIdle(true).build();

        assertThat(localUnderTest.rpmSafetyMargin()).isEqualTo(Fan.MIN_SAFETY_MARGIN);
    }

    @Test
    void when_allowIdle_is_false_then_use_default_safety_margin() {
        var localUnderTest = Fan.builder().allowIdle(false).build();

        assertThat(localUnderTest.rpmSafetyMargin()).isEqualTo(Fan.DEFAULT_SAFETY_MARGIN);
    }

    private RpmDevice simulateRpmDevice(JcHwmonDriver driverMock, FanMode expectedFanMode) throws Exception {
        var rawFanModeExample = "1";
        when(driverMock.toFanMode(rawFanModeExample)).thenReturn(expectedFanMode);

        var rpmFilePath = tempDir.resolve("rpmFileExample");
        Files.writeString(rpmFilePath, "100");

        var rpmModeFilePath = tempDir.resolve(rpmFilePath + "_enable");
        Files.writeString(rpmModeFilePath, rawFanModeExample);

        return new RpmDevice("rpmDeviceExample", rpmFilePath.toString());
    }
}
