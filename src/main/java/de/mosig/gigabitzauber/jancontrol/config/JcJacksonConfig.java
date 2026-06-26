package de.mosig.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.mosig.gigabitzauber.jancontrol.domain.CurveType;
import de.mosig.gigabitzauber.jancontrol.domain.CurveTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JcJacksonConfig {
    @Bean
    public YAMLMapper yamlMapper() {
        return new YAMLMapper();
    }

    @Bean
    public JcConfigReader jcConfigReader(YAMLMapper yamlMapper) {
        return new JcConfigReader(yamlMapper);
    }

    public static class CurveTypeDeserializer extends StdDeserializer<CurveType> {

        protected CurveTypeDeserializer() {
            super(CurveType.class);
        }

        @Override
        public CurveType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            var enumName = p.getText().toUpperCase();
            return CurveTypes.valueOf(enumName);
        }
    }
}
