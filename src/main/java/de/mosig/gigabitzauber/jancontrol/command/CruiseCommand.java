package de.mosig.gigabitzauber.jancontrol.command;

import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import org.springframework.stereotype.Component;

@Component
public class CruiseCommand {
    public void execute(JcConfig config) {
        var fan = config.fans().stream().findFirst().get();
        var curve = fan.curve();

        var inputs = new int[]{45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};

        for (var input : inputs) {
            System.out.println(input + " -> " + curve.getY(input));
        }
    }

}
