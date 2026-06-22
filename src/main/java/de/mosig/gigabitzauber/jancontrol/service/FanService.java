package de.mosig.gigabitzauber.jancontrol.service;

import org.springframework.stereotype.Service;
import de.mosig.gigabitzauber.jancontrol.domain.FanStatus;

@Service
public class FanService {
    public FanStatus setSpeed(int speed) {
        int s = Math.max(0, Math.min(speed, 5));
        String state = s > 0 ? "RUNNING" : "STOPPED";
        // hardware integration would live in infrastructure adapters
        return new FanStatus(s, state);
    }
}
