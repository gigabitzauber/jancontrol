package de.gigabitzauber.jancontrol.domain;

public interface JcHwmonDriver {
    String name();

    FanMode toFanMode(String rawValue);

    FanMode manualMode();
}
