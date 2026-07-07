package de.mosig.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.cruise.CruiseInstance;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import de.mosig.gigabitzauber.jancontrol.domain.RpmDevice;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
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
    void should_restore_old_values_when_shutting_down() throws Exception {
        var targetDeviceRpmFile = tempDir.resolve("target_test_device_rpm_file");
        var oldRpm = "100";
        Files.writeString(targetDeviceRpmFile, oldRpm, CREATE_NEW, WRITE);
        var targetDevice = new RpmDevice("rpmTestDevice", targetDeviceRpmFile.toString());
        var oldMode = "5";
        var targetDeviceModeFile = Paths.get(targetDeviceRpmFile + "_enable");
        Files.writeString(targetDeviceModeFile, oldMode, CREATE_NEW, WRITE);
        var fanExample = Fan.builder().device(targetDevice).build();

        try (var staticCruiseMock = mockStatic(CruiseInstance.class)) {
            var cruiseMock = mock(CruiseInstance.class);
            staticCruiseMock.when(() -> CruiseInstance.create(any(), any(), any(), any())).thenReturn(cruiseMock);
            underTest.register(fanExample);

        }
        assertThat(Files.readString(targetDeviceModeFile)).isEqualTo(JcLifecycle.FAN_MODE_MANUAL);

        simulateSuccessfulExecutorTermination();
        underTest.stop();
        assertThat(Files.readString(targetDeviceModeFile)).isEqualTo(oldMode);
        assertThat(Files.readString(targetDeviceRpmFile)).isEqualTo(oldRpm);
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
