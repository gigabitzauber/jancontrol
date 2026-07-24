package de.gigabitzauber.jancontrol.util;

import java.time.Instant;

public final class JcSystemTime implements JcTime {
    @Override
    public long currentTimestampMillis() {
        return Instant.now().toEpochMilli();
    }
}
