package de.gigabitzauber.jancontrol.domain.api;

public interface JcHwmonDriver {
    String name();

    FanMode toFanMode(String rawValue);

    FanMode manualMode();
}
