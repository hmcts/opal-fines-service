package uk.gov.hmcts.opal.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import uk.gov.hmcts.opal.dto.DraftAccountResponseDto;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;

@Slf4j(topic = "opal.DraftAccountControllerPutIntegrationTest")
@DisplayName("DraftAccountControllerPutIntegrationTest")
class DraftAccountControllerPutIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Replace draft account - Should return updated draft account [@PO-973, @PO-746]")
    void testReplaceDraftAccount_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
        String requestBody = validReplaceRequestBody(0L);
        log.info(":testReplaceDraftAccount_success: Request Body:\n{}", ToJsonString.toPrettyJson(requestBody));

        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/" + 5)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "3")
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
    @DisplayName("Replace draft account - Should create and call PDPLLoggingService [@PO-2359]")
    void testPutDraftAccount_success_and_pdplServiceCalled() throws Exception {
        String validRequestBody = validReplaceRequestBodyForPdpl(0L);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        String ifMatch = getIfMatchForDraftAccount(5L);
        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/5")
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(5)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(5, logs.size());

        PersonalDataProcessingLogDetails l0 = logs.get(0);
        assertEquals("Get Draft Account - Defendant", l0.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, l0.getCategory());
        assertEquals("1", l0.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l0.getCreatedBy().getType());
        assertNull(l0.getIpAddress());
        assertNull(l0.getRecipient());
        assertEquals(1, l0.getIndividuals().size());
        assertEquals("5", l0.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l0.getIndividuals().get(0).getType());
        assertNotNull(l0.getCreatedAt());

        PersonalDataProcessingLogDetails l1 = logs.get(1);
        assertEquals("Get Draft Account - Minor Creditor", l1.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, l1.getCategory());
        assertEquals("1", l1.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l1.getCreatedBy().getType());
        assertNull(l1.getIpAddress());
        assertNull(l1.getRecipient());
        assertEquals(1, l1.getIndividuals().size());
        assertEquals("5", l1.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l1.getIndividuals().get(0).getType());
        assertNotNull(l1.getCreatedAt());

        PersonalDataProcessingLogDetails l2 = logs.get(2);
        assertEquals("Update Draft Account - Parent or Guardian", l2.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l2.getCategory());
        assertEquals("1", l2.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l2.getCreatedBy().getType());
        assertEquals("192.168.1.100", l2.getIpAddress());
        assertNull(l2.getRecipient());
        assertEquals(1, l2.getIndividuals().size());
        assertEquals("5", l2.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l2.getIndividuals().get(0).getType());
        assertNotNull(l2.getCreatedAt());

        PersonalDataProcessingLogDetails l3 = logs.get(3);
        assertEquals("Update Draft Account - Defendant", l3.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l3.getCategory());
        assertEquals("1", l3.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l3.getCreatedBy().getType());
        assertEquals("192.168.1.100", l3.getIpAddress());
        assertNull(l3.getRecipient());
        assertEquals(1, l3.getIndividuals().size());
        assertEquals("5", l3.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l3.getIndividuals().get(0).getType());
        assertNotNull(l3.getCreatedAt());

        PersonalDataProcessingLogDetails l4 = logs.get(4);
        assertEquals("Update Draft Account - Minor Creditor", l4.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l4.getCategory());
        assertEquals("1", l4.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l4.getCreatedBy().getType());
        assertEquals("192.168.1.100", l4.getIpAddress());
        assertNull(l4.getRecipient());
        assertEquals(1, l4.getIndividuals().size());
        assertEquals("5", l4.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l4.getIndividuals().get(0).getType());
        assertNotNull(l4.getCreatedAt());
    }

    @Test
    @DisplayName("Replace draft account - Defendant only PDPL log [@PO-2359]")
    void testPutDraftAccount_defendantOnly_pdplLogged() throws Exception {
        String validRequestBody = validReplaceRequestBodyDefendantOnly(0L);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        final OffsetDateTime before = OffsetDateTime.now();
        String ifMatch = getIfMatchForDraftAccount(5L);
        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/5")
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));
        final OffsetDateTime after = OffsetDateTime.now();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(1)).personalDataAccessLogAsync(captor.capture());

        PersonalDataProcessingLogDetails pdpl = captor.getValue();
        assertEquals("Update Draft Account - Defendant", pdpl.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, pdpl.getCategory());
        assertNull(pdpl.getRecipient());
        assertEquals(1, pdpl.getIndividuals().size());
        assertEquals("5", pdpl.getIndividuals().getFirst().getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, pdpl.getIndividuals().getFirst().getType());

        assertNotNull(pdpl.getCreatedBy());
        assertEquals("1", pdpl.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, pdpl.getCreatedBy().getType());

        OffsetDateTime createdAt = pdpl.getCreatedAt();
        assertNotNull(createdAt);
        assertTrue(!createdAt.isBefore(before.minusSeconds(5)) && !createdAt.isAfter(after.plusSeconds(5)));
    }

    @Test
    @DisplayName("Replace draft account - Parent/Guardian only PDPL log [@PO-2359]")
    void testPutDraftAccount_parentGuardianOnly_pdplLogged() throws Exception {
        String validRequestBody = validReplaceRequestBodyParentGuardianOnly(0L);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        final OffsetDateTime before = OffsetDateTime.now();
        String ifMatch = getIfMatchForDraftAccount(5L);
        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/5")
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));
        final OffsetDateTime after = OffsetDateTime.now();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(3)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(3, logs.size());

        PersonalDataProcessingLogDetails l0 = logs.get(0);
        assertEquals("Get Draft Account - Defendant", l0.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, l0.getCategory());
        assertEquals("1", l0.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l0.getCreatedBy().getType());
        assertNull(l0.getIpAddress());
        assertNull(l0.getRecipient());
        assertEquals(1, l0.getIndividuals().size());
        assertEquals("5", l0.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l0.getIndividuals().get(0).getType());
        assertNotNull(l0.getCreatedAt());

        PersonalDataProcessingLogDetails l1 = logs.get(1);
        assertEquals("Update Draft Account - Parent or Guardian", l1.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l1.getCategory());
        assertEquals("1", l1.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l1.getCreatedBy().getType());
        assertEquals("192.168.1.100", l1.getIpAddress());
        assertNull(l1.getRecipient());
        assertEquals(1, l1.getIndividuals().size());
        assertEquals("5", l1.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l1.getIndividuals().get(0).getType());
        assertNotNull(l1.getCreatedAt());

        PersonalDataProcessingLogDetails l2 = logs.get(2);
        assertEquals("Update Draft Account - Defendant", l2.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l2.getCategory());
        assertEquals("1", l2.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l2.getCreatedBy().getType());
        assertEquals("192.168.1.100", l2.getIpAddress());
        assertNull(l2.getRecipient());
        assertEquals(1, l2.getIndividuals().size());
        assertEquals("5", l2.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l2.getIndividuals().get(0).getType());
        assertNotNull(l2.getCreatedAt());

    }

    @Test
    @DisplayName("Replace draft account - Minor creditor only PDPL log [@PO-2359]")
    void testPutDraftAccount_minorCreditorOnly_pdplLogged() throws Exception {
        String validRequestBody = validReplaceRequestBodyMinorCreditorOnly(0L);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        final OffsetDateTime before = OffsetDateTime.now();
        String ifMatch = getIfMatchForDraftAccount(5L);
        ResultActions resultActions = mockMvc.perform(put(URL_BASE + "/5")
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validRequestBody));
        final OffsetDateTime after = OffsetDateTime.now();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);
        verify(loggingService, times(3)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(3, logs.size());

        PersonalDataProcessingLogDetails first = logs.get(0);
        assertEquals("Get Draft Account - Defendant", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, first.getCategory());
        assertEquals("1", first.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, first.getCreatedBy().getType());
        assertNull(first.getIpAddress());
        assertNull(first.getRecipient());
        assertEquals(1, first.getIndividuals().size());
        assertEquals("5", first.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, first.getIndividuals().get(0).getType());
        assertNotNull(first.getCreatedAt());

        PersonalDataProcessingLogDetails second = logs.get(1);
        assertEquals("Update Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, second.getCategory());
        assertEquals("1", second.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, second.getCreatedBy().getType());
        assertEquals("192.168.1.100", second.getIpAddress());
        assertNull(second.getRecipient());
        assertEquals(1, second.getIndividuals().size());
        assertEquals("5", second.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, second.getIndividuals().get(0).getType());
        assertNotNull(second.getCreatedAt());

        PersonalDataProcessingLogDetails third = logs.get(2);
        assertEquals("Update Draft Account - Minor Creditor", third.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, third.getCategory());
        assertEquals("1", third.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, third.getCreatedBy().getType());
        assertEquals("192.168.1.100", third.getIpAddress());
        assertNull(third.getRecipient());
        assertEquals(1, third.getIndividuals().size());
        assertEquals("5", third.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, third.getIndividuals().get(0).getType());
        assertNotNull(third.getCreatedAt());

    }

    @Test
    @DisplayName("Put Draft Account : Deterministic and includes originator type")
    void testPutDraft_deterministic() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());
        String requestBody = validReplaceRequestBody(3L);

        String first = mockMvc.perform(put(URL_BASE + "/" + 5)
                .header("authorization", "Bearer some_value")
                .header("If-Match", getIfMatchForDraftAccount(5L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        String second = mockMvc.perform(put(URL_BASE + "/" + 5)
                .header("authorization", "Bearer some_value")
                .header("If-Match", getIfMatchForDraftAccount(5L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        DraftAccountResponseDto r1 = objectMapper.readValue(first, DraftAccountResponseDto.class);
        DraftAccountResponseDto r2 = objectMapper.readValue(second, DraftAccountResponseDto.class);

        assertThat(r1)
            .usingRecursiveComparison()
            .ignoringFields("accountStatusDate")
            .isEqualTo(r2);
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

    private static String validReplaceRequestBodyForPdpl(Long version) {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "pgToPay",
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
                  "surname": "LNAME",
                  "forenames": "FNAME",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "post_code": "AB1 2CD",
                  "parent_guardian": {
                    "company_flag": false,
                    "surname": "Guardian",
                    "forenames": "Pat",
                    "dob": "1970-01-01",
                    "address_line_1": "456 Justice Road",
                    "post_code": "AB1 2CD"
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
                        "major_creditor_id": null,
                        "minor_creditor": {
                          "company_flag": false,
                          "surname": "Minor",
                          "forenames": "Alice",
                          "payout_hold": false,
                          "pay_by_bacs": true
                        }
                      }
                    ]
                  }
                ],
                "payment_terms": {
                  "payment_terms_type_code": "P"
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
              "version": """ + version + """
              ,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ]
            }
            """;
    }

    private static String validReplaceRequestBodyDefendantOnly(Long version) {
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
                "originator_type": "NEW",
                "enforcement_court_id": 101,
                "collection_order_made": true,
                "collection_order_made_today": false,
                "payment_card_request": true,
                "account_sentence_date": "2023-12-01",
                "defendant": {
                  "company_flag": false,
                  "surname": "LNAME",
                  "forenames": "FNAME",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "post_code": "AB1 2CD"
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
                  "payment_terms_type_code": "P"
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
              "version": """ + version + """
              ,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ]
            }
            """;
    }

    private static String validReplaceRequestBodyParentGuardianOnly(Long version) {
        return """
            {
              "business_unit_id": 78,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
                "account_type": "Fine",
                "defendant_type": "pgToPay",
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
                  "surname": "LNAME",
                  "forenames": "FNAME",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "post_code": "AB1 2CD",
                  "parent_guardian": {
                    "company_flag": false,
                    "surname": "Guardian",
                    "forenames": "Pat",
                    "dob": "1970-01-01",
                    "address_line_1": "456 Justice Road",
                    "post_code": "AB1 2CD"
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
                  "payment_terms_type_code": "P"
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
              "version": """ + version + """
              ,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ]
            }
            """;
    }

    private static String validReplaceRequestBodyMinorCreditorOnly(Long version) {
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
                "originator_type": "NEW",
                "enforcement_court_id": 101,
                "collection_order_made": true,
                "collection_order_made_today": false,
                "payment_card_request": true,
                "account_sentence_date": "2023-12-01",
                "defendant": {
                  "company_flag": false,
                  "surname": "LNAME",
                  "forenames": "FNAME",
                  "dob": "1985-04-15",
                  "address_line_1": "123 Elm Street",
                  "post_code": "AB1 2CD"
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
                        "major_creditor_id": null,
                        "minor_creditor": {
                          "company_flag": false,
                          "surname": "Minor",
                          "forenames": "Alice",
                          "payout_hold": false,
                          "pay_by_bacs": true
                        }
                      }
                    ]
                  }
                ],
                "payment_terms": {
                  "payment_terms_type_code": "P"
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
              "version": """ + version + """
              ,
              "timeline_data": [
                {
                  "username": "johndoe123",
                  "status": "Active",
                  "status_date": "2025-10-15",
                  "reason_text": "Account created for testing"
                }
              ]
            }
            """;
    }

}
