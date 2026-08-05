package de.gigabitzauber.jancontrol.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CruiseConfigRootTest {

    private final CruiseConfigRoot underTest = new CruiseConfigRoot(Set.of());

    @Test
    void test_noArgsConstructor() {
        assertThat(underTest.fans()).isEmpty();
    }

    @Test
    void test_allArgsConstructor() {
        var expectedFans = Set.of(Fan.builder().build());
        var localUnderTest = new CruiseConfigRoot(expectedFans);

        assertThat(localUnderTest.fans()).isEqualTo(expectedFans);
    }

    @Test
    void test_equalsHashCodeContract() {
        EqualsVerifier.forClass(CruiseConfigRoot.class).verify();
    }
}
