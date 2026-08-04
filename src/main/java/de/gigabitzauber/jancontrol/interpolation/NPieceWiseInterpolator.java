package de.gigabitzauber.jancontrol.interpolation;

import de.gigabitzauber.jancontrol.domain.Curve;

public final class NPieceWiseInterpolator extends PieceWiseInterpolator {

    private final int n;

    public NPieceWiseInterpolator(Curve curve) {
        super(curve);
        n = curve.n();
    }

    @Override
    public int interpolate(int x) {
        var rawResult = super.interpolate(x);

        return getNearestMultiple(rawResult, n);
    }

    /**
     * Nearest multiple algorithm without using round and double precision div.
     */
    public int getNearestMultiple(int rawCurveY, int n) {
        int remainder = rawCurveY % n;
        if (remainder < (n + 1) / 2) {
            return rawCurveY - remainder;
        } else {
            return rawCurveY + (n - remainder);
        }
    }
}
