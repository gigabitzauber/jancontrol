package de.gigabitzauber.jancontrol.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.common.util.StringUtils;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public final class JcNodeUtil {
    private JcNodeUtil() {
    }

    public static String safeGetNode(JsonNode parent, String fieldName) {
        requireNonNull(parent, "parent must not be null");
        requireNonNull(fieldName, "fieldName must not be null");

        return Optional.ofNullable(parent.get(fieldName)).map(JsonNode::asText).orElse(null);
    }

    public static String assertNode(JsonNode parent, String fieldName) throws JsonParseException {
        requireNonNull(parent, "parent must not be null");
        requireNonNull(fieldName, "fieldName must not be null");

        var resultNode = parent.get(fieldName);
        if (resultNode == null) {
            throw new JsonParseException("Field '" + fieldName + "' is missing.");
        }

        var result = resultNode.asText();
        if (StringUtils.isBlank(result)) {
            throw new JsonParseException("Field '" + fieldName + "' is missing a proper value.");
        }

        return result;
    }
}
