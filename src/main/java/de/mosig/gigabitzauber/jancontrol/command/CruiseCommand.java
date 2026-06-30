package de.mosig.gigabitzauber.jancontrol.command;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import org.slf4j.Logger;

public final class CruiseCommand {

    private final ListeningScheduledExecutorService fanCruiseExecutor;
    private final Logger log;

    public CruiseCommand(ListeningScheduledExecutorService fanCruiseExecutor, Logger log) {
        this.fanCruiseExecutor = fanCruiseExecutor;
        this.log = log;
    }

    public void execute(CruiseConfig config) {
        for (var fan : config.fans()) {
            new CruiseInstance(fan, fanCruiseExecutor, log).schedule();
        }
    }
}
