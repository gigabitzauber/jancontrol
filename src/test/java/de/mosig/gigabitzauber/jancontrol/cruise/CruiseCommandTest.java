package de.mosig.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.config.CruiseCommand;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CruiseCommandTest {
    private static final Fan FAN_EXAMPLE_A = Fan.builder().interval(Duration.ofSeconds(10)).build();
    private static final Fan FAN_EXAMPLE_B = Fan.builder().interval(Duration.ofSeconds(5)).build();
    private static final CruiseConfig CONFIG_EXAMPLE = new CruiseConfig(Set.of(FAN_EXAMPLE_A, FAN_EXAMPLE_B));

    @Mock
    private ListeningScheduledExecutorService executorMock;
    @Mock
    private JcLifecycle lifecycleMock;
    @Mock
    private Logger logMock;

    @InjectMocks
    private CruiseCommand underTest;

    @Test
    void test_does_not_support_null_executor() {
        assertThatThrownBy(() -> new CruiseCommand(null, lifecycleMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fanCruiseExecutor must not be null");
    }

    @Test
    void test_does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> new CruiseCommand(executorMock, null, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void test_does_not_support_null_logger() {
        assertThatThrownBy(() -> new CruiseCommand(executorMock, lifecycleMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("log must not be null");
    }

    @Test
    void test_execute_does_not_support_null_config() {
        assertThatThrownBy(() -> underTest.execute(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("config must not be null");
    }

    @Test
    void test_registers_every_fan_with_lifecycle() {
        simulateFuture();

        underTest.execute(CONFIG_EXAMPLE);

        verify(lifecycleMock).register(FAN_EXAMPLE_A);
        verify(lifecycleMock).register(FAN_EXAMPLE_B);
        verifyNoMoreInteractions(lifecycleMock);
    }

    @Test
    void test_schedules_cruise_for_every_fan() {
        try (var staticCruiseMock = mockStatic(CruiseInstance.class)) {
            var cruiseInstanceMock = mock(CruiseInstance.class);
            staticCruiseMock.when(() -> CruiseInstance.create(any(Fan.class), any(ListeningScheduledExecutorService.class), any(Logger.class)))
                .thenReturn(cruiseInstanceMock);

            underTest.execute(CONFIG_EXAMPLE);

            staticCruiseMock.verify(() -> CruiseInstance.create(FAN_EXAMPLE_A, executorMock, logMock));
            staticCruiseMock.verify(() -> CruiseInstance.create(FAN_EXAMPLE_B, executorMock, logMock));
            staticCruiseMock.verifyNoMoreInteractions();
        }
    }

    private void simulateFuture() {
        ListenableScheduledFuture<?> futureMock = mock(ListenableScheduledFuture.class);
        when(executorMock.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
            .thenAnswer(_ -> futureMock);
    }
}
