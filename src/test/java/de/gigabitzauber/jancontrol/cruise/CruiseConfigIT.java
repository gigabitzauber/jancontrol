package de.gigabitzauber.jancontrol.cruise;

import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.CurveTypes;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JcJacksonConfig.class)
class CruiseConfigIT {

    private static final Resource CONFIG_FILE_EXAMPLE = new ClassPathResource("/config_file_example.yaml");
    private static final CruiseConfig EXPECTED_CONFIG = new CruiseConfig(Set.of(
        Fan.builder()
            .interval(Duration.ofSeconds(3))
            .device(
                new RpmDevice("CPU Fan", "/sys/devices/platform/nct6775.656/hwmon/hwmon2/pwm2"))
            .dependsOn(List.of(
                new TemperatureDevice("CPU Temp", "/sys/devices/platform/nct6775.656/hwmon/hwmon2/temp8_input")))
            .curves(Set.of(Curve.builder()
                .ref("CPU Temp")
                .type(CurveTypes.LINEAR)
                .points(
                    Set.of(
                        new CurvePoint(46, 20),
                        new CurvePoint(60, 28),
                        new CurvePoint(82, 72),
                        new CurvePoint(95, 95)
                    )).build()))
            .build()
    ));

    @Autowired
    private CruiseConfigReader underTest;

    @Test
    void test_read_config_happy_path() {
        var fan = underTest.readConfig(CONFIG_FILE_EXAMPLE);
        assertThat(fan).isEqualTo(EXPECTED_CONFIG);
    }
}
