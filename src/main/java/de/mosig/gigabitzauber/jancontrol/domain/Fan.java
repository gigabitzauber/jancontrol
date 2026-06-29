package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.mosig.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.Builder;

import java.time.Duration;
import java.util.List;

@Builder
public record Fan(
    @JsonDeserialize(using = JcJacksonConfig.DurationDeserializer.class)
    Duration interval,
    WriteableDevice device,
    Curve curve,
    List<ReadOnlyDevice> dependsOn) {

    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);

    public Fan {
        if (interval == null) {
            interval = DEFAULT_INTERVAL;
        }

        if (dependsOn == null) {
            dependsOn = List.of();
        }
        dependsOn = List.copyOf(dependsOn);
    }
}
