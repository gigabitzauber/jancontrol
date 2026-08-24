package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.JcLifecycle;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CruiseInstanceTest {
    private static final Duration DURATION_EXAMPLE = Duration.ofSeconds(6);
    private static final Fan FAN_EXAMPLE = Fan.builder()
        .interval(DURATION_EXAMPLE)
        .device(RpmDevice.builder()
            .ref("rpmDeviceExample")
            .sysPath("unused")
            .build())
        .build();

    @Mock
    private JcLifecycle lifecycleMock;
    @Mock
    private FanCruiseExecutor executorMock;
    @Mock
    private Logger logMock;

    private CruiseInstance underTest;

    @BeforeEach
    void setUp() {
        underTest = CruiseInstance.create(FAN_EXAMPLE, lifecycleMock, logMock);
    }

    @Test
    void must_be_tool_class() {
        var modifiers = CruiseInstance.class.getModifiers();

        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isFinal(modifiers)).isTrue();

        var constructors = CruiseInstance.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
    }

    @Test
    void test_does_not_support_null_fan() {
        assertThatThrownBy(() -> CruiseInstance.create(null, lifecycleMock, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fan must not be null");
    }

    @Test
    void test_does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> CruiseInstance.create(FAN_EXAMPLE, null, logMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void test_schedule_does_not_support_null_executor() {
        assertThatThrownBy(() -> underTest.schedule(null, lifecycleMock))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("executor must not be null");
    }

    @Test
    void test_schedule_does_not_support_null_callback() {
        assertThatThrownBy(() -> underTest.schedule(executorMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void test_does_not_support_null_logger() {
        assertThatThrownBy(() -> CruiseInstance.create(FAN_EXAMPLE, lifecycleMock, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("log must not be null");
    }

    @Test
    void test_schedule_schedules_cruiseAlgo() {
        underTest.schedule(executorMock, lifecycleMock);

        var initialDelayCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(executorMock).scheduleAtFixedRate(
            any(Runnable.class),
            initialDelayCaptor.capture(),
            eq(DURATION_EXAMPLE),
            eq(lifecycleMock));

        assertThat(initialDelayCaptor.getValue().toMillis())
            .isBetween(0L, CruiseInstance.INITIAL_MAX_DELAY.toMillis());
    }
}
