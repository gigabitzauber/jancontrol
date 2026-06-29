package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FanTest {

    private final Fan underTest = Fan.builder().build();

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.interval()).isEqualTo(Fan.DEFAULT_INTERVAL);
        assertThat(underTest.device()).isNull();
        assertThat(underTest.curve()).isNull();
        assertThat(underTest.dependsOn()).isEmpty();
    }

    @Test
    void test_allArgsConstructor() {
        var interval = Duration.ofSeconds(3);
        var device = mock(WriteableDevice.class);
        var curve = Curve.builder().build();
        var dependsOn = List.of(mock(ReadOnlyDevice.class));

        var localUnderTest = new Fan(interval, device, curve, dependsOn);

        assertThat(localUnderTest.interval()).isEqualTo(interval);
        assertThat(localUnderTest.device()).isEqualTo(device);
        assertThat(localUnderTest.curve()).isEqualTo(curve);
        assertThat(localUnderTest.dependsOn()).isEqualTo(dependsOn);
    }
}
