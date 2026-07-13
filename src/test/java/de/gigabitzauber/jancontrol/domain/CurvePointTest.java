package de.gigabitzauber.jancontrol.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurvePointTest {

    private static final int TEMP_EXAMPLE = 50;
    private static final int RPM_EXAMPLE = 75;

    @Test
    void when_construct_with_all_args_then_fields_are_set() {
        var localUnderTest = new CurvePoint(TEMP_EXAMPLE, RPM_EXAMPLE);

        assertThat(localUnderTest.temp()).isEqualTo(TEMP_EXAMPLE);
        assertThat(localUnderTest.rpm()).isEqualTo(RPM_EXAMPLE);
    }

    @Test
    void when_use_builder_then_fields_are_set() {
        var localUnderTest = new CurvePoint(TEMP_EXAMPLE, RPM_EXAMPLE);

        assertThat(localUnderTest.temp()).isEqualTo(TEMP_EXAMPLE);
        assertThat(localUnderTest.rpm()).isEqualTo(RPM_EXAMPLE);
    }

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(CurvePoint.class).verify();
    }
}
