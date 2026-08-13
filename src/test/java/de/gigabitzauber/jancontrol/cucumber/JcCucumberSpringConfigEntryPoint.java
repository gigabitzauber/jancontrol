package de.gigabitzauber.jancontrol.cucumber;

import de.gigabitzauber.jancontrol.JanControlApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// This is glue code, not a test itself
@SuppressWarnings("NewClassNamingConvention")
@CucumberContextConfiguration
@ContextConfiguration(classes = {
    JanControlApplication.class
})
@ExtendWith(SpringExtension.class)
@BootstrapWith(JcCucumberSpringConfigEntryPoint.Bootstrapper.class)
@MockitoSpyBean(types = Logger.class)
public class JcCucumberSpringConfigEntryPoint {
    static class Bootstrapper extends SpringBootTestContextBootstrapper {
        static class ArgumentSupplyingContextLoader extends SpringBootContextLoader {
            @Override
            protected SpringApplication getSpringApplication() {
                return new SpringApplication() {
                    @Override
                    public ConfigurableApplicationContext run(String... args) {
                        String configFilePathArg = null;
                        Path testDataDirPath = null;
                        Path configFilePath = null;
                        try {
                            testDataDirPath = Files.createTempDirectory(JcCucumberTestSuite.class.getSimpleName() + "TestData");
                            configFilePath = Files.createTempFile(testDataDirPath, JcCucumberTestSuite.class.getSimpleName() + "ConfigFile", ".yaml");
                            configFilePathArg = configFilePath.toString();

                        } catch (IOException e) {
                            Assertions.fail("Could not set up test data temp dir", e);
                        }
                        var result = super.run("-v", "-w", configFilePathArg);
                        var configFileData = new JcCucumberConfigFileData();
                        configFileData.setTestDataDirPath(testDataDirPath);
                        configFileData.setConfigFilePath(configFilePath);

                        result.getBeanFactory().registerResolvableDependency(configFileData.getClass(), configFileData);
                        return result;
                    }
                };
            }
        }

        @Override
        protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
            return ArgumentSupplyingContextLoader.class;
        }
    }
}
