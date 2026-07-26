package de.gigabitzauber.jancontrol.drivers.hwmon;

import de.gigabitzauber.jancontrol.domain.FanMode;
import de.gigabitzauber.jancontrol.domain.JcHwmonDriver;

import java.util.Arrays;

public enum JcHwmonDrivers implements JcHwmonDriver {
    NCT6775(Nct6775FanModes.values()),
    THINKPAD_ACPI(ThinkpadAcpiFanModes.values());

    private final FanMode[] knownFanModes;

    JcHwmonDrivers(FanMode[] knownFanModes) {
        this.knownFanModes = knownFanModes;
    }

    @Override
    public FanMode toFanMode(String rawValue) {
        return Arrays.stream(knownFanModes)
            .filter(curMode -> curMode.rawValue().equals(rawValue))
            .findFirst()
            .orElse(null);
    }

    public static JcHwmonDrivers fromCfgName(String cfgName) {

        return Arrays.stream(values())
            .filter(curDrv -> curDrv.name().equalsIgnoreCase(cfgName))
            .findFirst()
            .orElse(null);
    }
}
