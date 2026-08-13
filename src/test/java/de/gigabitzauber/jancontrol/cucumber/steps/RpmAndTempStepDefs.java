package de.gigabitzauber.jancontrol.cucumber.steps;

import de.gigabitzauber.jancontrol.cucumber.JcCucumberConfigFileData;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.ScenarioScope;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;

@ScenarioScope
public class RpmAndTempStepDefs {

    private final JcCucumberConfigFileData configFileData;
    private final Logger logSpy;

    @Autowired
    public RpmAndTempStepDefs(JcCucumberConfigFileData configFileData, Logger logSpy) {
        this.configFileData = configFileData;
        this.logSpy = logSpy;
    }

    @Given("the configuration file {string}")
    public void theConfigurationFile(String configFileName) {
        try (var in = this.getClass().getResourceAsStream("/config_file_examples/" + configFileName)) {
            Files.copy(in, configFileData.getConfigFilePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Assertions.fail("Could not set up test configuration file", e);
        }

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
            verify(logSpy).info(matches("Encountered changes in config.*")));

    }

    @And("The temperature of {string} is {int}")
    public void theTemperatureOfIs(String arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("the temperature of {string} does not change")
    public void theTemperatureOfDoesNotChange(String arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("rpm of fan {string} is {int}")
    public void rpmOfFanIs(String arg0, int arg1) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("The temperature increases to {int}")
    public void theTemperatureIncreasesTo(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("Fan RPM is set to {int}")
    public void fanRPMIsSetTo(int arg0) {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }
}
