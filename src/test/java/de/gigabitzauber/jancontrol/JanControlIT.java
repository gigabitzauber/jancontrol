package de.gigabitzauber.jancontrol;

import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.cruise.CruiseAlgorithm;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;
import de.gigabitzauber.jancontrol.domain.Curve;
import de.gigabitzauber.jancontrol.domain.CurvePoint;
import de.gigabitzauber.jancontrol.domain.CurveTypes;
import de.gigabitzauber.jancontrol.domain.Fan;
import de.gigabitzauber.jancontrol.domain.RpmDevice;
import de.gigabitzauber.jancontrol.domain.TemperatureDevice;
import de.gigabitzauber.jancontrol.drivers.hwmon.Nct6775FanModes;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static de.gigabitzauber.jancontrol.JcLifecycle.ERROR_THRESHOLD;
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
    public static final Duration LOG_MESSAGE_ASSERTION_TIMEOUT = INTERVAL_EXAMPLE.multipliedBy(5);
    private static final Duration EXECUTOR_SHUTDOWN_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SPRING_SHUTDOWN_TIMEOUT = Duration.ofSeconds(40);

    private final ExecutorService testExecutor = Executors.newFixedThreadPool(5);
    private final AtomicReference<ConfigurableApplicationContext> ctx = new AtomicReference<>();

    @TempDir
    private Path tempDir;

    private final AtomicInteger lastOutputAssertion = new AtomicInteger(0);
    private int outputAssertionIndex = 0;

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
    void when_no_config_file_specified_then_startup_but_do_nothing(CapturedOutput output) {
        startApp();
        assertOutput(output, "No config file specified.");
        assertOutput(output, "No fans specified. Running in NOP mode.");
    }

    @Test
    void when_empty_config_file_specified_then_startup_but_do_nothing(CapturedOutput output) throws Exception {
        var configFilePath = writeToConfigFile("fans:\n");
        startApp(configFilePath);
        assertOutput(output, "No fans specified. Running in NOP mode.");
    }

    @Test
    void test_happy_path(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(configFilePath);

        assertOutput(output, "No watch flag found. NOT watching config file for changes.");
        assertOutput(output, "Registering fan '%s' with allowIdle: false and activation threshold: 20%%".formatted(RPM_DEVICE_NAME_A));
        assertOutput(output, "Registering fan '%s' with allowIdle: false and activation threshold: 20%%".formatted(RPM_DEVICE_NAME_B));

        assertOutput(output, "Calculated RPM value for rpmDeviceA exceeds safe limits.");
        assertOutput(output, "Setting RPM value for rpmDeviceA to lowest allowed value: 20");
        var expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 30, 39, 20, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        discardOldOutput();

        write(tempDeviceFilePathB, "30000");
        write(tempDeviceFilePathC, "30000");
        var expectedActionOnB = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 30, 33, 33, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnB);

        discardOldOutput();

        write(tempDeviceFilePathA, "40000");
        write(tempDeviceFilePathB, "40000");
        write(tempDeviceFilePathC, "40000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 20, 25, RPM_DEVICE_NAME_A);
        expectedActionOnB = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_C, 40, 33, 45, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        discardOldOutput();

        write(tempDeviceFilePathA, "50000");
        write(tempDeviceFilePathB, "50000");
        write(tempDeviceFilePathC, "50000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 50, 25, 50, RPM_DEVICE_NAME_A);
        expectedActionOnB = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 50, 45, 55, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        discardOldOutput();

        write(tempDeviceFilePathA, "60000");
        write(tempDeviceFilePathB, "60000");
        write(tempDeviceFilePathC, "60000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 60, 50, 75, RPM_DEVICE_NAME_A);
        expectedActionOnB = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_C, 60, 55, 70, RPM_DEVICE_NAME_B);
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        discardOldOutput();

        write(tempDeviceFilePathA, "70000");
        write(tempDeviceFilePathB, "71000");
        write(tempDeviceFilePathC, "72000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 70, 75, 100, RPM_DEVICE_NAME_A);
        expectedActionOnB = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_B, 71, 70, 77, RPM_DEVICE_NAME_B);
        assertOutput(output, "Calculated RPM value for rpmDeviceA exceeds safe limits.");
        assertOutput(output, "Setting RPM value for rpmDeviceA to highest allowed value: 100");
        assertAction(output, expectedActionOnA);
        assertAction(output, expectedActionOnB);

        discardOldOutput();

        ctx.get().close();

        assertOutput(output, "=== Stats ===");
        assertOutput(output, "Highest measurement for tempDeviceA: 70");
        assertOutput(output, "Highest measurement for tempDeviceB: 71");
        assertOutput(output, "Highest measurement for tempDeviceC: 72");

        assertNotInFullOutput(output, "Encountered external change of fan mode");
    }

    @Test
    void when_mode_file_is_altered_then_change_it_back(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(configFilePath);

        write(tempDeviceFilePathA, "40000");
        var expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 39, 25, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        discardOldOutput();

        write(rpmDeviceModeFilePathA, Nct6775FanModes.SMART_FAN_IV.rawValue());
        assertOutput(output, "Encountered external change of fan mode for " + RPM_DEVICE_NAME_A + ". Enforcing mode " + Nct6775FanModes.MANUAL);
        assertFileContent(rpmDeviceModeFilePathA, Nct6775FanModes.MANUAL.rawValue());

        discardOldOutput();

        write(tempDeviceFilePathA, "50000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 50, 25, 50, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);
    }

    @Test
    void when_cruise_ultimately_fails_then_it_is_not_rescheduled(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(configFilePath);

        write(tempDeviceFilePathA, "40000");
        var expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 39, 25, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        discardOldOutput();

        Files.delete(tempDeviceFilePathA);
        assertOutput(output,
            "exhausted error threshold of " + ERROR_THRESHOLD + " for error: fan cruise (" + RPM_DEVICE_NAME_A + ") "
                + "ran into error: Path does not exist: " + tempDeviceFilePathA);
        assertOutput(output, "Putting " + RPM_DEVICE_NAME_A + " into emergency mode.");
    }

    @Test
    void test_override_safety_margins(CapturedOutput output) throws Exception {
        var expectedActivationThresholdPercent = 13;
        var configFilePath = createConfigWithActiveIdleFlag(expectedActivationThresholdPercent);
        startApp(configFilePath);

        write(tempDeviceFilePathA, "30000");
        var expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 30, 39, 0, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        discardOldOutput();

        write(tempDeviceFilePathA, "40000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 40, 0, 0, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        discardOldOutput();

        write(tempDeviceFilePathA, "50000");
        expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, 50, 0, expectedActivationThresholdPercent, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);

        assertNotInFullOutput(output, "Calculated RPM value for " + RPM_DEVICE_NAME_A + " exceeds safe limits.");
        assertNotInFullOutput(output, "Setting RPM value for " + RPM_DEVICE_NAME_B + " to lowest allowed value: 20");
    }

    @Test
    void test_config_reload(CapturedOutput output) throws Exception {
        var configFilePath = createConfig();
        startApp(List.of("-v", "-w"), configFilePath);

        assertOutput(output, "Registering fan '" + RPM_DEVICE_NAME_A + "' with allowIdle: false and activation threshold: 20%");
        discardOldOutput();
        assertOutput(output, "Setting " + RPM_DEVICE_NAME_A);
        discardOldOutput();

        int activationThresholdExample = 15;
        createConfigWithActiveIdleFlag(activationThresholdExample);
        assertOutput(output, "Encountered changes in config. Reloading..");
        assertOutput(output, "Registering fan '" + RPM_DEVICE_NAME_A + "' with allowIdle: true and activation threshold: " + activationThresholdExample + "%");

        discardOldOutput();

        var newTemp = 45;
        write(tempDeviceFilePathA, newTemp * 1000 + "");
        var expectedActionOnA = new CruiseAlgorithm.RpmCandidate(TEMP_DEVICE_NAME_A, newTemp, 15, 15, RPM_DEVICE_NAME_A);
        assertAction(output, expectedActionOnA);
        tearDown();
        assertOutput(output, "Highest measurement for tempDeviceA: " + newTemp);
    }

    private void assertFileContent(Path filePath, String expectedContent) {
        await().atMost(LOG_MESSAGE_ASSERTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(filePath).content().isEqualTo(expectedContent));
    }

    private void assertAction(CapturedOutput output, CruiseAlgorithm.RpmCandidate expectedAction) {
        assertOutput(output, expectedAction.toString());
    }

    private void discardOldOutput() {
        outputAssertionIndex = lastOutputAssertion.get();
    }

    private void assertOutput(CapturedOutput output, String expectedOutput) {
        await().atMost(LOG_MESSAGE_ASSERTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> {
                var localOutput = output.getAll().substring(outputAssertionIndex);
                assertThat(localOutput).contains(expectedOutput);
                lastOutputAssertion.set(localOutput.length());
            });
    }

    private void assertNotInFullOutput(CapturedOutput output, String expectedOutput) {
        await().atMost(LOG_MESSAGE_ASSERTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
            .untilAsserted(() -> assertThat(output.getAll()).doesNotContain(expectedOutput));
    }

    private void startApp(Path... configFilePaths) {
        startApp(List.of("-v"), configFilePaths);
    }

    private void startApp(List<String> flags, Path... configFilePaths) {
        var args = new ArrayList<>(flags);

        if (configFilePaths.length > 0) {
            args.add(configFilePaths[0].toString());
        }

        testExecutor.submit(() -> ctx.set(new SpringApplicationBuilder(JanControlApplication.class)
            .web(WebApplicationType.NONE)
            .run(args.toArray(new String[0]))));
    }

    private Path createConfig() throws Exception {
        var rpmDeviceA = RpmDevice.builder()
            .name(RPM_DEVICE_NAME_A)
            .sysPath(rpmDeviceFilePathA.toString())
            .build();
        write(rpmDeviceFilePathA, "100");
        write(rpmDeviceModeFilePathA, Nct6775FanModes.SMART_FAN_IV.rawValue());
        var tempDeviceA = new TemperatureDevice(TEMP_DEVICE_NAME_A, tempDeviceFilePathA.toString());
        write(tempDeviceFilePathA, "30000");

        var rpmDeviceB = RpmDevice.builder()
            .name(RPM_DEVICE_NAME_B)
            .sysPath(rpmDeviceFilePathB.toString())
            .build();
        write(rpmDeviceFilePathB, "100");
        write(rpmDeviceModeFilePathB, Nct6775FanModes.SMART_FAN_IV.rawValue());
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

        return writeToConfigFile(fanOne, fanTwo);
    }

    private Path createConfigWithActiveIdleFlag(int activationRpmPercent) throws Exception {
        var rpmDeviceA = RpmDevice.builder()
            .name(RPM_DEVICE_NAME_A)
            .sysPath(rpmDeviceFilePathA.toString())
            .allowIdle(true)
            .activationThreshold(activationRpmPercent)
            .build();
        write(rpmDeviceFilePathA, "100");
        write(rpmDeviceModeFilePathA, Nct6775FanModes.SMART_FAN_IV.rawValue());
        var tempDeviceA = new TemperatureDevice(TEMP_DEVICE_NAME_A, tempDeviceFilePathA.toString());
        write(tempDeviceFilePathA, "30000");

        var curveA = Curve.builder()
            .ref(TEMP_DEVICE_NAME_A)
            .type(CurveTypes.LINEAR)
            .points(Set.of(
                new CurvePoint(30, 0),
                new CurvePoint(40, activationRpmPercent - 1),
                new CurvePoint(50, activationRpmPercent),
                new CurvePoint(60, 75),
                new CurvePoint(70, 110)
            ))
            .build();

        var fanOne = Fan.builder()
            .interval(INTERVAL_EXAMPLE)
            .device(rpmDeviceA)
            .dependsOn(List.of(tempDeviceA))
            .curves(Set.of(curveA))
            .build();

        return writeToConfigFile(fanOne);
    }

    private @NonNull Path writeToConfigFile(Fan... fans) throws Exception {
        var fanList = Arrays.asList(fans);
        var config = new CruiseConfigRoot(fanList);

        var yamlMapper = new JcJacksonConfig().yamlMapper();
        var configData = yamlMapper.writeValueAsString(config);
        return writeToConfigFile(configData);
    }

    private Path writeToConfigFile(String content) throws Exception {
        var configFilePath = tempDir.resolve("config_file.yaml");
        Files.writeString(configFilePath, content);
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
