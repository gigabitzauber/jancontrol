package de.gigabitzauber.jancontrol.domain;

import java.util.Arrays;

/*
 * Currently known modes are taken from https://www.kernel.org/doc/Documentation/hwmon/nct6775
 */
public enum FanModes implements FanMode {
    DISABLED("0"),
    MANUAL("1"),
    THERMAL_CRUISE("2"),
    FAN_SPEED_CRUISE("3"),
    SMART_FAN_III("4"),
    SMART_FAN_IV("5");

    private final String rawValue;

    FanModes(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String rawValue() {
        return this.rawValue;
    }

    public static FanModes fromRawValue(String rawValue) {
        return Arrays.stream(values())
            .filter(curMode -> curMode.rawValue.equals(rawValue))
            .findFirst()
            .orElse(null);
    }
}