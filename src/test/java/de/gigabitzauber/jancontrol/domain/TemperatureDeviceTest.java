package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.error.JcException;
import de.gigabitzauber.jancontrol.util.JcIoUtil;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemperatureDeviceTest {
    private static final String NAME_EXAMPLE = "readOnlyDeviceExample";
    private static final String SYS_FILE_EXAMPLE = "sysFileExample";
    private static final Path SYS_FILE_PATH_EXAMPLE = Paths.get(SYS_FILE_EXAMPLE);

    private final TemperatureDevice underTest = new TemperatureDevice(NAME_EXAMPLE, SYS_FILE_EXAMPLE);

    @Test
    void should_inherit_from_proper_parents() {
        assertThat(this.underTest).isInstanceOf(NamedDevice.class);
        assertThat(this.underTest).isInstanceOf(TypedReadableDevice.class);
    }

    @Test
    void when_constructed_with_all_args_then_properties_are_set() {
        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
        assertThat(underTest.getSysPath()).isEqualTo(SYS_FILE_EXAMPLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123000", "  123000   ", "\t 123000\n  ", "123000\n"})
    void test_read_happy_path(String rawValueCandidate) {
        var actualValue = executeReadOpSuccess(rawValueCandidate);

        assertThat(actualValue).isEqualTo(123);
    }

    @ParameterizedTest
    @ValueSource(strings = {"   ", "NaN"})
    @EmptySource
    void when_read_contents_are_not_a_number_then_throw_jc_exception(String rawValue) {
        assertThatThrownBy(() -> executeReadOpSuccess(rawValue))
            .isInstanceOf(JcException.class)
            .hasMessage("Value of device '" + NAME_EXAMPLE + "' is not a number.");
    }

    @Test
    void when_read_fails_then_throw_exception() {
        var expectedException = new JcException("expected exception");
        assertThatThrownBy(() -> executeReadOpFail(expectedException)).isSameAs(expectedException);
    }

    @Test
    void test_equals_and_hashCode_contract() {
        EqualsVerifier.forClass(TemperatureDevice.class).withRedefinedSuperclass().verify();
    }

    private Integer executeReadOpSuccess(String contents) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE)).thenReturn(contents);
            return underTest.read();
        }
    }

    private void executeReadOpFail(RuntimeException expectedException) {
        try (var staticJcIoUtilMock = Mockito.mockStatic(JcIoUtil.class)) {
            staticJcIoUtilMock.when(() -> JcIoUtil.assertReadable(SYS_FILE_PATH_EXAMPLE))
                .thenReturn(SYS_FILE_PATH_EXAMPLE);
            staticJcIoUtilMock.when(() -> JcIoUtil.readString(SYS_FILE_PATH_EXAMPLE)).thenThrow(expectedException);
            underTest.read();
        }
    }
}
