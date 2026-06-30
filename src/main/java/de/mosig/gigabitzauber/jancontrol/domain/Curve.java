package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.mosig.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.Builder;

import java.util.Collection;
import java.util.Set;

@Builder
public record Curve(
    String ref,
    @JsonDeserialize(using = JcJacksonConfig.CurveTypeDeserializer.class)
    CurveType type,
    Collection<CurvePoint> points) {

    public Curve {
        if (points == null) {
            points = Set.of();
        }
        points = Set.copyOf(points);
    }

    @JsonIgnore
    public int getY(int x) {
        return type.createInterpolator(this).interpolate(x);
    }
}
