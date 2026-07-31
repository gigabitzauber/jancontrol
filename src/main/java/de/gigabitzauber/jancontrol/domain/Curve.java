package de.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.domain.api.CurveType;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.Collection;
import java.util.Set;

@Builder
@Jacksonized
public record Curve(
    String ref,
    @JsonDeserialize(using = JcJacksonConfig.CurveTypeDeserializer.class)
    CurveType type,
    Collection<CurvePoint> points) {

    public Curve {
        if (type == null) {
            type = CurveTypes.LINEAR;
        }
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
