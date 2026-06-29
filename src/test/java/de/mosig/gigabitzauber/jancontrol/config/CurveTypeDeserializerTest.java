package de.mosig.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import de.mosig.gigabitzauber.jancontrol.domain.CurveTypes;
import de.mosig.gigabitzauber.jancontrol.error.JcException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurveTypeDeserializerTest {
    @Mock
    private JsonParser jsonParserMock;

    @Mock
    private DeserializationContext deserializationContextMock;

    private final JcJacksonConfig.CurveTypeDeserializer underTest = new JcJacksonConfig.CurveTypeDeserializer();

    @ParameterizedTest
    @ValueSource(strings = {"linear", "LINEAR", "Linear", "LiNeAr"})
    void test_deserialize_happy_path(String input) throws Exception {
        when(jsonParserMock.getText()).thenReturn(input);

        var result = underTest.deserialize(jsonParserMock, deserializationContextMock);

        assertThat(result).isEqualTo(CurveTypes.LINEAR);
    }

    @ParameterizedTest
    @EmptySource
    @NullSource
    @ValueSource(strings = {"   "})
    void when_curve_type_is_blank_then_error(String textCandidate) throws IOException {
        when(jsonParserMock.getText()).thenReturn(textCandidate);

        assertThatThrownBy(() -> underTest.deserialize(jsonParserMock, deserializationContextMock))
            .isInstanceOf(JcException.class)
            .hasMessage("Curve type must not be blank");
    }

    void when_curve_type_unknown_then_error() throws IOException {
        var unknownCurveType = "unknown";
        when(jsonParserMock.getText()).thenReturn(unknownCurveType);

        assertThatThrownBy(() -> underTest.deserialize(jsonParserMock, deserializationContextMock))
            .isInstanceOf(JcException.class)
            .hasMessage("Unknown curve type: " + unknownCurveType);
    }
}
