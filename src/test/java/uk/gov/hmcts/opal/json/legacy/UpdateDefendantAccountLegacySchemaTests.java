package uk.gov.hmcts.opal.json.legacy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateDefendantAccountLegacySchemaTests {

    private static final String SCHEMA_PATH = "/legacy/updateDefendantAccountLegacyRequest.json";

    private static final JsonSchemaValidationService validator = new JsonSchemaValidationService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("validPayloads")
    void shouldValidatePayloadsAgainstSchema(Map<String, Object> payload) throws JsonProcessingException {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        assertTrue(validator.isValid(jsonPayload, SCHEMA_PATH));
    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    void shouldInvalidateIncorrectPayloads(Map<String, Object> payload) throws JsonProcessingException {
        String jsonPayload = objectMapper.writeValueAsString(payload);
        assertFalse(validator.isValid(jsonPayload, SCHEMA_PATH));
    }

    static Stream<Map<String, Object>> validPayloads() {
        return Stream.of(
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "comment_and_notes", Map.of(
                    "account_comment", "Test comment",
                    "free_text_note_1", "Note 1"
                )
            ),
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "enforcement_court", Map.of(
                    "court_id", 1,
                    "court_name", "Central Court"
                )
            ),
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "collection_order", Map.of(
                    "collection_order_flag", true,
                    "collection_order_date", "2025-07-15"
                )
            ),
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "enforcement_overrides", Map.of(
                    "enforcement_override_id", "EO-1",
                    "enforcement_override_title", "Override Title"
                )
            )
        );
    }

    static Stream<Map<String, Object>> invalidPayloads() {
        return Stream.of(
            // Multiple optional fields
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "comment_and_notes", Map.of(
                    "account_comment", "Test comment"
                ),
                "enforcement_court", Map.of(
                    "court_id", 1,
                    "court_name", "Central Court"
                )
            ),
            // No optional fields
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1
            ),
            // Two optional fields present
            Map.of(
                "defendant_account_id", "abc123",
                "version", 1,
                "collection_order", Map.of(
                    "collection_order_flag", true,
                    "collection_order_date", "2025-07-15"
                ),
                "enforcement_overrides", Map.of(
                    "enforcement_override_id", "EO-1",
                    "enforcement_override_title", "Override Title"
                )
            )
        );
    }
}
