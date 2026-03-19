package uk.gov.hmcts.opal.entity.draft;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class DraftAccountTypeJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void jsonShouldSerialiseUsingLabel() throws Exception {
        assertEquals("\"Fine\"", objectMapper.writeValueAsString(DraftAccountType.FINE));
    }

    @Test
    void jsonShouldDeserialiseUsingLabel() throws Exception {
        DraftAccountType value = objectMapper.readValue("\"Fine\"", DraftAccountType.class);
        assertEquals(DraftAccountType.FINE, value);
    }
}
