package de.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.gigabitzauber.jancontrol.cruise.CruiseConfigReader;
import de.gigabitzauber.jancontrol.domain.CurveType;
import de.gigabitzauber.jancontrol.domain.CurveTypes;
import de.gigabitzauber.jancontrol.error.JcException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.annotation.DurationFormat;
import org.springframework.format.datetime.standard.DurationFormatterUtils;

import java.io.IOException;
import java.time.Duration;

@Configuration
public class JcJacksonConfig {
    @Bean
    public YAMLMapper yamlMapper() {
        var result = new YAMLMapper();
        result.registerModule(new JavaTimeModule());
        return result;
    }

    @Bean
    public CruiseConfigReader jcConfigReader(YAMLMapper yamlMapper) {
        return new CruiseConfigReader(yamlMapper);
    }

    public static final class CurveTypeDeserializer extends StdDeserializer<CurveType> {

        CurveTypeDeserializer() {
            super(CurveType.class);
        }

        @Override
        public CurveType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var rawText = p.getText();
            if (Strings.isBlank(rawText)) {
                throw new JcException("Curve type must not be blank");
            } else {
                var fixedText = rawText.toUpperCase();
                try {
                    return CurveTypes.valueOf(fixedText);
                } catch (IllegalArgumentException e) {
                    throw new JcException("Unknown curve type: " + rawText, e);
                }
            }
        }
    }

    public static final class DurationDeserializer extends StdDeserializer<Duration> {

        DurationDeserializer() {
            super(Duration.class);
        }

        @Override
        public Duration deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var rawText = p.getText();
            if (Strings.isBlank(rawText)) {
                throw new JcException("Duration must not be blank");
            } else {
                return DurationFormatterUtils.detectAndParse(p.getText());
            }
        }
    }

    public static final class DurationSerializer extends JsonSerializer<Duration> {
        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String serializedValue = null;
            if (value != null) {
                serializedValue = DurationFormatterUtils.print(value, DurationFormat.Style.SIMPLE, DurationFormat.Unit.SECONDS);
            }
            gen.writeString(serializedValue);
        }
    }
}
