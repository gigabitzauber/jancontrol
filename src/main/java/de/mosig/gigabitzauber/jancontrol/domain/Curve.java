package de.mosig.gigabitzauber.jancontrol.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.mosig.gigabitzauber.jancontrol.config.JcJacksonConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Curve {
    @JsonDeserialize(using = JcJacksonConfig.CurveTypeDeserializer.class)
    private CurveType type;
    @Builder.Default
    private Set<CurvePoint> points = new HashSet<>();

    public int getY(int x) {
        return type.createInterpolator(this).interpolate(x);
    }
}
