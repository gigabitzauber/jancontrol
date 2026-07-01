package de.mosig.gigabitzauber.jancontrol.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NamedDeviceTest {
    private static final String NAME_EXAMPLE = "nameExample";
    private static final String SYS_FS_PATH_EXAMPLE = "sysFsPathExample";

    private final NamedDevice underTest = new NamedDevice(NAME_EXAMPLE, SYS_FS_PATH_EXAMPLE) {
    };

    @Test
    void test_namedDevice_inherits_from_device() {
        assertThat(underTest).isInstanceOf(Device.class);
    }

    @Test
    void test_getName() {
        assertThat(underTest.getName()).isEqualTo(NAME_EXAMPLE);
    }
}
