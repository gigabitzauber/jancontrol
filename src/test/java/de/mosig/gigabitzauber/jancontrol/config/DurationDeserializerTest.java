package de.mosig.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.datetime.standard.DurationFormatterUtils;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DurationDeserializerTest {

    @Mock
    private JsonParser jsonParserMock;

    @Mock
    private DeserializationContext deserializationContextMock;

    private final JcJacksonConfig.DurationDeserializer underTest = new JcJacksonConfig.DurationDeserializer();

    @ParameterizedTest
    @ValueSource(strings = {"PT10S", "PT1M", "PT1H", "PT1M30S"})
    void when_valid_iso_8601_duration_then_deserialize_to_duration(String textCandidate) throws IOException {
        when(jsonParserMock.getText()).thenReturn(textCandidate);

        var result = underTest.deserialize(jsonParserMock, deserializationContextMock);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(Duration.parse(textCandidate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"00", "0", "01", "10", "10s", "1m", "01m", "1h"})
    void when_valid_duration_format_then_deserialize_to_duration(String textCandidate) throws IOException {
        when(jsonParserMock.getText()).thenReturn(textCandidate);

        var result = underTest.deserialize(jsonParserMock, deserializationContextMock);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(DurationFormatterUtils.detectAndParse(textCandidate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"   "})
    @EmptySource
    @NullSource
    void when_duration_is_blank_then_throw_exception(String textCandidate) throws IOException {
        when(jsonParserMock.getText()).thenReturn(textCandidate);

        assertThatThrownBy(() -> underTest.deserialize(jsonParserMock, deserializationContextMock))
            .isInstanceOf(JcException.class)
            .hasMessage("Duration must not be blank");
    }

    @Test
    void when_invalid_duration_format_then_throw_exception() throws IOException {
        when(jsonParserMock.getText()).thenReturn("nan");

        assertThatThrownBy(() -> underTest.deserialize(jsonParserMock, deserializationContextMock))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not a valid duration");
    }
}
