package de.gigabitzauber.jancontrol.cruise;

import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

public final class JcErrorUtil {
    private JcErrorUtil() {
    }

    public static String safeGetMessage(Throwable t) {
        requireNonNull(t, "t must not be null");
        var rawMsg = t.getMessage();
        return StringUtils.isBlank(rawMsg) ? "No further information" : rawMsg.strip();
    }
}
