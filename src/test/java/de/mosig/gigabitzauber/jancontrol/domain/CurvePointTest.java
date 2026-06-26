package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurvePointTest {

    private static final int TEMP_EXAMPLE = 50;
    private static final int RPM_EXAMPLE = 75;

    @Test
    void when_construct_with_all_args_then_fields_are_set() {
        var localUnderTest = new CurvePoint(TEMP_EXAMPLE, RPM_EXAMPLE);

        assertThat(localUnderTest.getTemp()).isEqualTo(TEMP_EXAMPLE);
        assertThat(localUnderTest.getRpm()).isEqualTo(RPM_EXAMPLE);
    }

    @Test
    void when_construct_with_no_args_then_fields_are_zeroed() {
        var localUnderTest = new CurvePoint();

        assertThat(localUnderTest.getTemp()).isEqualTo(0);
        assertThat(localUnderTest.getRpm()).isEqualTo(0);
    }

    @Test
    void when_use_builder_then_fields_are_set() {
        var localUnderTest = CurvePoint.builder()
            .temp(TEMP_EXAMPLE)
            .rpm(RPM_EXAMPLE)
            .build();

        assertThat(localUnderTest.getTemp()).isEqualTo(TEMP_EXAMPLE);
        assertThat(localUnderTest.getRpm()).isEqualTo(RPM_EXAMPLE);
    }

    @Test
    void when_set_temp_then_temp_is_updated() {
        var localUnderTest = new CurvePoint(TEMP_EXAMPLE, RPM_EXAMPLE);
        var newTemp = 60;

        localUnderTest.setTemp(newTemp);

        assertThat(localUnderTest.getTemp()).isEqualTo(newTemp);
    }

    @Test
    void when_set_rpm_then_rpm_is_updated() {
        var localUnderTest = new CurvePoint(TEMP_EXAMPLE, RPM_EXAMPLE);
        var newRpm = 85;

        localUnderTest.setRpm(newRpm);

        assertThat(localUnderTest.getRpm()).isEqualTo(newRpm);
    }
}
