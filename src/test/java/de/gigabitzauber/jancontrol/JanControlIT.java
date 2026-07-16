package de.gigabitzauber.jancontrol;

import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.cruise.SimpleCruiseAlgorithm;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.CurveTypes;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.FanModes;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(OutputCaptureExtension.class)
class JanControlIT {

    private static final String RPM_DEVICE_NAME_A = "rpmDeviceA";
    private static final String TEMP_DEVICE_NAME_A = "tempDeviceA";

    private static final String RPM_DEVICE_NAME_B = "rpmDeviceB";
    private static final String TEMP_DEVICE_NAME_B = "tempDeviceB";

    private static final String TEMP_DEVICE_NAME_C = "tempDeviceC";

    private static final Duration INTERVAL_EXAMPLE = Duration.ofSeconds(2);
    private static final Duration EXECUTOR_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SPRING_SHUTDOWN_TIMEOUT = Duration.ofSeconds(40);

    private final ExecutorService testExecutor = Executors.newFixedThreadPool(5);
    private final AtomicReference<ConfigurableApplicationContext> ctx = new AtomicReference<>();

    @TempDir
    private Path tempDir;

    private int lastFoundOutputIndex = -1;
    private Path rpmDeviceFilePathA;
    private Path rpmDeviceModeFilePathA;
    private Path tempDeviceFilePathA;

    private Path rpmDeviceFilePathB;
    private Path rpmDeviceModeFilePathB;
    private Path tempDeviceFilePathB;

    private Path tempDeviceFilePathC;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        rpmDeviceFilePathA = tempDir.resolve(RPM_DEVICE_NAME_A);
        rpmDeviceModeFilePathA = tempDir.resolve(RPM_DEVICE_NAME_A + "_enable");
        tempDeviceFilePathA = tempDir.resolve(TEMP_DEVICE_NAME_A);

        rpmDeviceFilePathB = tempDir.resolve(RPM_DEVICE_NAME_B);
        rpmDeviceModeFilePathB = tempDir.resolve(RPM_DEVICE_NAME_B + "_enable");
        tempDeviceFilePathB = tempDir.resolve(TEMP_DEVICE_NAME_B);

        tempDeviceFilePathC = tempDir.resolve(TEMP_DEVICE_NAME_C);
    }

    @AfterEach
    void tearDown() {
        testExecutor.shutdownNow();

        var successfulShutdown = false;
        try {
            successfulShutdown = testExecutor.awaitTermination(EXECUTOR_SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("WARN: Interrupted while waiting for test executor to shut down");
        }

        if (!successfulShutdown) {
            System.err.println("WARN: Test executor shut down timed out");
        }

        if (ctx.get() != null && !ctx.get().isClosed()) {
            ctx.get().close();
        }

        await().atMost(SPRING_SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(ctx.get().isClosed()).isTrue());
    }

    @Test
    void test_happy_path(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(configFilePath);

        write(tempDeviceFilePathA, "30000");
        write(tempDeviceFilePathB, "30000");
        write(tempDeviceFilePathC, "30000");
        var expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 30, 20, RPM_DEVICE_NAME_A);
        var expectedActionOnB = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 30, 33, RPM_DEVICE_NAME_B);
        assertOutput(output, "Calculated RPM value for rpmDeviceA exceeds safe limits.");
        assertOutput(output, "Setting RPM value for rpmDeviceA to lowest allowed value: 20");
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        write(tempDeviceFilePathA, "40000");
        write(tempDeviceFilePathB, "40000");
        write(tempDeviceFilePathC, "40000");
        expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 25, RPM_DEVICE_NAME_A);
        expectedActionOnB = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_C, 40, 45, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        write(tempDeviceFilePathA, "50000");
        write(tempDeviceFilePathB, "50000");
        write(tempDeviceFilePathC, "50000");
        expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 50, 50, RPM_DEVICE_NAME_A);
        expectedActionOnB = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 50, 55, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        write(tempDeviceFilePathA, "60000");
        write(tempDeviceFilePathB, "60000");
        write(tempDeviceFilePathC, "60000");
        expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 60, 75, RPM_DEVICE_NAME_A);
        expectedActionOnB = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_C, 60, 70, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        write(tempDeviceFilePathA, "70000");
        write(tempDeviceFilePathB, "71000");
        write(tempDeviceFilePathC, "72000");
        expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 70, 100, RPM_DEVICE_NAME_A);
        expectedActionOnB = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 71, 77, RPM_DEVICE_NAME_B);
        assertOutput(output, "Calculated RPM value for rpmDeviceA exceeds safe limits.");
        assertOutput(output, "Setting RPM value for rpmDeviceA to highest allowed value: 100");
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        ctx.get().close();

        assertOutput(output, "=== Stats ===");
        assertOutput(output, "Highest measurement for tempDeviceA: 70");
        assertOutput(output, "Highest measurement for tempDeviceB: 71");
        assertOutput(output, "Highest measurement for tempDeviceC: 72");
    }

    @Test
    void when_mode_file_is_altered_then_change_it_back(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(configFilePath);

        write(tempDeviceFilePathA, "40000");
        var expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 25, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        write(rpmDeviceModeFilePathA, FanModes.SMART_FAN_IV.rawValue());
        assertOutput(output, "Encountered external change of fan mode for " + RPM_DEVICE_NAME_A + ". Enforcing mode " + FanModes.MANUAL);
        assertFileContent(rpmDeviceModeFilePathA, FanModes.MANUAL.rawValue());

        write(tempDeviceFilePathA, "50000");
        expectedActionOnA = new SimpleCruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 50, 50, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);
    }

    private void assertFileContent(Path filePath, String expectedContent) {
        await().atMost(INTERVAL_EXAMPLE.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(filePath).content().isEqualTo(expectedContent));
    }

    private void assertAction(CapturedOutput output, SimpleCruiseAlgorithm.RpmCandidate expectedAction) {
        assertOutput(output, expectedAction.toString());
    }

    private void assertOutput(CapturedOutput output, String expectedOutput) {
        await().atMost(INTERVAL_EXAMPLE.multipliedBy(2).toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                int foundOutputIndex = output.getAll().indexOf(expectedOutput, lastFoundOutputIndex);
                assertThat(foundOutputIndex)
                    .as("Could not find expected output: " + expectedOutput)
                    .isGreaterThan(lastFoundOutputIndex);
                lastFoundOutputIndex = foundOutputIndex;
            });
    }

    private void startApp(Path configFilePath) {
        testExecutor.submit(() -> ctx.set(new SpringApplicationBuilder(JanControlApplication.class)
            .web(WebApplicationType.NONE)
            .run("-v", configFilePath.toString())));
    }

    private Path createConfig() throws Exception {
        var rpmDeviceA = new RpmDevice(RPM_DEVICE_NAME_A, rpmDeviceFilePathA.toString());
        write(rpmDeviceFilePathA, "100");
        write(rpmDeviceModeFilePathA, "5");
        var tempDeviceA = new TemperatureDevice(TEMP_DEVICE_NAME_A, tempDeviceFilePathA.toString());
        write(tempDeviceFilePathA, "30000");

        var rpmDeviceB = new RpmDevice(RPM_DEVICE_NAME_B, rpmDeviceFilePathB.toString());
        write(rpmDeviceFilePathB, "100");
        write(rpmDeviceModeFilePathB, "5");
        var tempDeviceB = new TemperatureDevice(TEMP_DEVICE_NAME_B, tempDeviceFilePathB.toString());
        write(tempDeviceFilePathB, "10000");

        var tempDeviceC = new TemperatureDevice(TEMP_DEVICE_NAME_C, tempDeviceFilePathC.toString());
        write(tempDeviceFilePathC, "10000");

        var curveA = Curve.builder()
            .ref(TEMP_DEVICE_NAME_A)
            .type(CurveTypes.LINEAR)
            .points(Set.of(
                new CurvePoint(30, 10),
                new CurvePoint(40, 25),
                new CurvePoint(50, 50),
                new CurvePoint(60, 75),
                new CurvePoint(70, 110)
            ))
            .build();

        var curveB = Curve.builder()
            .ref(TEMP_DEVICE_NAME_B)
            .type(CurveTypes.LINEAR)
            .points(Set.of(
                new CurvePoint(30, 33),
                new CurvePoint(40, 44),
                new CurvePoint(50, 55),
                new CurvePoint(60, 66),
                new CurvePoint(70, 77)
            ))
            .build();

        var curveC = Curve.builder()
            .ref(TEMP_DEVICE_NAME_C)
            .type(CurveTypes.LINEAR)
            .points(Set.of(
                new CurvePoint(30, 30),
                new CurvePoint(40, 45),
                new CurvePoint(50, 50),
                new CurvePoint(60, 70),
                new CurvePoint(70, 75)
            ))
            .build();

        var fanOne = Fan.builder()
            .interval(INTERVAL_EXAMPLE)
            .device(rpmDeviceA)
            .dependsOn(List.of(tempDeviceA))
            .curves(Set.of(curveA))
            .build();

        var fanTwo = Fan.builder()
            .interval(INTERVAL_EXAMPLE)
            .device(rpmDeviceB)
            .dependsOn(List.of(tempDeviceB, tempDeviceC))
            .curves(Set.of(curveB, curveC))
            .build();

        var config = new CruiseConfig(Set.of(fanOne, fanTwo));

        var yamlMapper = new JcJacksonConfig().yamlMapper();
        var configData = yamlMapper.writeValueAsString(config);
        var configFilePath = tempDir.resolve("config_file.yaml");
        Files.writeString(configFilePath, configData);
        return configFilePath;
    }

    private static void write(Path filePath, String value) {
        try {
            Files.writeString(filePath, value,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC);
        } catch (IOException e) {
            Assertions.fail("Could not write to test data file", e);
        }
    }
}
