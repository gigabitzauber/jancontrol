package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.interpolation.JcInterpolator;

@FunctionalInterface
public interface CurveType {
    JcInterpolator createInterpolator(Curve curve);
}
