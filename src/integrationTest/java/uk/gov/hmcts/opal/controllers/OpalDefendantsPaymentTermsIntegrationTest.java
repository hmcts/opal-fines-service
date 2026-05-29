package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.support.UserServiceStub.stubUserWithNoPermissions;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@Slf4j(topic = "opal.OpalDefendantsPaymentTermsIntegrationTest")
class OpalDefendantsPaymentTermsIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    @MockitoSpyBean
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Test
    @DisplayName("OPAL: Add Payment Terms – Happy Path [@PO-1718]")
    void test_Opal_AddPaymentTerms_Happy() throws Exception {
        authorise((short) 78, FinesPermission.AMEND_PAYMENT_TERMS);

        Integer currentVersion = versionFor(77L);
        HttpHeaders headers = authorisedHeaders(validToken, "78", "\"" + currentVersion + "\"");

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
            .andExpect(jsonPath("$.payment_terms.posted_details.posted_by_name").value("opal-test@HMCTS.NET"));
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Unauthorized when missing auth header [@PO-1718]")
    void test_Opal_AddPaymentTerms_Unauthorized() throws Exception {
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
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Forbidden when user lacks permission [@PO-1718]")
    void test_Opal_AddPaymentTerms_Forbidden() throws Exception {
        stubUserWithNoPermissions((short) 78);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
    void test_Opal_AddPaymentTerms_NotFound_WrongBU() throws Exception {
        authorise((short) 99, FinesPermission.AMEND_PAYMENT_TERMS);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
    void test_Opal_AddPaymentTerms_IfMatchConflict() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
    void test_Opal_AddPaymentTerms_IfMatchMissing() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
    void test_Opal_AddPaymentTerms_SchemaValidation() throws Exception {
        authoriseAllPermissions();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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

    @Test
    @DisplayName("OPAL: Add Payment Terms - Timeout [@PO-1718]")
    void test_Opal_AddPaymentTerms_Timeout() throws Exception {
        authoriseAllPermissions();
        stubAddPaymentTermsProxyException(REQUEST_TIMEOUT, "Timeout");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
            .andExpect(status().isRequestTimeout());
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Service unavailable [@PO-1718]")
    void test_Opal_AddPaymentTerms_ServiceUnavailable() throws Exception {
        authoriseAllPermissions();
        stubAddPaymentTermsProxyException(SERVICE_UNAVAILABLE, "Gateway down");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("OPAL: Add Payment Terms - Server error [@PO-1718]")
    void test_Opal_AddPaymentTerms_ServerError() throws Exception {
        authoriseAllPermissions();
        stubAddPaymentTermsProxyException(INTERNAL_SERVER_ERROR, "Boom");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(validToken);
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
            .andExpect(status().isInternalServerError());
    }

    private void stubAddPaymentTermsProxyException(org.springframework.http.HttpStatus status, String reason) {
        doThrow(new ResponseStatusException(status, reason))
            .when(defendantAccountServiceProxy)
            .addPaymentTerms(
                any(Long.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(AddDefendantAccountPaymentTermsRequest.class)
            );
    }
}
