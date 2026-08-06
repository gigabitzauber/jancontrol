package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.error.JcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FanCruiseExecutorTest {
    @Mock
    private ListeningScheduledExecutorService executorMock;

    private FanCruiseExecutor underTest;

    @BeforeEach
    void setUp() {
        underTest = new FanCruiseExecutor(() -> this.executorMock);
    }

    @Test
    void test_executor_termination() throws Exception {
        simulateSuccessfulExecutorTermination();

        underTest.terminate();

        verify(executorMock).shutdownNow();
        verify(executorMock).awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    void when_termination_fails_then_throw_exception() throws Exception {
        simulateFailedExecutorTermination();

        assertThatThrownBy(() -> underTest.terminate())
            .isInstanceOf(JcException.class)
            .hasMessage("Fan cruise executor termination timed out")
            .hasNoCause();
    }

    @Test
    void when_waiting_for_termination_is_interrupted_then_throw_exception() throws Exception {
        var expectedCause = new InterruptedException("expected exception");
        simulateFailedExecutorTermination(expectedCause);

        assertThatThrownBy(() -> underTest.terminate())
            .isInstanceOf(JcException.class)
            .hasMessage("Interrupted while waiting for fan cruise to stop")
            .hasRootCause(expectedCause);
    }

    @Test
    void test_schedule_registers_lifecycle_as_callback() {
        var scheduledFuture = simulateFuture();

        var lifecycleMock = mock(JcLifecycle.class);
        try (var staticFuturesMock = mockStatic(Futures.class)) {
            underTest.scheduleAtFixedRate(() -> {
            }, Duration.ZERO, Duration.ZERO, lifecycleMock);
            staticFuturesMock.verify(() -> Futures.addCallback(scheduledFuture, lifecycleMock, executorMock));
        }
    }

    @Test
    void test_schedule_schedules() {
        simulateFuture();
        var lifecycleMock = mock(JcLifecycle.class);

        Runnable expectedRunnable = mock(Runnable.class);
        var expectedInitialDelay = Duration.ofMillis(11);
        var expectedPeriod = Duration.ofMillis(22);

        underTest.scheduleAtFixedRate(
            expectedRunnable,
            expectedInitialDelay,
            expectedPeriod,
            lifecycleMock);

        verify(executorMock).scheduleAtFixedRate(expectedRunnable, expectedInitialDelay.toMillis(), expectedPeriod.toMillis(), TimeUnit.MILLISECONDS);
    }

    private ListenableScheduledFuture<?> simulateFuture() {
        ListenableScheduledFuture<?> futureMock = mock(ListenableScheduledFuture.class);
        when(executorMock.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenAnswer(_ -> futureMock);

        return futureMock;
    }

    private void simulateSuccessfulExecutorTermination() throws InterruptedException {
        when(executorMock.awaitTermination(anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    private void simulateFailedExecutorTermination(Exception... exceptions) throws InterruptedException {
        OngoingStubbing<Boolean> whenTerminate = when(executorMock.awaitTermination(anyLong(), any(TimeUnit.class)));
        if (exceptions.length > 0) {
            whenTerminate.thenThrow(exceptions[0]);
        } else {
            whenTerminate.thenReturn(false);
        }
    }
}
