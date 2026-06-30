package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.interpolation.JcInterpolator;
import de.mosig.gigabitzauber.jancontrol.interpolation.PieceWiseInterpolator;

import java.util.function.Function;

public enum CurveTypes implements CurveType {
    LINEAR(PieceWiseInterpolator::new);

    private final Function<Curve, JcInterpolator> interpolatorFunc;

    CurveTypes(Function<Curve, JcInterpolator> interpolatorFunc) {
        this.interpolatorFunc = interpolatorFunc;
    }

    @Override
    public JcInterpolator createInterpolator(Curve curve) {
        return this.interpolatorFunc.apply(curve);
    }
}
