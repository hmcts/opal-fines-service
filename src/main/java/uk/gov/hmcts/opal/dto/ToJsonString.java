package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface ToJsonString {

    default String toJsonString() throws JsonProcessingException {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .writeValueAsString(this);
    }
}
