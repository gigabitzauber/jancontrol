package de.gigabitzauber.jancontrol.interpolation;

import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;

import java.util.Objects;

public final class PieceWiseInterpolator implements JcInterpolator {
    private final int[] piecesX;
    private final int[] piecesY;

    public PieceWiseInterpolator(Curve curve) {
        Objects.requireNonNull(curve, "curve must not be null");
        this.piecesX = curve.points().stream().map(CurvePoint::temp).sorted().mapToInt(Integer::intValue).toArray();
        this.piecesY = curve.points().stream().map(CurvePoint::rpm).sorted().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public int interpolate(int x) {
        if (x <= piecesX[0])
            return piecesY[0];
        if (x >= piecesX[piecesX.length - 1])
            return piecesY[piecesY.length - 1];

        for (int i = 0; i < piecesX.length - 1; i++) {
            if (x >= piecesX[i] && x <= piecesX[i + 1]) {
                double slope = (double) (piecesY[i + 1] - piecesY[i]) / (piecesX[i + 1] - piecesX[i]);
                double result = piecesY[i] + slope * (x - piecesX[i]);
                return (int) Math.ceil(result);
            }
        }
        return 0;
    }
}
