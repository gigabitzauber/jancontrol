package de.gigabitzauber.jancontrol.domain;

import de.gigabitzauber.jancontrol.domain.api.Device;
import de.gigabitzauber.jancontrol.domain.api.ReferableDevice;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReferableDeviceTest {
    private static final String REF_EXAMPLE = "refExample";
    private static final String SYS_FS_PATH_EXAMPLE = "sysFsPathExample";

    private final ReferableDevice underTest = new ReferableDevice(REF_EXAMPLE, SYS_FS_PATH_EXAMPLE) {
    };

    @Test
    void test_referableDevice_inherits_from_device() {
        assertThat(underTest).isInstanceOf(Device.class);
    }

    @Test
    void test_getRef() {
        assertThat(underTest.getRef()).isEqualTo(REF_EXAMPLE);
    }
}
