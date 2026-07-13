package de.gigabitzauber.jancontrol.cruise;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.gigabitzauber.jancontrol.domain.CruiseConfig;
import de.gigabitzauber.jancontrol.error.JcException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CruiseConfigReaderTest {

    @Mock
    private YAMLMapper yamlMapperMock;

    @Mock
    private Resource resourceMock;

    @InjectMocks
    private CruiseConfigReader underTest;

    @Test
    void when_mapper_is_null_then_npe() {
        assertThatThrownBy(() -> new CruiseConfigReader(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("mapper must not be null");
    }

    @Test
    void when_read_config_with_valid_yaml_then_returns_empty_config() throws Exception {
        simulateValidConfig();

        var result = underTest.readConfig(resourceMock);

        assertThat(result).isNotNull();
    }

    @Test
    void when_config_file_cannot_be_read_then_throw_exception() throws Exception {
        simulateValidConfig();
        var expectedRootCause = simulateConfigFileReadFail();

        assertThatThrownBy(() -> underTest.readConfig(resourceMock))
            .isInstanceOf(JcException.class)
            .hasMessageContaining("Could not read config file")
            .hasRootCause(expectedRootCause);
    }

    @Test
    void when_deserialising_yaml_throws_exception_then_rethrow() throws Exception {
        simulateValidConfig();
        var expectedRootCause = simulateDeserialisationFail();

        assertThatThrownBy(() -> underTest.readConfig(resourceMock))
            .isInstanceOf(JcException.class)
            .hasMessageContaining("Config file contains faulty YAML")
            .hasRootCause(expectedRootCause);
    }

    // Mockito's API does not go well with Class arguments
    @SuppressWarnings("unchecked")
    private RuntimeException simulateDeserialisationFail() throws JsonProcessingException {
        var expectedException = new RuntimeException("expected exception");
        when(yamlMapperMock.readValue(anyString(), any(Class.class))).thenThrow(expectedException);

        return expectedException;
    }

    private Exception simulateConfigFileReadFail() throws IOException {
        var expectedException = new IOException("expected exception");
        when(resourceMock.getContentAsString(StandardCharsets.UTF_8)).thenThrow(expectedException);

        return expectedException;
    }

    // Mockito's API does not go well with Class arguments
    @SuppressWarnings("unchecked")
    private void simulateDeserialisationSuccess() {
        var result = new CruiseConfig(Set.of());
        try {
            lenient().when(yamlMapperMock.readValue(anyString(), any(Class.class))).thenReturn(result);
        } catch (JsonProcessingException e) {
            Assertions.fail("This should never happen", e);
        }

    }

    private void simulateConfigFileReadSuccess() throws IOException {
        var result = "some: yaml";
        lenient().when(resourceMock.getContentAsString(StandardCharsets.UTF_8)).thenReturn(result);

    }

    private void simulateValidConfig() throws IOException {
        simulateConfigFileReadSuccess();
        simulateDeserialisationSuccess();
    }
}
