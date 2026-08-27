package de.gigabitzauber.jancontrol.util;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JcNodeUtilTest {
    private static final String PROPERTY_FIELD_NAME = "field";

    private final ObjectNode parentNode = JsonNodeFactory.instance.objectNode();

    @Test
    void must_be_tool_class() {
        var modifiers = JcNodeUtil.class.getModifiers();

        assertThat(Modifier.isPublic(modifiers)).isTrue();
        assertThat(Modifier.isFinal(modifiers)).isTrue();

        var constructors = JcNodeUtil.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterCount()).isZero();
        assertThat(Modifier.isPrivate(constructors[0].getModifiers())).isTrue();
    }

    @Test
    void when_safeGetNode_reads_text_field_then_return_text() {
        var expectedValue = "expectedValue";
        parentNode.put(PROPERTY_FIELD_NAME, expectedValue);

        var result = JcNodeUtil.safeGetNode(parentNode, PROPERTY_FIELD_NAME);

        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    void when_safeGetNode_reads_numeric_field_then_return_text_representation() {
        int expectedValue = 42;
        parentNode.put(PROPERTY_FIELD_NAME, expectedValue);

        var result = JcNodeUtil.safeGetNode(parentNode, PROPERTY_FIELD_NAME);

        assertThat(result).isEqualTo(expectedValue + "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "", "   "})
    void when_safeGetNode_reads_missing_field_then_return_null(String fieldNameExample) {
        var result = JcNodeUtil.safeGetNode(parentNode, fieldNameExample);

        assertThat(result).isNull();
    }

    @Test
    void when_safeGetNode_receives_null_parent_then_throw_exception() {
        assertThatThrownBy(() -> JcNodeUtil.safeGetNode(null, "field"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("parent must not be null");
    }

    @Test
    void when_safeGetNode_receives_null_field_name_then_throw_exception() {
        assertThatThrownBy(() -> JcNodeUtil.safeGetNode(parentNode, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fieldName must not be null");
    }

    @Test
    void when_assertNode_reads_text_field_then_return_text() throws Exception {
        var expectedValue = "expectedValue";
        parentNode.put(PROPERTY_FIELD_NAME, expectedValue);

        var result = JcNodeUtil.assertNode(parentNode, PROPERTY_FIELD_NAME);

        assertThat(result).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "", "   "})
    void when_assertNode_reads_missing_field_then_throw_parse_exception(String fieldNameExample) {
        assertThatThrownBy(() -> JcNodeUtil.assertNode(parentNode, fieldNameExample))
            .isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class)
            .hasMessage("Field '" + fieldNameExample + "' is missing.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void when_assertNode_reads_empty_field_then_throw_parse_exception(String blankValueExample) {
        parentNode.put(PROPERTY_FIELD_NAME, blankValueExample);

        assertThatThrownBy(() -> JcNodeUtil.assertNode(parentNode, PROPERTY_FIELD_NAME))
            .isInstanceOf(com.fasterxml.jackson.core.JsonParseException.class)
            .hasMessage("Field '" + PROPERTY_FIELD_NAME + "' is missing a proper value.");
    }

    @Test
    void when_assertNode_receives_null_parent_then_throw_exception() {
        assertThatThrownBy(() -> JcNodeUtil.assertNode(null, PROPERTY_FIELD_NAME))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("parent must not be null");
    }

    @Test
    void when_assertNode_receives_null_field_name_then_throw_exception() {
        var parentNode = JsonNodeFactory.instance.objectNode();

        assertThatThrownBy(() -> JcNodeUtil.assertNode(parentNode, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("fieldName must not be null");
    }
}
