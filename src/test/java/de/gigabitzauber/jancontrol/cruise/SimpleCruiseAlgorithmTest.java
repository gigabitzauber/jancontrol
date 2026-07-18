package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleCruiseAlgorithmTest {
    private static final Fan FAN_EXAMPLE = Fan.builder().interval(Duration.ofSeconds(10)).build();

    @Mock
    private JcLifecycle lifecycleMock;
    @Mock
    private Logger logMock;

    @Test
    void does_not_support_null_fan() {
        assertThatThrownBy(() -> new SimpleCruiseAlgorithm(null, lifecycleMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fan must not be null");
    }

    @Test
    void does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> new SimpleCruiseAlgorithm(FAN_EXAMPLE, null, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void does_not_support_null_log() {
        assertThatThrownBy(() -> new SimpleCruiseAlgorithm(FAN_EXAMPLE, lifecycleMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("log must not be null");
    }

    @Test
    void test_with_one_dependency_and_low_mid_and_high_values() {
        var lowTempExample = 30;
        var midTempExample = 40;
        var highTempExample = 50;
        var temperatureDevice = simulateTemperatureDevice("dependencyA", lowTempExample, midTempExample, highTempExample);
        String rpmDeviceName = "rpmDeviceMockA";
        var rpmDevice = simulateRpmDevice(rpmDeviceName);
        var expectedLowerRpmThreshold = 20;
        var expectedMidRpm = 50;
        var expectedHighRpmThreshold = 100;
        var curve = Curve.builder()
            .ref(temperatureDevice.getName())
            .points(Set.of(
                new CurvePoint(lowTempExample, expectedLowerRpmThreshold / 2),
                new CurvePoint(midTempExample, expectedMidRpm),
                new CurvePoint(highTempExample, expectedHighRpmThreshold * 2)
            )).build();
        var fan = Fan.builder()
            .device(rpmDevice)
            .dependsOn(List.of(temperatureDevice))
            .curves(List.of(curve))
            .build();
        var localUnderTest = new SimpleCruiseAlgorithm(fan, lifecycleMock, logMock);

        localUnderTest.run();
        verify(rpmDevice).write(expectedLowerRpmThreshold);
        verify(logMock).warn("Setting RPM value for {} to lowest allowed value: {}", rpmDeviceName, expectedLowerRpmThreshold);

        localUnderTest.run();
        verify(rpmDevice).write(expectedMidRpm);

        localUnderTest.run();
        verify(rpmDevice).write(expectedHighRpmThreshold);
        verify(logMock).warn("Setting RPM value for {} to highest allowed value: {}", rpmDeviceName, expectedHighRpmThreshold);
        verify(logMock, times(2)).warn("Calculated RPM value for {} exceeds safe limits.", rpmDeviceName);
    }

    @Test
    void when_multiple_dependencies_then_write_highest_rpm() {
        var tempA = 30;
        var dependencyA = simulateTemperatureDevice("dependencyA", tempA);
        var tempB = 40;
        var dependencyB = simulateTemperatureDevice("dependencyB", tempB);
        var rpmDevice = simulateRpmDevice("rpmDeviceMockA");
        var expectedDeviceRpm = 50;
        var curveA = Curve.builder()
            .ref(dependencyA.getName())
            .points(Set.of(new CurvePoint(tempA, expectedDeviceRpm / 2)))
            .build();
        var curveB = Curve.builder()
            .ref(dependencyB.getName())
            .points(Set.of(new CurvePoint(tempB, expectedDeviceRpm)))
            .build();
        var fan = Fan.builder()
            .device(rpmDevice)
            .dependsOn(List.of(dependencyA, dependencyB))
            .curves(List.of(curveA, curveB))
            .build();
        var localUnderTest = new SimpleCruiseAlgorithm(fan, lifecycleMock, logMock);

        localUnderTest.run();

        verify(rpmDevice).write(expectedDeviceRpm);
    }

    @Test
    void when_curve_cannot_be_matched_to_dependency_then_do_nothing() {
        var dependency = mock(TemperatureDevice.class);
        var rpmDevice = simulateRpmDevice("rpmDeviceMock");
        var curve = Curve.builder()
            .ref("unknown dependency")
            .points(Set.of(new CurvePoint(30, 100)))
            .build();
        var fan = Fan.builder()
            .device(rpmDevice)
            .dependsOn(List.of(dependency))
            .curves(List.of(curve))
            .build();
        var localUnderTest = new SimpleCruiseAlgorithm(fan, lifecycleMock, logMock);

        localUnderTest.run();

        verify(rpmDevice, never()).write(anyInt());
    }

    private TemperatureDevice simulateTemperatureDevice(String name, Integer... measurements) {
        var result = mock(TemperatureDevice.class);
        when(result.getName()).thenReturn(name);
        var otherMeasurements = Arrays.copyOfRange(measurements, 1, measurements.length);
        when(result.read()).thenReturn(measurements[0], otherMeasurements);

        return result;
    }

    private RpmDevice simulateRpmDevice(String name) {
        var result = mock(RpmDevice.class);
        when(result.getName()).thenReturn(name);

        return result;
    }
}
