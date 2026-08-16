package de.gigabitzauber.jancontrol.cruise;

import java.time.Duration;

public class NopCruise extends JcSchedulable {

    static final Duration INITIAL_DELAY = Duration.ofMillis(123);
    static final Duration POLL_INTERVAL = Duration.ofMillis(60000);

    private NopCruise() {
        super(
            new JcOp(
                "NOP cruise",
                () -> {
                }
            ),
            INITIAL_DELAY,
            POLL_INTERVAL);
    }

    public static NopCruise create() {
        return new NopCruise();
    }
}
