package de.mosig.gigabitzauber.jancontrol.domain;

import de.mosig.gigabitzauber.jancontrol.error.JcException;
import de.mosig.gigabitzauber.jancontrol.util.JcIoUtil;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RwSysFileTest {

    private static final String SYS_FILE_EXAMPLE = "sysFileExample";
    private static final Path SYS_FILE_PATH_EXAMPLE = Paths.get(SYS_FILE_EXAMPLE);

    private final RwSysFile underTest = new RwSysFile(SYS_FILE_EXAMPLE);

    @Test
    void when_readRaw_then_delegate_to_ioUtil() {
        var expectedValue = "expectedValue";

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE)).thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE)).thenReturn(expectedValue);

            var actualValue = underTest.readRaw();

            assertThat(actualValue).isEqualTo(expectedValue);
            staticJcIoUtilMock.verify(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verify(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void readRaw_delegates_ioUtil_exceptions() {
        var expectedException = new JcException("expectedException");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenThrow(expectedException);
            assertThatThrownBy(underTest::readRaw).isSameAs(expectedException);

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void when_writeRaw_with_null_then_throw_npe() {
        assertThatThrownBy(() -> underTest.writeRaw(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("rawValue must not be null");
    }

    @Test
    void when_writeRaw_then_delegate_to_ioUtil() {
        var expectedValue = "expectedValue";

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE)).thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, expectedValue)).thenAnswer(_ -> null);

            underTest.writeRaw(expectedValue);

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verify(() -> JcIoUtil.writeString(SYS_FILE_PATH_EXAMPLE, expectedValue));
            staticJcIoUtilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void writeRaw_delegates_ioUtil_exceptions() {
        var expectedException = new JcException("expectedException");

        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE))
                .thenThrow(expectedException);
            assertThatThrownBy(() -> underTest.writeRaw("")).isSameAs(expectedException);

            staticJcIoUtilMock.verify(() -> JcIoUtil.assertWritable(SYS_FILE_PATH_EXAMPLE));
            staticJcIoUtilMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(RwSysFile.class).verify();
    }
}
