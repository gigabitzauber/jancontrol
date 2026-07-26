package de.gigabitzauber.jancontrol.drivers.hwmon;

import de.gigabitzauber.jancontrol.domain.FanMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class JcHwmonDriversTest {

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void toFanMode_when_rawValue_is_null_then_return_null(JcHwmonDrivers localUnderTest) {
        assertThat(localUnderTest.toFanMode(null)).isNull();
    }

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void toFanMode_when_rawValue_is_unknown_then_return_null(JcHwmonDrivers localUnderTest) {
        assertThat(localUnderTest.toFanMode("unknown")).isNull();
    }

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void toFanMode_when_rawValue_is_blank_then_return_null(JcHwmonDrivers localUnderTest) {
        assertThat(localUnderTest.toFanMode("   ")).isNull();
    }

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void toFanMode_when_rawValue_is_empty_then_return_null(JcHwmonDrivers localUnderTest) {
        assertThat(localUnderTest.toFanMode("")).isNull();
    }

    @Test
    void should_know_all_nct6775_modes() {
        for (FanMode fanMode : Nct6775FanModes.values()) {
            assertThat(JcHwmonDrivers.NCT6775.toFanMode(fanMode.rawValue())).isEqualTo(fanMode);
        }
    }

    @Test
    void should_know_all_thinkpad_acpi_modes() {
        for (FanMode fanMode : ThinkpadAcpiFanModes.values()) {
            assertThat(JcHwmonDrivers.THINKPAD_ACPI.toFanMode(fanMode.rawValue())).isEqualTo(fanMode);
        }
    }

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void fromCfgName_should_know_all_names(JcHwmonDrivers localUnderTest) {
        assertThat(JcHwmonDrivers.fromCfgName(localUnderTest.name())).isSameAs(localUnderTest);
    }

    @Test
    void fromCfgName_when_cfgName_is_null_then_return_null() {
        assertThat(JcHwmonDrivers.fromCfgName(null)).isNull();
    }

    @Test
    void fromCfgName_when_cfgName_is_unknown_then_return_null() {
        assertThat(JcHwmonDrivers.fromCfgName("unknown")).isNull();
    }

    @Test
    void fromCfgName_when_cfgName_is_blank_then_return_null() {
        assertThat(JcHwmonDrivers.fromCfgName("   ")).isNull();
    }

    @Test
    void fromCfgName_when_cfgName_is_empty_then_return_null() {
        assertThat(JcHwmonDrivers.fromCfgName("")).isNull();
    }

    @ParameterizedTest
    @EnumSource(JcHwmonDrivers.class)
    void fromCfgName_should_always_use_lowerCase(JcHwmonDrivers localUnderTest) {
        assertThat(JcHwmonDrivers.fromCfgName(localUnderTest.name().toLowerCase())).isSameAs(localUnderTest);
    }
}
