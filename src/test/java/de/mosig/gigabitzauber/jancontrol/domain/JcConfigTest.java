package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JcConfigTest {

    private final JcConfig underTest = new JcConfig(Set.of());

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.fans()).isEmpty();
    }

    @Test
    void test_allArgsConstructor() {
        var expectedFans = Set.of(Fan.builder().build());
        var localUnderTest = new JcConfig(expectedFans);

        assertThat(localUnderTest.fans()).isEqualTo(expectedFans);
    }
}
