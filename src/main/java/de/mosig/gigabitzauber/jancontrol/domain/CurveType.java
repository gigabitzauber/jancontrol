package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.util.JcInterpolator;
import de.mosig.gigabitzauber.jancontrol.util.PieceWiseInterpolator;

import java.util.function.Function;

public enum CurveType {
    LINEAR(PieceWiseInterpolator::new);

    private final Function<Curve, JcInterpolator> interpolatorFunc;

    CurveType(Function<Curve, JcInterpolator> interpolatorFunc) {
        this.interpolatorFunc = interpolatorFunc;
    }

    public JcInterpolator getInterpolator(Curve curve) {
        return this.interpolatorFunc.apply(curve);
    }
}
