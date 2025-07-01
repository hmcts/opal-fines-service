package uk.gov.hmcts.opal.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.SchemaPaths;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplaceDraftAccountRequestDtoSchemaTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonSchemaValidationService validator = new JsonSchemaValidationService();

    @Test
    void shouldMatchSchema() throws Exception {
        ReplaceDraftAccountRequestDto dto = ReplaceDraftAccountRequestDto.builder()
            .businessUnitId((short) 73)
            .submittedBy("opal-test@HMCTS.NET")
            .submittedByName("Opal Test")
            .account("""
                {
                  "account_type": "Fine",
                  "defendant_type": "Adult",
                  "originator_name": "LJS",
                  "originator_id": 1,
                  "enforcement_court_id": 123,
                  "payment_card_request": false,
                  "account_sentence_date": "2024-01-01",
                  "defendant": {
                    "company_flag": false,
                    "address_line_1": "123 High Street"
                  },
                  "offences": [
                    {
                      "offence_id": 1,
                      "date_of_sentence": "2024-01-01",
                      "impositions": [
                        {
                          "result_id": "ABC123",
                          "amount_paid": 0.0,
                          "amount_imposed": 100.0
                        }
                      ]
                    }
                  ],
                  "payment_terms": {
                    "payment_terms_type_code": "B"
                  }
                }
            """)
            .accountType("Fines")
            .accountStatus("Submitted")
            .timelineData("""
                [
                  {
                    "username": "johndoe",
                    "status": "Pending",
                    "status_date": "2024-05-01",
                    "reason_text": "Waiting for review"
                  }
                ]
            """)
            .version(0L)
            .build();

        JsonNode jsonNode = mapper.readTree(dto.toJson());

        boolean isValid = validator.isValid(
            jsonNode, SchemaPaths.DRAFT_ACCOUNT + "/replaceDraftAccountRequest.json");

        assertTrue(isValid, "DTO should conform to replaceDraftAccountRequest.json");
    }
}
