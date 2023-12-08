package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface ToJsonString {

    default String toJsonString() throws JsonProcessingException {
        return newObjectMapper()
            .writeValueAsString(this);
    }

    default String toPrettyJsonString() throws JsonProcessingException {
        return newObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(this);
    }

    default JsonNode toJsonNode() throws JsonProcessingException {
        return newObjectMapper()
            .readTree(this.toJsonString());
    }

    static ObjectMapper newObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }
}
