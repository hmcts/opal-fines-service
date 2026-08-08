package uk.gov.hmcts.opal.generated.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class GetResultByIdResponseResultsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    class Serialization {

        @Test
        void whenOptionalFieldsAreNull_omitsThem() throws Exception {
            JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(responseWithNullOptionalFields()));

            assertAll(
                () -> assertTrue(json.has("result_id")),
                () -> assertFalse(json.has("imposition_allocation_priority")),
                () -> assertFalse(json.has("imposition_creditor")),
                () -> assertFalse(json.has("imposition_category")),
                () -> assertFalse(json.has("imposition_accruing")),
                () -> assertFalse(json.has("result_parameters")),
                () -> assertFalse(json.has("requires_employment_data")),
                () -> assertFalse(json.has("allow_payment_terms")),
                () -> assertFalse(json.has("requires_lja")),
                () -> assertFalse(json.has("enf_next_permitted_actions"))
            );
        }

        @Test
        void whenOptionalFieldIsNullWithAlways_includesIt() throws Exception {
            JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(responseWithNullOptionalFields()));

            assertAll(
                () -> assertTrue(json.has("allow_additional_action")),
                () -> assertTrue(json.get("allow_additional_action").isNull())
            );
        }
    }

    private GetResultByIdResponseResults responseWithNullOptionalFields() {
        return GetResultByIdResponseResults.builder()
            .resultId("ABC")
            .build();
    }
}
