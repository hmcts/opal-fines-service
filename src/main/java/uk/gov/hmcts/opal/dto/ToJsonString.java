package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface ToJsonString {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper()
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

    default String toPrettyJson() {
        try {
            return toPrettyJsonString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    default JsonNode toJsonNode() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(this.toJsonString());
    }

    static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
