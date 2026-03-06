package uk.gov.hmcts.opal.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.ToJsonString;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest00")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerIntegrationTestReplace extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Replace draft account - Should return updated draft account [@PO-973, @PO-746]")
    void testReplaceDraftAccount_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
        String requestBody = validReplaceRequestBody(0L);
        log.info(":testReplaceDraftAccount_success: Request Body:\n{}", ToJsonString.toPrettyJson(requestBody));

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testReplaceDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(5))
            .andExpect(jsonPath("$.business_unit_id").value(78))
            .andExpect(jsonPath("$.submitted_by").value("USER01"))
            .andExpect(jsonPath("$.submitted_by_name").value("normal@users.com"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Resubmitted"))
            .andExpect(jsonPath("$.account.originator_type").value("TFO"))
            .andExpect(jsonPath("$.timeline_data").isArray());

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNT_RESPONSE);

    }

    @Test
    @DisplayName("Replace draft account - Should return 400 when originator_type is missing")
    void testReplaceDraftAccount_originatorTypeIsMissing() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\",", "");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Replace draft account - Should return 400 when originator_type is blank")
    void testReplaceDraftAccount_originatorTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Replace draft account - Should return 400 when originator_type has invalid value")
    void testReplaceDraftAccount_originatorTypeIsInvalid() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"ABC\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Replace draft account - user with no permission [@PO-973, @PO-830]")
    void testReplaceDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateRequestBody()))
            .andExpect(status().isForbidden());

    }

    private static String validCreateRequestBody() {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "Adult",
                "originator_name": "Police Force",
                "originator_id": 12345,
                "originator_type": "NEW",
                "enforcement_court_id": 101,
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
                    "document_language": "English",
                    "hearing_language": "English",
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
                    "imposing_court_id": 202,
                    "offence_id": 1234,
                    "impositions": [
                      {
                        "result_id": "1",
                        "amount_imposed": 500.00,
                        "amount_paid": 200.00,
                        "major_creditor_id": 999
                      }
                    ]
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
              "account_type": "Fines",
              "account_status": "Submitted",
              "version": 0,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2023-11-01",
                  "reason_text": "Account successfully activated after review."
                },
                {
                  "username": "janedoe456",
                  "status": "Pending",
                  "status_date": "2023-12-05",
                  "reason_text": "Awaiting additional documentation for verification."
                },
                {
                  "username": "mikebrown789",
                  "status": "Suspended",
                  "status_date": "2023-10-15",
                  "reason_text": "Violation of terms of service."
                }
              ]
            }""";
    }

    private static String validReplaceRequestBody(Long version) {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "adultOrYouthOnly",
                "originator_name": "Police Force",
                "originator_id": 12345,
                "originator_type": "TFO",
                "enforcement_court_id": 101,
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
                    "document_language": "English",
                    "hearing_language": "English",
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
                    "imposing_court_id": 202,
                    "offence_id": 1234,
                    "impositions": [
                      {
                        "result_id": "1",
                        "amount_imposed": 500.00,
                        "amount_paid": 200.00,
                        "major_creditor_id": 999
                      }
                    ]
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
              "account_type": "Fines",
              "account_status": "Submitted",
              "version": """ + version
            +
            """
            ,
            "timeline_data": [
              {
                "username": "johndoe123",
                "status": "Active",
                "status_date": "2023-11-01",
                "reason_text": "Account successfully activated after review."
              },
              {
                "username": "janedoe456",
                "status": "Pending",
                "status_date": "2023-12-05",
                "reason_text": "Awaiting additional documentation for verification."
              },
              {
                "username": "mikebrown789",
                "status": "Suspended",
                "status_date": "2023-10-15",
                "reason_text": "Violation of terms of service."
              }
            ]
          }""";
    }






}
