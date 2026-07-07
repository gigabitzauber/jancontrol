package de.mosig.gigabitzauber.jancontrol.cruise;

import de.mosig.gigabitzauber.jancontrol.JcLifecycle;
import de.mosig.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CruiseCommandTest {
    private static final Fan FAN_EXAMPLE_A = Fan.builder().interval(Duration.ofSeconds(10)).build();
    private static final Fan FAN_EXAMPLE_B = Fan.builder().interval(Duration.ofSeconds(5)).build();
    private static final CruiseConfig CONFIG_EXAMPLE = new CruiseConfig(Set.of(FAN_EXAMPLE_A, FAN_EXAMPLE_B));

    @Mock
    private JcLifecycle lifecycleMock;

    @InjectMocks
    private CruiseCommand underTest;

    @Test
    void test_does_not_support_null_lifecycle() {
        assertThatThrownBy(() -> new CruiseCommand(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("lifecycle must not be null");
    }

    @Test
    void test_execute_does_not_support_null_config() {
        assertThatThrownBy(() -> underTest.execute(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("config must not be null");
    }

    @Test
    void test_registers_every_fan_with_lifecycle() {
        underTest.execute(CONFIG_EXAMPLE);

        verify(lifecycleMock).register(FAN_EXAMPLE_A);
        verify(lifecycleMock).register(FAN_EXAMPLE_B);
        verifyNoMoreInteractions(lifecycleMock);
    }
}
