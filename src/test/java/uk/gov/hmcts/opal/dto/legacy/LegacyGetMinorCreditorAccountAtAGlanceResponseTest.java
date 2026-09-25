package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse.AtAGlanceDefendant;

class LegacyGetMinorCreditorAccountAtAGlanceResponseTest {

    @Test
    void toJsonString_serializesOrganisationDefendantFieldsInSnakeCase() throws Exception {
        LegacyGetMinorCreditorAccountAtAGlanceResponse response =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .defendant(AtAGlanceDefendant.builder()
                    .organisationFlag(true)
                    .organisationName("Acme Corporation")
                    .build())
                .build();

        JsonNode defendant = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(response)
        ).get("defendant");

        assertTrue(defendant.get("organisation_flag").asBoolean());
        assertEquals("Acme Corporation", defendant.get("organisation_name").asText());
        assertFalse(defendant.has("organisationFlag"));
        assertFalse(defendant.has("organisationName"));
    }

    @Test
    void toJsonString_serializesIndividualDefendantOrganisationFlagAsFalse() throws Exception {
        LegacyGetMinorCreditorAccountAtAGlanceResponse response =
            LegacyGetMinorCreditorAccountAtAGlanceResponse.builder()
                .defendant(AtAGlanceDefendant.builder()
                    .title("Ms")
                    .forenames("Jane")
                    .surname("Doe")
                    .organisationFlag(false)
                    .build())
                .build();

        JsonNode defendant = ToJsonString.getObjectMapper().readTree(
            ToJsonString.getObjectMapper().writeValueAsString(response)
        ).get("defendant");

        assertFalse(defendant.get("organisation_flag").asBoolean());
        assertEquals("Ms", defendant.get("title").asText());
        assertEquals("Jane", defendant.get("forenames").asText());
        assertEquals("Doe", defendant.get("surname").asText());
        assertFalse(defendant.has("organisation_name"));
    }
}
