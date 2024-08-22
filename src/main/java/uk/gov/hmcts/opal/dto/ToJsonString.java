package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        return toPrettyJsonString(this);
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

    default JsonNode toJsonNode() throws JsonProcessingException {
        return toJsonNode(this.toJsonString());
    }

    static JsonNode toJsonNode(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(json);
    }

    static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
