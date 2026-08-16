package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Builder
@Jacksonized
public record Fan(
    @JsonDeserialize(using = JcJacksonConfig.DurationDeserializer.class)
    @JsonSerialize(using = JcJacksonConfig.DurationSerializer.class)
    Duration interval,
    Integer downSkip,
    Integer n,
    RpmDevice device,
    Collection<Curve> curves,
    List<TemperatureDevice> dependsOn) {

    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);
    public static final int DEFAULT_DOWN_SKIP = 0;
    public static final int DEFAULT_N = 1;

    public Fan {
        if (interval == null) {
            interval = DEFAULT_INTERVAL;
        }

        if (downSkip == null) {
            downSkip = DEFAULT_DOWN_SKIP;
        }

        if (n == null) {
            n = DEFAULT_N;
        }

        if (curves == null) {
            curves = Set.of();
        }
        curves = Set.copyOf(curves);

        if (dependsOn == null) {
            dependsOn = List.of();
        }
        dependsOn = List.copyOf(dependsOn);
    }

    public void emergency() {
        device().setEmergencyRpm();
    }
}
