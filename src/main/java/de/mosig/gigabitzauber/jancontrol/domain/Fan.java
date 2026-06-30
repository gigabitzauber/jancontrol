package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.mosig.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.Builder;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Builder
public record Fan(
    @JsonDeserialize(using = JcJacksonConfig.DurationDeserializer.class)
    Duration interval,
    WriteableDevice device,
    Collection<Curve> curves,
    List<ReadOnlyDevice> dependsOn) {

    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(5);

    public Fan {
        if (interval == null) {
            interval = DEFAULT_INTERVAL;
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
}
