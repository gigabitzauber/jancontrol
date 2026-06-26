package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JcConfigTest {

    private final JcConfig underTest = new JcConfig();

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.getFans()).isEmpty();
    }

    @Test
    void test_allArgsConstructor() {
        var expectedFans = Set.of(new Fan());
        var localUnderTest = new JcConfig(expectedFans);

        assertThat(localUnderTest.getFans()).isEqualTo(expectedFans);
    }

    @Test
    void test_setGetFans() {
        var expectedFans = Set.of(new Fan());

        underTest.setFans(expectedFans);

        assertThat(underTest.getFans()).isEqualTo(expectedFans);
    }
}
