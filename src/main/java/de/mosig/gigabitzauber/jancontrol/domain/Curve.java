package de.mosig.gigabitzauber.jancontrol.domain;

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
    private CurveType type;
    @Builder.Default
    private Set<CurvePoint> points = new HashSet<>();

    public int getY(int x) {
        return type.getInterpolator(this).interpolate(x);
    }
}
