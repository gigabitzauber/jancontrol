package de.gigabitzauber.jancontrol.domain.api;

import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.interpolation.JcInterpolator;

@FunctionalInterface
public interface CurveType {
    JcInterpolator createInterpolator(Curve curve);
}
