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
import de.gigabitzauber.jancontrol.test.JcTestHwmonInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(
    classes = JcJacksonConfig.class,
    initializers = JcTestHwmonInitializer.class)
class ConfigLoadIT {

    private static final Resource CONFIG_FILE_EXAMPLE = new ClassPathResource("/config_file_example.yaml");

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private YAMLMapper mapper;

    private CruiseConfigRoot expectedConfig;

    @BeforeEach
    void setUp() {
        var hwmonRoot = JcTestHwmonInitializer.getHwmonRoot(applicationContext);

        expectedConfig = new CruiseConfigRoot(Set.of(
            Fan.builder()
                .interval(Duration.ofSeconds(3))
                .downSkip(5)
                .n(6)
                .device(RpmDevice.builder()
                    .ref("CPU Fan")
                    .sysName("thinkpad")
                    .slot(1)
                    .sysPath(hwmonRoot
                        .resolve("hwmon0")
                        .resolve("pwm1")
                        .toString())
                    .driver(JcHwmonDrivers.THINKPAD_ACPI)
                    .allowIdle(true)
                    .activationThreshold(15)
                    .build())
                .dependsOn(List.of(
                    TemperatureDevice.builder()
                        .ref("CPU Temp")
                        .sysName("thinkpad")
                        .slot(2)
                        .sysPath(hwmonRoot
                            .resolve("hwmon0")
                            .resolve("temp2_input").toString())
                        .build()
                ))
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
    }

    @AfterEach
    void tearDown() {
        JcTestHwmonInitializer.tearDown(applicationContext);
    }

    @Test
    void test_read_config_happy_path() {
        var localUnderTest = new CruiseConfig(CONFIG_FILE_EXAMPLE, mapper);
        var fan = localUnderTest.load();

        assertThat(fan).isEqualTo(expectedConfig);
    }
}
