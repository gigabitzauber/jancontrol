package de.mosig.gigabitzauber.jancontrol.command;

import de.mosig.gigabitzauber.jancontrol.domain.CurvePoint;
import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CruiseCommand {
    public void execute(JcConfig config) {
        var fan = config.getFans().stream().findFirst().get();
        var interpolator = new PieceWiseInterpolator(fan.getCurve());

        var inputs = new int[]{45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100};

        for (var input : inputs) {
            System.out.println(input + " -> " + interpolator.interpolate(input));
        }
    }

    private static final class PieceWiseInterpolator {
        private final int[] piecesX;
        private final int[] piecesY;

        public PieceWiseInterpolator(Set<CurvePoint> curve) {
            this.piecesX = curve.stream().map(CurvePoint::getTemp).sorted().mapToInt(Integer::intValue).toArray();
            this.piecesY = curve.stream().map(CurvePoint::getRpm).sorted().mapToInt(Integer::intValue).toArray();
        }

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
}
