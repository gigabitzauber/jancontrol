package de.gigabitzauber.jancontrol.cucumber.steps;

import com.google.common.io.MoreFiles;
import de.gigabitzauber.jancontrol.cucumber.JcCucumberConfigFileData;
import io.cucumber.java.After;
import io.cucumber.spring.ScenarioScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;

@ScenarioScope
@Slf4j
public class ConfigFileHooks {

    private final JcCucumberConfigFileData configFileData;

    @Autowired
    public ConfigFileHooks(JcCucumberConfigFileData configFileData) {
        this.configFileData = configFileData;
    }

    @After
    public void afterScenario() {
        var testDataDirPath = this.configFileData.getTestDataDirPath();
        try {
            if (testDataDirPath != null && Files.exists(testDataDirPath)) {
                MoreFiles.deleteRecursively(testDataDirPath);
            }
        } catch (IOException e) {
            var errMsg = "Could not delete test data directory " + testDataDirPath;
            log.warn(errMsg, e);
        }
    }
}
