package de.mosig.gigabitzauber.jancontrol.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import de.mosig.gigabitzauber.jancontrol.domain.FanStatus;

public class FanServiceTest {

    @Test
    void when_set_speed_out_of_range_then_clamped() {
        var svc = new FanService();
        FanStatus status = svc.setSpeed(99);
        assertThat(status.speed()).isEqualTo(5);
        assertThat(status.state()).isEqualTo("RUNNING");
    }

    @Test
    void when_speed_zero_then_stopped() {
        var svc = new FanService();
        FanStatus status = svc.setSpeed(0);
        assertThat(status.speed()).isEqualTo(0);
        assertThat(status.state()).isEqualTo("STOPPED");
    }
}
