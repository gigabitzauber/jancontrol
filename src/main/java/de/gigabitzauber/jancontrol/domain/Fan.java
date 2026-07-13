package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.Builder;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Builder
public record Fan(
    @JsonDeserialize(using = JcJacksonConfig.DurationDeserializer.class)
    @JsonSerialize(using = JcJacksonConfig.DurationSerializer.class)
    Duration interval,
    RpmDevice device,
    Collection<Curve> curves,
    List<TemperatureDevice> dependsOn) {

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
