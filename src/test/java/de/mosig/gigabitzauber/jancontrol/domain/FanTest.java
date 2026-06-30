package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FanTest {

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
        var device = mock(WriteableDevice.class);
        var curves = Set.of(Curve.builder().build());
        var dependsOn = List.of(mock(ReadOnlyDevice.class));

        var localUnderTest = new Fan(interval, device, curves, dependsOn);

        assertThat(localUnderTest.interval()).isEqualTo(interval);
        assertThat(localUnderTest.device()).isEqualTo(device);
        assertThat(localUnderTest.curves()).isEqualTo(curves);
        assertThat(localUnderTest.dependsOn()).isEqualTo(dependsOn);
    }
}
