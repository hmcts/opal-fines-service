package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;

@Slf4j(topic = "opal.OpalDefendantsPaymentTermsIntegrationTest")
class OpalDefendantsPaymentTermsIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @Test
    @DisplayName("OPAL: Add Payment Terms - account controls return 422 for blocked last enforcement")
    @JiraStory("PO-5757")
    @JiraEpic("PO-2990")
    void test_Opal_AddPaymentTerms_returns422_whenBlockedByAccountControls() throws Exception {
        // Arrange
        authorise((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        long defendantAccountId = 78L;
        Integer currentVersion = versionFor(defendantAccountId);
        HttpHeaders headers = authorisedHeaders(userStateStub.getBearerToken(), "78", "\"" + currentVersion + "\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        // Act
        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/78/payment-terms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        log.info(":opalAddPaymentTerms_accountControls response body:\n{}",
                 ToJsonString.toPrettyJson(result.andReturn().getResponse().getContentAsString()));

        // Assert
        result.andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Unprocessable Content"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value(
                "Defendant account update blocked: Payment terms last enforcement check failed because "
                    + "last_enforcement is MPSO."))
            .andExpect(jsonPath("$.retriable").value(false));

        assertEquals(currentVersion, versionFor(defendantAccountId));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms – Happy Path [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6049")
    void test_Opal_AddPaymentTerms_Happy() throws Exception {
        authorise((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders(userStateStub.getBearerToken(), "78", "\"" + currentVersion + "\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": { "payment_terms_type_code": "B",
                "payment_terms_type_display_name": "By date"},
                "instalment_period": { "instalment_period_code": "W" ,
                "instalment_period_display_name": "Weekly"},
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": true,
              "generate_payment_terms_change_letter": true
            }
            """;

        ResultActions result = mockMvc.perform(
            post("/defendant-accounts/77/payment-terms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        ).andDo(MockMvcResultHandlers.print());

        String body = result.andReturn().getResponse().getContentAsString();
        String etag = result.andReturn().getResponse().getHeader(HttpHeaders.ETAG);

        log.info(":opalAddPaymentTerms_Happy response body:\n{}", ToJsonString.toPrettyJson(body));
        log.info(":opalAddPaymentTerms_Happy ETag: {}", etag);

        result.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.payment_terms.days_in_default").value(30))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by").value("L078JG"))
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").value("Pablo"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Forbidden when missing auth header [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6045")
    void test_Opal_AddPaymentTerms_Unauthorized() throws Exception {
        userStateStub.setupWithNoPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Forbidden when user lacks permission [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6050")
    void test_Opal_AddPaymentTerms_Forbidden() throws Exception {
        userStateStub.setupWithNoPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token_without_permission");
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Not Found when account not in header BU [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6044")
    void test_Opal_AddPaymentTerms_NotFound_WrongBU() throws Exception {
        authorise((short) 99, FinesPermission.AMEND_PAYMENT_TERMS);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "99");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/entity-not-found"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Conflict when If-Match does not match [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6047")
    void test_Opal_AddPaymentTerms_IfMatchConflict() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"9999\"");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/optimistic-locking"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Conflict when If-Match missing [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6053")
    void test_Opal_AddPaymentTerms_IfMatchMissing() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");

        String requestJson = """
            {
              "payment_terms": {
                "days_in_default": 30,
                "date_days_in_default_imposed": "2025-11-05",
                "extension": true,
                "reason_for_extension": "extn reason text",
                "effective_date": "2025-11-01",
                "payment_terms_type": {
                  "payment_terms_type_code": "B",
                  "payment_terms_type_display_name": "By date"
                },
                "instalment_period": {
                  "instalment_period_code": "W",
                  "instalment_period_display_name": "Weekly"
                },
                "lump_sum_amount": 120.00,
                "instalment_amount": 10.00,
                "posted_details": {
                  "posted_by": "clerk1",
                  "posted_date": "2025-02-02T10:11:12",
                  "posted_by_name": "aa"
                }
              },
              "request_payment_card": false,
              "generate_payment_terms_change_letter": false
            }
            """;

        ResultActions a = mockMvc.perform(
            post("/defendant-accounts/77/payment-terms")
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        );

        String body = a.andReturn().getResponse().getContentAsString();
        log.info(":addPaymentTerms_IfMatchMissing body:\n{}", body);

        a.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type",
                org.hamcrest.Matchers.anyOf(is("https://hmcts.gov.uk/problems/resource-conflict"),
                    is("https://hmcts.gov.uk/problems/optimistic-locking"))));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Schema validation error [@PO-1718]")
    @JiraStory("PO-1718")
    @JiraEpic("PO-977")
    @JiraTestKey("PO-6046")
    void test_Opal_AddPaymentTerms_SchemaValidation() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userStateStub.getBearerToken());
        headers.add("Business-Unit-Id", "78");
        headers.add(HttpHeaders.IF_MATCH, "\"0\"");

        mockMvc.perform(
                post("/defendant-accounts/77/payment-terms")
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"request_payment_card\": true}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }
}
