package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Optional;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public interface ToJsonString {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(new JavaTimeModule());

    default String toJsonString() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(this);
    }

    default String toJson() {
        try {
            return toJsonString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default String toPrettyJsonString() throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    static String toPrettyJsonString(Object original) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(original);
    }

    default String toPrettyJson() {
        try {
            return toPrettyJsonString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String toPrettyJson(String json) {
        try {
            return toPrettyJsonString(toJsonNode(json));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static String objectToPrettyJson(Object json) {
        try {
            return toPrettyJsonString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default JsonNode toJsonNode() throws JsonProcessingException {
        return toJsonNode(this.toJsonString());
    }

    static JsonNode toJsonNode(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(json);
    }

    static Optional<JsonNode> toOptionalJsonNode(String json) {
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(OBJECT_MAPPER.readTree(json));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    static <T> T toClassInstance(String json, Class<T> clss) {
        try {
            return OBJECT_MAPPER.readValue(json, clss);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
