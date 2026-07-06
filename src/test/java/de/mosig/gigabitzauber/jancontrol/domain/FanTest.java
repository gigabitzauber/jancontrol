package de.mosig.gigabitzauber.jancontrol.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
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
        var device = mock(RpmDevice.class);
        var curves = Set.of(Curve.builder().build());
        var dependsOn = List.of(mock(TemperatureDevice.class));

        var localUnderTest = new Fan(interval, device, curves, dependsOn);

        assertThat(localUnderTest.interval()).isEqualTo(interval);
        assertThat(localUnderTest.device()).isEqualTo(device);
        assertThat(localUnderTest.curves()).isEqualTo(curves);
        assertThat(localUnderTest.dependsOn()).isEqualTo(dependsOn);
    }

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(Fan.class).verify();
    }
}
