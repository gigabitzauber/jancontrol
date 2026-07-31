package de.gigabitzauber.jancontrol.drivers.hwmon;

import de.gigabitzauber.jancontrol.domain.api.FanMode;

import java.util.Arrays;

/*
 * See https://docs.kernel.org/admin-guide/laptops/thinkpad-acpi.html
 */
public enum ThinkpadAcpiFanModes implements FanMode {
    DISABLED("0"),
    MANUAL("1"),
    AUTO("2"),
    RESERVED("3");

    private final String rawValue;

    ThinkpadAcpiFanModes(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String rawValue() {
        return this.rawValue;
    }

    public static ThinkpadAcpiFanModes fromRawValue(String rawValue) {
        return Arrays.stream(values())
            .filter(curMode -> curMode.rawValue.equals(rawValue))
            .findFirst()
            .orElse(null);
    }
}