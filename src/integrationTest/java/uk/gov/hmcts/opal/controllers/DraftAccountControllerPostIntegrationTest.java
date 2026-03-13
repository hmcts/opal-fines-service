package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;

@Slf4j(topic = "opal.DraftAccountControllerPostIntegrationTest")
@DisplayName("DraftAccountControllerPostIntegrationTest")
class DraftAccountControllerPostIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    private String validRawJsonCreateRequestBody() {
        AddDraftAccountRequestDto dto = AddDraftAccountRequestDto.builder()
            .businessUnitId((short) 78)
            .submittedBy("BUUID1")
            .submittedByName("John")
            .account(validAccountJsonString())
            .accountType("Fines")
            .timelineData(validTimelineDataString())
            .build();

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize AddDraftAccountRequestDto", e);
        }
    }

    private static String validAccountJsonString() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "originator_type": "NEW",
              "enforcement_court_id": 101,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": false,
                "surname": "LNAME",
                "address_line_1": "123 Main Street"
              },
              "offences": [],
              "payment_terms": {
                "payment_terms_type_code": "P",
                "effective_date": "2023-11-01",
                "instalment_period": "M",
                "lump_sum_amount": 1000.00,
                "instalment_amount": 200.00,
                "default_days_in_jail": 5
              }
            }
            """;
    }

    private static String validTimelineDataString() {
        return """
            [
              {
                "username": "johndoe123",
                "status": "Active",
                "status_date": "2023-11-01",
                "reason_text": "Valid reason"
              }
            ]
            """;
    }

    @Test
    @DisplayName("Create draft account - POST with valid request - Should return newly created account "
        + "[@PO-973, @PO-591]")
    void testPostDraftAccount_permission() throws Exception {

        String validRequestBody = validRawJsonCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.submitted_by").value("USER01"))
            .andExpect(jsonPath("$.submitted_by_name").value("normal@users.com"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account.defendant.surname")
                .value("LNAME"))
            .andExpect(jsonPath("$.account.originator_type").value("NEW"))
        ;
    }

    @Test
    @DisplayName("Should ignore blank submitted_by_name")
    void shouldIgnoreBlankSubmittedByName() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by_name\": \"John\"", "\"submitted_by_name\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should ignore blank submitted_by")
    void shouldIgnoreBlankSubmittedBy() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"submitted_by\": \"BUUID1\"", "\"submitted_by\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 when account_type is blank")
    void shouldReturn400WhenAccountTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"account_type\": \"Fines\"", "\"account_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type is missing")
    void shouldReturn400WhenOriginatorTypeIsMissing() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\",", "");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type is blank")
    void shouldReturn400WhenOriginatorTypeIsBlank() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when originator_type has invalid value")
    void shouldReturn400WhenOriginatorTypeIsInvalid() throws Exception {
        String request = validCreateRequestBody()
            .replace("\"originator_type\": \"NEW\"", "\"originator_type\": \"ABC\"");

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(post(URL_BASE)
                .header("Authorization", "Bearer some_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create draft account - Should return 400 Bad Request [@PO-973, @PO-691]")
    void testPostDraftAccount_trap400Response() throws Exception {

        String expectedErrorMessageStart =
            "JSON Schema Validation Error: Validating against JSON schema 'addDraftAccountRequest.json',"
                + " found 15 validation errors:";

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidCreateRequestBody()));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap400Response: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("The request does not conform to the required JSON schema"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/json-schema-validation"));
    }

    @Test
    @DisplayName("Create draft account - user with no permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_noPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap403Response_noPermission: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Create draft account - user with wrong permission [@PO-973, @PO-827]")
    void testPostDraftAccount_trap403Response_wrongPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)5, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS, FinesPermission.ACCOUNT_ENQUIRY));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap403Response_wrongPermission: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Create draft account - user with wrong permission (correct permission, wrong business unit)")
    void testPostDraftAccount_trap403Response_wrongBusinessUnitPermission() throws Exception {

        String validRequestBody = validCreateRequestBody();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)5, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPostDraftAccount_trap403Response_wrongPermission: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Create draft account - Should create and call PDPLLoggingService")
    void testPostDraftAccount_success_and_pdplServiceCalled() throws Exception {

        // arrange: request body from your helper
        String validRequestBody = validPostRequestBody(); // reuse your helper

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        // act: perform POST
        ResultActions resultActions = mockMvc.perform(post(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        // assert response (controller returned 201 in your run)
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        // verify PDPLLoggingService was called with a DraftAccountEntity
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails pdpl = captor.getValue();
        assertNotNull(pdpl);

        assertNotNull(pdpl.getCreatedBy());
        assertEquals("0", pdpl.getCreatedBy().getIdentifier()); // adapt to your ParticipantIdentifier API

        assertEquals("Submit Draft Account - Minor Creditor", pdpl.getBusinessIdentifier());

        OffsetDateTime createdAt = pdpl.getCreatedAt();
        assertNotNull(createdAt);

        assertEquals(PersonalDataProcessingCategory.COLLECTION, pdpl.getCategory()); // adapt to expected enum

        assertNull(pdpl.getRecipient());

        List<ParticipantIdentifier> individuals = pdpl.getIndividuals();
        assertNotNull(individuals);
        assertEquals(1, individuals.size());
        assertEquals("202", individuals.getFirst().getIdentifier());
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

    private static String invalidCreateRequestBody() {
        return """
            {
             "invalid_field": "This field shouldn't be here",
             "account": {
                "account_create_request": {
                "defendant": {
                    "company_name": "Company ABC",
                    "surname": "LNAME",
                    "fornames": "FNAME",
                    "dob": "2000-01-01"
                },
             "account": {
                "account_type": "Invalid"
             }
            }
            },
                "business_unit_id": 1
            }""";
    }

    private String validPostRequestBody() {
        return """
            {
              "draft_account_id": 5,
              "created_at": "2025-11-01T10:30:00+00:00",
              "business_unit_id": 78,
              "validated_by": null,
              "account": {
                "account_type": "Fines",
                "defendant_type": "adultOrYouthOnly",
                "originator_name": "LJS",
                "originator_id": 123,
                "originator_type": "NEW",
                "prosecutor_case_reference": null,
                "enforcement_court_id": 456,
                "collection_order_made": null,
                "collection_order_made_today": null,
                "collection_order_date": null,
                "suspended_committal_date": null,
                "payment_card_request": false,
                "account_sentence_date": "2025-10-01",
                "defendant": {
                  "company_flag": false,
                  "title": null,
                  "surname": "Smith",
                  "company_name": null,
                  "forenames": "John",
                  "dob": "1985-07-20",
                  "address_line_1": "1 Justice Road",
                  "address_line_2": null,
                  "address_line_3": null,
                  "address_line_4": null,
                  "address_line_5": null,
                  "post_code": "AB1 2CD",
                  "telephone_number_home": null,
                  "telephone_number_business": null,
                  "telephone_number_mobile": "07123456789",
                  "email_address_1": "john.smith@example.com",
                  "email_address_2": null,
                  "national_insurance_number": "QQ123456C",
                  "driving_licence_number": null,
                  "pnc_id": null,
                  "nationality_1": "British",
                  "nationality_2": null,
                  "ethnicity_self_defined": null,
                  "ethnicity_observed": null,
                  "cro_number": null,
                  "occupation": "Engineer",
                  "gender": "M",
                  "custody_status": null,
                  "prison_number": null,
                  "interpreter_lang": null,
                  "debtor_detail": null,
                  "parent_guardian": null
                },
                "offences": [
                  {
                    "date_of_sentence": "2025-10-01",
                    "imposing_court_id": 789,
                    "offence_id": 10,
                    "impositions": [
                      {
                        "result_id": "FINE",
                        "amount_imposed": 100.00,
                        "amount_paid": 0.00,
                        "major_creditor_id": null,
                        "minor_creditor": {
                          "company_flag": false,
                          "title": null,
                          "company_name": null,
                          "surname": "Minor",
                          "forenames": "Alice",
                          "dob": "2010-05-05",
                          "address_line_1": "5 Minor St",
                          "address_line_2": null,
                          "address_line_3": null,
                          "address_line_4": null,
                          "address_line_5": null,
                          "post_code": "MN1 2OP",
                          "telephone": null,
                          "email_address": null,
                          "payout_hold": false,
                          "pay_by_bacs": false,
                          "bank_account_type": null,
                          "bank_sort_code": null,
                          "bank_account_number": null,
                          "bank_account_name": null,
                          "bank_account_ref": null
                        }
                      }
                    ]
                  }
                ],
                "fp_ticket_detail": null,
                "payment_terms": {
                  "payment_terms_type_code": "B",
                  "effective_date": null,
                  "instalment_period": null,
                  "lump_sum_amount": null,
                  "instalment_amount": null,
                  "default_days_in_jail": null,
                  "enforcements": null
                },
                "account_notes": null
              },
              "account_snapshot": null,
              "account_type": "Fines",
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ],
              "submitted_by": "BUUID1",
              "submitted_by_name": "Business User 1"
            }

            """;
    }

}
