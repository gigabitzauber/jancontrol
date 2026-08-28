package de.gigabitzauber.jancontrol.test;

import com.google.common.io.MoreFiles;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.util.JcErrorUtil;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;

public final class JcTestHwmonInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String TEST_DIR_ROOT_PROP_KEY = JcTestHwmonInitializer.class.getSimpleName() + "test.dir.root";

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        Path tempRoot = null;
        try {
            tempRoot = Files.createTempDirectory(JcTestHwmonInitializer.class.getSimpleName());
        } catch (IOException e) {
            Assertions.fail("Could not create temp dir.", e);
        }

        var hwmonClassDir = JcHwmonTestHelper.createHwmonClassDir(tempRoot, "thinkpad");
        addInlinedPropertiesToEnvironment(
            applicationContext, JcJacksonConfig.HWMON_CLASS_DIR_PROP_KEY + "=" + hwmonClassDir);
        addInlinedPropertiesToEnvironment(
            applicationContext, TEST_DIR_ROOT_PROP_KEY + "=" + tempRoot);
    }

    public static void tearDown(ApplicationContext ctx) {
        if (ctx != null) {
            var rawTestDirRoot = ctx.getEnvironment().getProperty(TEST_DIR_ROOT_PROP_KEY);
            if (rawTestDirRoot != null) {
                var testDir = Path.of(rawTestDirRoot);
                if (Files.exists(testDir)) {
                    try {
                        MoreFiles.deleteRecursively(testDir);
                    } catch (IOException e) {
                        var msg = JcErrorUtil.safeGetMessage(e);
                        System.err.println("[ERROR] Could not delete test temp directory: " + testDir + " - " + msg);
                    }
                }
            }
        }
    }

    public static Path getTestTempRoot(ApplicationContext ctx) {
        return safeGetPath(ctx, TEST_DIR_ROOT_PROP_KEY);
    }

    public static Path getHwmonRoot(ApplicationContext ctx) {
        return safeGetPath(ctx, JcJacksonConfig.HWMON_CLASS_DIR_PROP_KEY);
    }

    public static @NonNull Path safeGetPath(ApplicationContext ctx, String hwmonClassDirPropKey) {
        var rawResult = safeGetProp(ctx, hwmonClassDirPropKey);
        var result = Path.of(rawResult);

        assertThat(result).exists();

        return result;
    }

    public static String safeGetProp(ApplicationContext ctx, String propKey) {
        var propVal = ctx.getEnvironment().getProperty(propKey);
        assertThat(propVal).as("application context does not contain property " + propKey).isNotNull();

        return propVal;
    }
}
