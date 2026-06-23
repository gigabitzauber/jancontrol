package de.mosig.gigabitzauber.jancontrol.command;

import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import org.springframework.stereotype.Component;

@Component
public class CruiseCommand {
    public void execute(JcConfig config) {
        System.out.println(config);
    }
}
