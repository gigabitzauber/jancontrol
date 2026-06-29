package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FanTest {

    private final Fan underTest = Fan.builder().build();

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.dependsOn()).isEmpty();
        assertThat(underTest.device()).isNull();
        assertThat(underTest.curve()).isNull();
    }

    @Test
    void test_allArgsConstructor() {
        var device = mock(WriteableDevice.class);
        var curve = Curve.builder().build();
        var dependsOn = List.of(mock(ReadOnlyDevice.class));

        var localUnderTest = new Fan(device, curve, dependsOn);

        assertThat(localUnderTest.device()).isEqualTo(device);
        assertThat(localUnderTest.curve()).isEqualTo(curve);
        assertThat(localUnderTest.dependsOn()).isEqualTo(dependsOn);
    }
}
