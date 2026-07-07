package de.mosig.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CruiseInstanceTest {
    private static final Duration DURATION_EXAMPLE = Duration.ofSeconds(6);
    private static final Fan FAN_EXAMPLE = Fan.builder().interval(DURATION_EXAMPLE).build();

    @Mock
    private JcLifecycle lifecycleMock;
    @Mock
    private ListeningScheduledExecutorService executorMock;
    @Mock
    private Logger logMock;

    private CruiseInstance underTest;

    @BeforeEach
    void setUp() {
        underTest = CruiseInstance.create(FAN_EXAMPLE, lifecycleMock, executorMock, logMock);
    }

    @Test
    void test_does_not_support_null_fan() {
        assertThatThrownBy(() -> CruiseInstance.create(null, lifecycleMock, executorMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fan must not be null");
    }

    @Test
    void test_does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> CruiseInstance.create(FAN_EXAMPLE, null, executorMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void test_does_not_support_null_executor() {
        assertThatThrownBy(() -> CruiseInstance.create(FAN_EXAMPLE, lifecycleMock, null, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("executor must not be null");
    }

    @Test
    void test_does_not_support_null_logger() {
        assertThatThrownBy(() -> CruiseInstance.create(FAN_EXAMPLE, lifecycleMock, executorMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("log must not be null");
    }

    @Test
    void onSuccess_does_nothing() {
        var objMock = mock(Object.class);

        underTest.onSuccess(objMock);
        verifyNoInteractions(objMock, lifecycleMock, executorMock, logMock);
    }

    @Test
    void test_schedule_schedules_simpleAlgo() {
        simulateFuture();

        underTest.schedule();

        verify(executorMock).scheduleAtFixedRate(
            any(SimpleCruiseAlgorithm.class),
            eq(0L),
            eq(DURATION_EXAMPLE.toMillis()),
            eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void test_schedule_registers_itself_as_callback() {
        var scheduledFuture = simulateFuture();

        try (var staticFuturesMock = mockStatic(Futures.class)) {
            underTest.schedule();
            staticFuturesMock.verify(() -> Futures.addCallback(scheduledFuture, underTest, executorMock));
        }
    }

    private ListenableScheduledFuture<?> simulateFuture() {
        ListenableScheduledFuture<?> futureMock = mock(ListenableScheduledFuture.class);
        when(executorMock.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenAnswer(_ -> futureMock);

        return futureMock;
    }
}
