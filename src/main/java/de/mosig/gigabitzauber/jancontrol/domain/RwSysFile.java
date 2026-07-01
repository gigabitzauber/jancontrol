package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;

import static java.util.Objects.requireNonNull;

public class RwSysFile extends Device implements RawReadableDevice, RawWriteableDevice {

    public RwSysFile(String rawPath) {
        super(rawPath);
    }

    @Override
    public String readRaw() {
        return JcIoUtil.readString(safeSysPath());
    }

    @Override
    public void writeRaw(String rawValue) {
        requireNonNull(rawValue, "rawValue must not be null");
        JcIoUtil.writeString(safeSysPath(), rawValue);
    }
}
