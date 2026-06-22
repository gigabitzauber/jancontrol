package de.mosig.gigabitzauber.jancontrol.command;

import org.springframework.stereotype.Component;
import de.mosig.gigabitzauber.jancontrol.service.FanService;

@Component
public class RunFanCommand {
    private final FanService fanService;

    public RunFanCommand(FanService fanService) {
        this.fanService = fanService;
    }

    public void execute(int speed) {
        var status = fanService.setSpeed(speed);
        System.out.println("Fan status: speed=" + status.speed() + " state=" + status.state());
    }
}
