package uk.gov.hmcts.opal.controllers.r1a;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.testutil.JsonErrorAssertions.expectBadRequestWithoutStatus;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@ActiveProfiles({"legacy"})
@Slf4j(topic = "opal.LegacyDraftAccountIntegrationTest")
@DisplayName("DraftAccountController Integration Tests (LEGACY)")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1a=true",
    "launchdarkly.default-flag-values.is-legacy-mode=true"
})
public class LegacyDraftAccountIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Publish draft account does not return legacy server error in response [@PO-2819]")
    @JiraStory("PO-2819")
    @JiraEpic("PO-2220")
    @JiraTestKey("PO-5945")
    void testPublishDraftAccountGobServerErrorIsNotReturnedInResponse() throws Exception {

        long draftAccountId = 3L;
        ResultActions actions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .with(userStateStub.getAuthenticaitonRequestPostProcessor())
            .header("authorization", userStateStub.getBearerToken())
            .header("If-Match", getIfMatchForDraftAccount(draftAccountId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("73", "Publishing Pending", "A")));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDefendantAccountsSearch: Response body:\n{}", ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Result FCOST is not valid")))) // =server error msg
            .andExpect(content().string(containsString("An error was encountered during publication of the account")));
    }

    @Test
    @DisplayName("PO-10909: Create draft account - Should reject TFO fine originator from prosecutors in legacy mode")
    @JiraStory("PO-10909")
    @JiraEpic("PO-9031")
    void shouldRejectTfoFineOriginatorWhenItUsesProsecutorReferenceData() throws Exception {
        String request = createRequestBody("Fine", "TFO", VALID_PROSECUTOR_ORIGINATOR_ID,
            VALID_PROSECUTOR_ORIGINATOR_NAME);

        mockMvc.perform(post(URL_BASE)
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header("authorization", userStateStub.getBearerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(expectBadRequestWithoutStatus(
                expectedSingleReferenceValidationError("$.originator_id: local justice area id 32010 does not exist"),
                "https://hmcts.gov.uk/problems/invalid-reference-validation"
            ));
    }

    private String createRequestBody(String accountType, String originatorType, long originatorId,
        String originatorName) {
        return createValidCreateRequestBody()
            .replace("\"account_type\": \"Fine\"", "\"account_type\": \"" + accountType + "\"")
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"" + originatorType + "\"")
            .replace("\"originator_name\": \"" + VALID_FINE_ORIGINATOR_NAME + "\"",
                "\"originator_name\": \"" + originatorName + "\"")
            .replace("\"originator_id\": " + VALID_FINE_ORIGINATOR_ID, "\"originator_id\": " + originatorId);
    }

    private static String createValidCreateRequestBody() {
        return """
            {
              "business_unit_id": 78,
              "account": {
                "account_type": "Fine",
                "defendant_type": "Adult",
                "originator_name": "%s",
                "originator_id": %d,
                "originator_type": "NEW",
                "enforcement_court_id": 260000000048,
                "collection_order_made": true,
                "collection_order_made_today": false,
                "payment_card_request": true,
                "account_sentence_date": "2023-12-01",
                "defendant": {
                  "company_flag": false,
                  "title": "Mr",
                  "surname": "LNAME",
                  "forenames": "John",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "address_line_2": "Suite 45",
                  "post_code": "AB1 2CD",
                  "telephone_number_home": "0123456789",
                  "telephone_number_mobile": "07712345678",
                  "email_address_1": "john.doe@example.com",
                  "national_insurance_number": "AB123456C",
                  "nationality_1": "British",
                  "occupation": "Engineer",
                  "debtor_detail": {
                    "document_language": "EN",
                    "hearing_language": "EN",
                    "vehicle_make": "Toyota",
                    "vehicle_registration_mark": "ABC123",
                    "aliases": [
                      {
                        "alias_forenames": "Jon",
                        "alias_surname": "Smith"
                      }
                    ]
                  }
                },
                "offences": [
                  {
                    "date_of_sentence": "2023-11-15",
                    "imposing_court_id": 260000000048,
                    "offence_id": 35014,
                    "impositions": [
                      {
                        "result_id": "FO",
                        "amount_imposed": 500.00,
                        "amount_paid": 200.00,
                        "major_creditor_id": null
                      }
                    ],
                    "business_unit_id": "78"
                  }
                ],
                "payment_terms": {
                  "payment_terms_type_code": "P",
                  "effective_date": "2023-11-01",
                  "instalment_period": "M",
                  "lump_sum_amount": 1000.00,
                  "instalment_amount": 200.00,
                  "default_days_in_jail": 5
                },
                "account_notes": [
                  {
                    "account_note_serial": 1,
                    "account_note_text": "Defendant requested an installment plan.",
                    "note_type": "AC"
                  }
                ]
              },
              "account_type": "Fine",
              "account_status": "Submitted",
              "version": 0
            }""".formatted(VALID_FINE_ORIGINATOR_NAME, VALID_FINE_ORIGINATOR_ID);
    }

    private static String expectedSingleReferenceValidationError(String detail) {
        return """
            Draft account reference validation failed with 1 error(s):
             - %s
            """.formatted(detail).stripIndent().stripTrailing();
    }

}
