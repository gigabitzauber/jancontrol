package de.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.CurveTypes;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
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
class ConfigLoadIT {

    private static final Resource CONFIG_FILE_EXAMPLE = new ClassPathResource("/config_file_example.yaml");
    private static final CruiseConfigRoot EXPECTED_CONFIG = new CruiseConfigRoot(Set.of(
        Fan.builder()
            .interval(Duration.ofSeconds(3))
            .downSkip(5)
            .device(RpmDevice.builder()
                .name("CPU Fan")
                .sysPath("/sys/devices/platform/thinkpad_acpi/hwmon/hwmon2/pwm2")
                .driver(JcHwmonDrivers.THINKPAD_ACPI)
                .allowIdle(true)
                .activationThreshold(15)
                .build())
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
    private YAMLMapper mapper;

    @Test
    void test_read_config_happy_path() {
        var localUnderTest = new CruiseConfig(CONFIG_FILE_EXAMPLE, mapper);
        var fan = localUnderTest.read();

        assertThat(fan).isEqualTo(EXPECTED_CONFIG);
    }
}
