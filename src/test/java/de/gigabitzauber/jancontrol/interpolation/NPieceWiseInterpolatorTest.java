package de.gigabitzauber.jancontrol.interpolation;

import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NPieceWiseInterpolatorTest {

    private static final int FIRST_X = 20;
    private static final int LAST_X = 40;
    private static final int MID_X = 30;

    @Test
    void when_constructed_with_null_curve_then_throw_null_pointer_exception() {
        assertThatThrownBy(() -> createUnderTest(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("curve must not be null");
    }

    @Test
    void extends_pieceWiseInterpolator() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);
        assertThat(underTest).isInstanceOf(PieceWiseInterpolator.class);
    }

    @ParameterizedTest
    @MethodSource("interpolationExamples")
    void interpolate_returns_only_multiples_of_n(Integer x, Integer expectedResult) {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        assertThat(underTest.interpolate(x)).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> interpolationExamples() {
        return Stream.of(
            arguments(FIRST_X, 40),
            arguments(MID_X, 45),
            arguments(LAST_X, 45)
        );
    }

    private static Curve simulateCurve() {
        return simulateCurve(
            new CurvePoint(FIRST_X, 42),
            new CurvePoint(MID_X, 45),
            new CurvePoint(LAST_X, 43)
        );
    }

    private static Curve simulateCurve(CurvePoint... points) {
        return Curve.builder()
            .n(5)
            .points(Set.of(points))
            .build();
    }

    private static PieceWiseInterpolator createUnderTest(Curve curve) {
        return new NPieceWiseInterpolator(curve);
    }
}
