package de.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.gigabitzauber.jancontrol.config.JcJacksonConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.domain.CruiseConfigRoot;
import de.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.COLLECTION;

class CruiseConfigIT {

    private Path configFilePath;

    private static final Resource RESOURCE_EXAMPLE = new ClassPathResource("/config_file_example.yaml");
    private final YAMLMapper mapper = new JcJacksonConfig().yamlMapper();

    private CruiseConfig underTest;

    @BeforeEach
    public void setUp() throws Exception {
        var mapper = new JcJacksonConfig().yamlMapper();

        configFilePath = Files.createTempFile(this.getClass().getSimpleName(), "yaml");
        setConfigFileContent(RESOURCE_EXAMPLE.getContentAsString(StandardCharsets.UTF_8));
        var tempResource = new FileSystemResource(configFilePath);

        underTest = new CruiseConfig(tempResource, mapper);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Files.deleteIfExists(configFilePath);
    }

    @Test
    void does_not_accept_null_resource() {
        assertThatThrownBy(() -> new CruiseConfig(null, mapper))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("configResource must not be null");
    }

    @Test
    void does_not_accept_null_mapper() {
        assertThatThrownBy(() -> new CruiseConfig(RESOURCE_EXAMPLE, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("mapper must not be null");
    }

    @Test
    void test_read_happy_path() {
        assertThat(underTest.load()).isNotNull();
    }

    @Test
    void test_read_failure_path() {
        var faultyFile = new ClassPathResource("/faulty_config_file_example.yaml");
        var localUnderTest = new CruiseConfig(faultyFile, mapper);

        assertThatThrownBy(localUnderTest::load)
            .isInstanceOf(JcException.class)
            .hasMessage("Config file contains faulty YAML")
            .hasRootCauseInstanceOf(InvalidFormatException.class);
    }

    @Test
    void when_file_content_has_not_changed_then_return_false() {
        assertThat(underTest.hasChanged()).isFalse();
    }

    @Test
    void when_file_content_has_changed_then_return_false() {
        setConfigFileContent("Changed Content");

        assertThat(underTest.hasChanged()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void when_config_is_blank_then_return_empty_config_root(String contentExample) {
        setConfigFileContent(contentExample);

        var result = underTest.load();

        assertThat(result)
            .extracting(CruiseConfigRoot::fans)
            .asInstanceOf(COLLECTION)
            .isEmpty();
    }

    @Test
    void when_config_contains_empty_fan_collection_then_return_empty_config_root() {
        setConfigFileContent("fans:");

        var result = underTest.load();

        assertThat(result)
            .extracting(CruiseConfigRoot::fans)
            .asInstanceOf(COLLECTION)
            .isEmpty();
    }

    private void setConfigFileContent(String expectedContent) {
        try {
            Files.writeString(configFilePath, expectedContent, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            Assertions.fail("Could not write temp config file contents", e);
        }
    }
}
