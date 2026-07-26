package de.gigabitzauber.jancontrol.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import de.gigabitzauber.jancontrol.domain.JcHwmonDriver;
import de.gigabitzauber.jancontrol.drivers.hwmon.JcHwmonDrivers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JcHwmonDriverDeserializerTest {
    @Mock
    private JsonParser jsonParserMock;

    @Mock
    private DeserializationContext deserializationContextMock;

    private final JcJacksonConfig.JcHwmonDriverDeserializer underTest = new JcJacksonConfig.JcHwmonDriverDeserializer();

    @Test
    void handledType_must_be_driver_class() {
        assertThat(underTest.handledType()).isEqualTo(JcHwmonDriver.class);
    }

    @Test
    void deserialize_just_calls_fromCfgName() throws Exception {
        var someText = "someText";
        when(jsonParserMock.getText()).thenReturn(someText);

        try (var staticJcHwmonDriversMock = mockStatic(JcHwmonDrivers.class)) {
            underTest.deserialize(jsonParserMock, deserializationContextMock);

            staticJcHwmonDriversMock.verify(() -> JcHwmonDrivers.fromCfgName(someText));
        }
    }
}
