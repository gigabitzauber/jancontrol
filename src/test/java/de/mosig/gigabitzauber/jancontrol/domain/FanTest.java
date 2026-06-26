package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FanTest {

    private final Fan underTest = new Fan();

    @Test
    void test_noArgsConstructor() {

        assertThat(underTest.getDependsOn()).isEmpty();
        assertThat(underTest.getDevice()).isNull();
        assertThat(underTest.getCurve()).isNull();
    }

    @Test
    void test_allArgsConstructor() {
        var device = new WriteableDevice();
        var curve = new Curve();
        var dependsOn = List.of(new ReadOnlyDevice());

        var localUnderTest = new Fan(device, curve, dependsOn);

        assertThat(localUnderTest.getDevice()).isEqualTo(device);
        assertThat(localUnderTest.getCurve()).isEqualTo(curve);
        assertThat(localUnderTest.getDependsOn()).isEqualTo(dependsOn);
    }

    @Test
    void test_setGetDevice() {
        var expectedDevice = new WriteableDevice();

        underTest.setDevice(expectedDevice);

        assertThat(underTest.getDevice()).isEqualTo(expectedDevice);
    }

    @Test
    void test_setGetCurve() {
        var expectedCurve = new Curve();

        underTest.setCurve(expectedCurve);

        assertThat(underTest.getCurve()).isEqualTo(expectedCurve);
    }

    @Test
    void test_setGetDependsOn() {
        var expectedDependsOn = List.of(new ReadOnlyDevice());

        underTest.setDependsOn(expectedDependsOn);

        assertThat(underTest.getDependsOn()).isEqualTo(expectedDependsOn);
    }
}
