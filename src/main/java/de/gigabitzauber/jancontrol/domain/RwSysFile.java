package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.util.JcIoUtil;

import static java.util.Objects.requireNonNull;

public final class RwSysFile extends Device implements RawReadableDevice, RawWriteableDevice {

    public RwSysFile(String rawPath) {
        super(rawPath);
    }

    @Override
    public String readRaw() {
        return JcIoUtil.readString(safeReadableSysPath());
    }

    @Override
    public void writeRaw(String rawValue) {
        requireNonNull(rawValue, "rawValue must not be null");
        JcIoUtil.writeString(safeWritableSysPath(), rawValue);
    }
}
