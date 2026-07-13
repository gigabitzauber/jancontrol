package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.interpolation.JcInterpolator;

@FunctionalInterface
public interface CurveType {
    JcInterpolator createInterpolator(Curve curve);
}
