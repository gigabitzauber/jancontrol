package de.gigabitzauber.jancontrol.domain;

public interface JcHwmonDriver {
    public String name();

    public FanMode toFanMode(String rawValue);
}
