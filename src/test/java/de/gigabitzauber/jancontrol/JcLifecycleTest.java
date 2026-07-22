package de.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.gigabitzauber.jancontrol.cruise.JcSchedulable;
import de.gigabitzauber.jancontrol.cruise.ModeEnforcer;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.error.JcSchedulableException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JcLifecycleTest {
    @TempDir
    private Path tempDir;

    @Mock
    private ListeningScheduledExecutorService executorMock;

    @Mock
    private Logger logMock;

    @InjectMocks
    private JcLifecycle underTest;

    @Test
    void must_implement_lifecycle() {
        assertThat(underTest).isInstanceOf(Lifecycle.class);
    }

    @Test
    void test_isRunning_always_returns_true() {
        assertThat(underTest.isRunning()).isTrue();
    }

    @Test
    void test_start_does_nothing() {
        underTest.start();

        verifyNoInteractions(executorMock);
        verifyNoInteractions(logMock);
    }

    @Test
    void test_stop_terminates_executor() throws Exception {
        simulateSuccessfulExecutorTermination();

        underTest.stop();

        verify(executorMock).shutdownNow();
        verify(executorMock).awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    void when_termination_fails_then_throw_exception() throws Exception {
        simulateFailedExecutorTermination();

        assertThatThrownBy(() -> underTest.stop())
            .isInstanceOf(JcException.class)
            .hasMessage("Fan cruise executor termination timed out")
            .hasNoCause();
    }

    @Test
    void when_waiting_for_termination_is_interrupted_then_throw_exception() throws Exception {
        var expectedCause = new InterruptedException("expected exception");
        simulateFailedExecutorTermination(expectedCause);

        assertThatThrownBy(() -> underTest.stop())
            .isInstanceOf(JcException.class)
            .hasMessage("Interrupted while waiting for fan cruise to stop")
            .hasRootCause(expectedCause);
    }

    @Test
    void should_restore_old_fan_settings_when_shutting_down() throws Exception {
        var targetDeviceRpmFile = tempDir.resolve("target_test_device_rpm_file");
        var oldRpm = "100";
        Files.writeString(targetDeviceRpmFile, oldRpm, CREATE_NEW, WRITE);
        var targetDevice = new RpmDevice("rpmTestDevice", targetDeviceRpmFile.toString());
        var oldMode = "5";
        var targetDeviceModeFile = Paths.get(targetDeviceRpmFile + "_enable");
        Files.writeString(targetDeviceModeFile, oldMode, CREATE_NEW, WRITE);
        var fanExample = Fan.builder().device(targetDevice).build();

        try (var staticCruiseMock = mockStatic(CruiseInstance.class); var staticEnforcerMock = mockStatic(ModeEnforcer.class)) {
            var cruiseMock = mock(CruiseInstance.class);
            staticCruiseMock.when(() -> CruiseInstance.create(any(), any(), any())).thenReturn(cruiseMock);
            var enforcerMock = mock(ModeEnforcer.class);
            staticEnforcerMock.when(() -> ModeEnforcer.create(any(), any(), any())).thenReturn(enforcerMock);
            underTest.register(fanExample);
        }

        simulateSuccessfulExecutorTermination();
        underTest.stop();
        assertThat(Files.readString(targetDeviceModeFile)).isEqualTo(oldMode);
        assertThat(Files.readString(targetDeviceRpmFile)).isEqualTo(oldRpm);
    }

    @Test
    void when_error_count_of_schedulable_is_below_threshold_then_do_not_throw_but_log_debug_only() {
        var schedulableExample = simulateSchedulable();
        var expectedErrorMsg = "expectedErrorMsg";
        var schedulableErrorExample = new JcSchedulableException(expectedErrorMsg, schedulableExample);

        assertThatNoException().isThrownBy(() -> underTest.onFailure(schedulableErrorExample));

        // False positive. This is a Mockito verification, not an actual log statement.
        //noinspection LoggingSimilarMessage
        verify(logMock).debug("Schedulable {} encountered error #{}: {}", schedulableExample.id(), 1, expectedErrorMsg);
    }

    @Test
    void when_error_count_of_schedulable_is_above_threshold_then_log_error() {
        var schedulableExample = simulateSchedulable();
        var expectedErrorMsg = "expectedErrorMsg";
        var schedulableErrorExample = new JcSchedulableException(expectedErrorMsg, schedulableExample);

        for (int i = 0; i <= JcLifecycle.ERROR_THRESHOLD; i++) {
            assertThatNoException().isThrownBy(() -> underTest.onFailure(schedulableErrorExample));
        }

        verify(logMock).error("Schedulable {} exhausted error threshold of {} for error: {}",
            schedulableExample.id(),
            JcLifecycle.ERROR_THRESHOLD,
            schedulableErrorExample.getMessage(),
            schedulableErrorExample);
    }

    @Test
    void different_kinds_of_schedulable_errors_have_their_own_error_counter() {
        var schedulableExample = simulateSchedulable();
        var expectedErrorMsgA = "expectedErrorMsgA";
        var expectedErrorMsgB = "expectedErrorMsgB";

        assertThatNoException().isThrownBy(() -> underTest.onFailure(new JcSchedulableException(expectedErrorMsgA, schedulableExample)));
        assertThatNoException().isThrownBy(() -> underTest.onFailure(new JcSchedulableException(expectedErrorMsgB, schedulableExample)));

        // False positive. This is a Mockito verification, not an actual log statement.
        //noinspection LoggingSimilarMessage
        verify(logMock).debug("Schedulable {} encountered error #{}: {}", schedulableExample.id(), 1, expectedErrorMsgA);
        // False positive. This is a Mockito verification, not an actual log statement.
        //noinspection LoggingSimilarMessage
        verify(logMock).debug("Schedulable {} encountered error #{}: {}", schedulableExample.id(), 1, expectedErrorMsgB);
    }

    @Test
    void different_kinds_of_schedulables_have_their_own_error_counter() {
        var schedulableExampleA = simulateSchedulable();
        var schedulableExampleB = simulateSchedulable();
        var expectedErrorMsg = "expectedErrorMsg";
        var schedulableErrorExampleA = new JcSchedulableException(expectedErrorMsg, schedulableExampleA);
        var schedulableErrorExampleB = new JcSchedulableException(expectedErrorMsg, schedulableExampleB);

        assertThatNoException().isThrownBy(() -> underTest.onFailure(schedulableErrorExampleA));
        assertThatNoException().isThrownBy(() -> underTest.onFailure(schedulableErrorExampleB));

        verify(logMock).debug("Schedulable {} encountered error #{}: {}", schedulableExampleA.id(), 1, expectedErrorMsg);
        verify(logMock).debug("Schedulable {} encountered error #{}: {}", schedulableExampleB.id(), 1, expectedErrorMsg);
    }

    @Test
    void when_error_is_not_from_a_schedulable_then_log_error_immediately() {
        var expectedErrorMsg = "expectedErrorMsg";
        var unexpectedErrorExample = new IOException(expectedErrorMsg);

        underTest.onFailure(unexpectedErrorExample);

        verify(logMock).error("Encountered unexpected error", unexpectedErrorExample);
    }

    private static @NonNull JcSchedulable simulateSchedulable() {
        var schedulableExample = mock(JcSchedulable.class);
        var schedulableIdExample = "schedulableIdExample";
        when(schedulableExample.id()).thenReturn(schedulableIdExample);
        return schedulableExample;
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
