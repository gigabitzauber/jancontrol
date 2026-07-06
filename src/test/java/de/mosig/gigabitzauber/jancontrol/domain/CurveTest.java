package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.interpolation.JcInterpolator;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurveTest {
    private static final Set<CurvePoint> POINTS_EXAMPLE = Set.of(
        new CurvePoint(20, 100),
        new CurvePoint(10, 0)
    );
    @Mock
    private CurveType curveTypeMock;

    private Curve underTest;

    @BeforeEach
    void setUp() {
        underTest = Curve.builder()
            .type(curveTypeMock)
            .points(POINTS_EXAMPLE).build();
    }

    @Test
    void test_noArgConstructor_sets_default_values() {
        var localUnderTest = Curve.builder().build();

        assertThat(localUnderTest.ref()).isNull();
        assertThat(localUnderTest.type()).isEqualTo(CurveTypes.LINEAR);
        assertThat(localUnderTest.points()).isEmpty();
    }

    @Test
    void test_getType() {
        assertThat(underTest.type()).isEqualTo(curveTypeMock);
    }

    @Test
    void test_getPoints() {
        assertThat(underTest.points()).isEqualTo(POINTS_EXAMPLE);
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

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(Curve.class).verify();
    }

    private JcInterpolator simulateInterpolator() {
        var localInterpolatorMock = mock(JcInterpolator.class);
        when(curveTypeMock.createInterpolator(underTest)).thenReturn(localInterpolatorMock);

        return localInterpolatorMock;
    }
}
