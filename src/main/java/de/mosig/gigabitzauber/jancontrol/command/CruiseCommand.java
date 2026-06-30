package de.mosig.gigabitzauber.jancontrol.command;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.slf4j.Logger;

public final class CruiseCommand {

    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final JcLifecycle lifecycle;
    private final Logger log;

    public CruiseCommand(ListeningScheduledExecutorService fanCruiseExecutor, JcLifecycle lifecycle, Logger log) {
        this.fanCruiseExecutor = fanCruiseExecutor;
        this.lifecycle = lifecycle;
        this.log = log;
    }

    public void execute(CruiseConfig config) {
        for (var fan : config.fans()) {
            lifecycle.register(fan);
            new CruiseInstance(fan, fanCruiseExecutor, log).schedule();
        }
    }
}
