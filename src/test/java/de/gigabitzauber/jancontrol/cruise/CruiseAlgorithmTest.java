package de.gigabitzauber.jancontrol.cruise;

import com.google.common.collect.Range;
import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CruiseAlgorithmTest {
    private static final Fan FAN_EXAMPLE = Fan.builder().interval(Duration.ofSeconds(10)).build();

    @Mock
    private JcLifecycle lifecycleMock;
    @Mock
    private Logger logMock;

    @Test
    void does_not_support_null_fan() {
        assertThatThrownBy(() -> new CruiseAlgorithm(null, lifecycleMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fan must not be null");
    }

    @Test
    void does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> new CruiseAlgorithm(FAN_EXAMPLE, null, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void does_not_support_null_log() {
        assertThatThrownBy(() -> new CruiseAlgorithm(FAN_EXAMPLE, lifecycleMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("log must not be null");
    }

    @ParameterizedTest
    @CsvSource({"false, 20", "true, 0"})
    void test_with_one_dependency_and_low_mid_and_high_values(boolean idleFlag, int expectedLowerRpmThreshold) {
        var lowTempExample = 30;
        var midTempExample = 40;
        var highTempExample = 50;
        var temperatureDevice = simulateTemperatureDevice("dependencyA", lowTempExample, midTempExample, highTempExample);
        var rpmDeviceName = "rpmDeviceMockA";
        var rpmDevice = simulateRpmDevice(rpmDeviceName);
        when(rpmDevice.allowIdle()).thenReturn(idleFlag);
        var expectedMidRpm = 50;
        var expectedHighRpmThreshold = 100;
        var curve = Curve.builder()
            .ref(temperatureDevice.getRef())
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
        var localUnderTest = new CruiseAlgorithm(fan, lifecycleMock, logMock);

        localUnderTest.run();
        verify(rpmDevice).write(expectedLowerRpmThreshold);
        if (!idleFlag) {
            verify(logMock).warn("Setting RPM value for {} to lowest allowed value: {}", rpmDeviceName, expectedLowerRpmThreshold);
        }

        localUnderTest.run();
        verify(rpmDevice).write(expectedMidRpm);

        localUnderTest.run();
        verify(rpmDevice).write(expectedHighRpmThreshold);
        verify(logMock).warn("Setting RPM value for {} to highest allowed value: {}", rpmDeviceName, expectedHighRpmThreshold);
        verify(logMock, times(idleFlag ? 1 : 2)).warn("Calculated RPM value for {} exceeds safe limits.", rpmDeviceName);
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
            .ref(dependencyA.getRef())
            .points(Set.of(new CurvePoint(tempA, expectedDeviceRpm / 2)))
            .build();
        var curveB = Curve.builder()
            .ref(dependencyB.getRef())
            .points(Set.of(new CurvePoint(tempB, expectedDeviceRpm)))
            .build();
        var fan = Fan.builder()
            .device(rpmDevice)
            .dependsOn(List.of(dependencyA, dependencyB))
            .curves(List.of(curveA, curveB))
            .build();
        var localUnderTest = new CruiseAlgorithm(fan, lifecycleMock, logMock);

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
        var localUnderTest = new CruiseAlgorithm(fan, lifecycleMock, logMock);

        localUnderTest.run();

        verify(rpmDevice, never()).write(anyInt());
    }

    @Test
    void when_going_downwards_then_respect_downSkip() {
        var dependencyA = simulateTemperatureDevice("dependencyA", 50, 40, 30, 20);
        var rpmDevice = simulateRpmDevice("rpmDeviceMockA", 20, 50);
        var curveA = Curve.builder()
            .ref(dependencyA.getRef())
            .points(Set.of(
                new CurvePoint(20, 20),
                new CurvePoint(30, 30),
                new CurvePoint(40, 40),
                new CurvePoint(50, 50)))
            .build();
        var downSkipExample = 3;
        var fan = Fan.builder()
            .downSkip(downSkipExample)
            .device(rpmDevice)
            .dependsOn(List.of(dependencyA))
            .curves(List.of(curveA))
            .build();
        var localUnderTest = new CruiseAlgorithm(fan, lifecycleMock, logMock);

        // Set RPM from 10 to 40
        localUnderTest.run();

        for (int i = 0; i <= downSkipExample; i++) {
            localUnderTest.run();
        }

        var inOrder = Mockito.inOrder(rpmDevice);
        inOrder.verify(rpmDevice).write(50);
        inOrder.verify(rpmDevice).write(20);
    }

    @ParameterizedTest
    @MethodSource("nInterpolationExamples")
    void writes_only_multiples_of_n(Integer x, Integer expectedResult) {
        var dependencyA = simulateTemperatureDevice("dependencyA", 40);
        var rpmDevice = simulateRpmDevice("rpmDeviceMockA", 100);
        var curveA = Curve.builder()
            .ref(dependencyA.getRef())
            .points(Set.of(
                new CurvePoint(40, x)))
            .build();
        var fan = Fan.builder()
            .n(5)
            .device(rpmDevice)
            .dependsOn(List.of(dependencyA))
            .curves(List.of(curveA))
            .build();

        var localUnderTest = new CruiseAlgorithm(fan, lifecycleMock, logMock);
        localUnderTest.run();

        verify(rpmDevice).write(expectedResult);
    }

    private static Stream<Arguments> nInterpolationExamples() {
        return Stream.of(
            arguments(42, 40),
            arguments(43, 45),
            arguments(55, 55)
        );
    }

    private TemperatureDevice simulateTemperatureDevice(String ref, Integer... measurements) {
        var result = mock(TemperatureDevice.class);
        when(result.getRef()).thenReturn(ref);
        var otherMeasurements = Arrays.copyOfRange(measurements, 1, measurements.length);
        when(result.read()).thenReturn(measurements[0], otherMeasurements);

        return result;
    }

    private RpmDevice simulateRpmDevice(String ref, Integer... rpms) {
        var result = mock(RpmDevice.class);
        when(result.getRef()).thenReturn(ref);
        lenient().when(result.safetyMargin()).thenReturn(Range.closed(20, 100));

        if (rpms.length > 1) {
            var otherRpms = Arrays.copyOfRange(rpms, 1, rpms.length);
            lenient().when(result.read()).thenReturn(rpms[0], otherRpms);
        }

        return result;
    }
}
