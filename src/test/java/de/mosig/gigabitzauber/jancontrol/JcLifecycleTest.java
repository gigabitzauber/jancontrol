package de.mosig.gigabitzauber.jancontrol;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class JcLifecycleTest {
    @Mock
    private ListeningScheduledExecutorService executorMock;

    @Mock
    private Logger logMock;

    @InjectMocks
    private JcLifecycle underTest;

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
}
