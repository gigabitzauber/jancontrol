package de.mosig.gigabitzauber.jancontrol.config;

import de.mosig.gigabitzauber.jancontrol.domain.Curve;
import de.mosig.gigabitzauber.jancontrol.domain.CurvePoint;
import de.mosig.gigabitzauber.jancontrol.domain.CurveType;
import de.mosig.gigabitzauber.jancontrol.domain.Fan;
import de.mosig.gigabitzauber.jancontrol.domain.JcConfig;
import de.mosig.gigabitzauber.jancontrol.domain.ReadOnlyDevice;
import de.mosig.gigabitzauber.jancontrol.domain.WriteableDevice;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JcConfigReaderTest {

    private static final Resource CONFIG_FILE_EXAMPLE = new ClassPathResource("/config_file_example.yaml");
    private static final JcConfig EXPECTED_CONFIG = new JcConfig(Set.of(
        Fan.builder()
            .device(
                new WriteableDevice("CPU Fan", "/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm2"))
            .dependsOn(List.of(
                new ReadOnlyDevice("CPU Temp", "/sys/devices/platform/nct6775.656/hwmon/hwmon2/temp8_input")))
            .curve(Curve.builder()
                .type(CurveType.LINEAR)
                .points(
                    Set.of(
                        new CurvePoint(46, 20),
                        new CurvePoint(60, 28),
                        new CurvePoint(82, 72),
                        new CurvePoint(95, 95)
                    )).build())
            .build()
    ));

    @Test
    void test_read_config_happy_path() {
        var fan = new JcConfigReader().readConfig(CONFIG_FILE_EXAMPLE);

        assertThat(fan).isEqualTo(EXPECTED_CONFIG);
    }
}
