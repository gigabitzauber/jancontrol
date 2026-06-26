package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.util.JcInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurveTest {
    @Mock
    private CurveType curveTypeMock;

    private Curve underTest;

    @BeforeEach
    void setUp() {
        underTest = Curve.builder()
            .type(curveTypeMock)
            .points(Set.of(
                new CurvePoint(20, 100),
                new CurvePoint(10, 0)
            )).build();
    }

    @Test
    void test_setGetType() {
        var localCurveMock = mock(CurveType.class);

        underTest.setType(localCurveMock);
        assertThat(underTest.getType()).isEqualTo(localCurveMock);
    }

    @Test
    void test_setGetPoints() {
        var localPoints = new HashSet<CurvePoint>();
        underTest.setPoints(localPoints);
        assertThat(underTest.getPoints()).isSameAs(localPoints);
    }

    @Test
    void test_getY_primes_interpolator() {
        simulateInterpolator();

        underTest.getY(0);

        verify(curveTypeMock).createInterpolator(underTest);
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE})
    void test_getY_returns_interpolator_result(int xCandidate) {
        var localInterpolatorMock = simulateInterpolator();
        var expectedResult = new Random().nextInt();
        when(localInterpolatorMock.interpolate(xCandidate)).thenReturn(expectedResult);

        assertThat(underTest.getY(xCandidate)).isEqualTo(expectedResult);

        verify(localInterpolatorMock).interpolate(xCandidate);
    }

    private JcInterpolator simulateInterpolator() {
        var localInterpolatorMock = mock(JcInterpolator.class);
        when(curveTypeMock.createInterpolator(underTest)).thenReturn(localInterpolatorMock);

        return localInterpolatorMock;
    }
}
