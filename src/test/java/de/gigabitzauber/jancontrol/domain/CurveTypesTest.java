package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.interpolation.PieceWiseInterpolator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CurveTypesTest {

    @Test
    void linear_type_uses_piecewise_interpolator() {
        var curve = mock(Curve.class);

        var interpolator = CurveTypes.LINEAR.createInterpolator(curve);

        assertThat(interpolator).isNotNull();
        assertThat(interpolator).isInstanceOf(PieceWiseInterpolator.class);
    }
}
