package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.util.JcInterpolator;

public interface CurveType {
    JcInterpolator createInterpolator(Curve curve);
}
