package de.gigabitzauber.jancontrol.interpolation;

import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PieceWiseInterpolatorTest {

    private static final int BELOW_FIRST_X = 10;
    private static final int FIRST_Y = 100;
    private static final int FIRST_X = 20;
    private static final int ABOVE_LAST_X = 50;
    private static final int LAST_Y = 200;
    private static final int LAST_X = 40;
    private static final int MID_X = 30;
    private static final int MID_Y = 150;

    @Test
    void when_constructed_with_null_curve_then_throw_null_pointer_exception() {
        assertThatThrownBy(() -> createUnderTest(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("curve must not be null");
    }

    @Test
    void when_x_is_below_first_point_then_return_first_y() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(BELOW_FIRST_X);

        assertThat(result).isEqualTo(FIRST_Y);
    }

    @Test
    void when_x_equals_first_point_then_return_first_y() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(FIRST_X);

        assertThat(result).isEqualTo(FIRST_Y);
    }

    @Test
    void when_x_is_above_last_point_then_return_last_y() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(ABOVE_LAST_X);

        assertThat(result).isEqualTo(LAST_Y);
    }

    @Test
    void when_x_equals_last_point_then_return_last_y() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(LAST_X);

        assertThat(result).isEqualTo(LAST_Y);
    }

    @Test
    void when_x_is_between_two_points_then_interpolate_linearly() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(25);

        assertThat(result).isEqualTo(125);
    }

    @Test
    void when_x_is_between_two_points_with_non_integer_result_then_ceil_result() {
        var curve = simulateCurve(
            new CurvePoint(FIRST_X, FIRST_Y),
            new CurvePoint(30, 155)
        );
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(23);

        assertThat(result).isEqualTo(117);
    }

    @Test
    void when_x_is_between_two_points_with_negative_slope_then_interpolate_correctly() {
        var curve = simulateCurve(
            new CurvePoint(FIRST_X, LAST_Y),
            new CurvePoint(MID_X, FIRST_Y)
        );
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(25);

        assertThat(result).isEqualTo(MID_Y);
    }

    @Test
    void when_curve_has_single_point_and_x_below_then_return_that_y() {
        var curve = simulateCurve(new CurvePoint(MID_X, MID_Y));
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(10);

        assertThat(result).isEqualTo(MID_Y);
    }

    @Test
    void when_curve_has_single_point_and_x_above_then_return_that_y() {
        var curve = simulateCurve(new CurvePoint(MID_X, MID_Y));
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(ABOVE_LAST_X);

        assertThat(result).isEqualTo(MID_Y);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1000, -1})
    void when_x_is_negative_below_first_point_then_return_first_y(int xCandidate) {
        var curve = simulateCurve(
            new CurvePoint(FIRST_X, FIRST_Y),
            new CurvePoint(MID_X, MID_Y)
        );
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(xCandidate);

        assertThat(result).isEqualTo(FIRST_Y);
    }

    @Test
    void when_curve_has_unordered_points_then_interpolate_correctly() {
        var curve = simulateCurve(
            new CurvePoint(LAST_X, LAST_Y),
            new CurvePoint(FIRST_X, FIRST_Y),
            new CurvePoint(MID_X, MID_Y)
        );
        var underTest = createUnderTest(curve);

        var result = underTest.interpolate(25);

        assertThat(result).isEqualTo(125);
    }

    @Test
    void when_interpolating_multiple_times_then_results_are_consistent() {
        var curve = simulateCurve();
        var underTest = createUnderTest(curve);

        var resultA = underTest.interpolate(25);
        var resultB = underTest.interpolate(25);

        assertThat(resultA).isEqualTo(resultB).isEqualTo(125);
    }

    private static Curve simulateCurve() {
        return simulateCurve(
            new CurvePoint(FIRST_X, FIRST_Y),
            new CurvePoint(MID_X, MID_Y),
            new CurvePoint(LAST_X, LAST_Y)
        );
    }

    private static Curve simulateCurve(CurvePoint... points) {
        return Curve.builder().points(Set.of(points)).build();
    }

    private static PieceWiseInterpolator createUnderTest(Curve curve) {
        return new PieceWiseInterpolator(curve);
    }
}
