package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.entity.draft.DraftAccountStatus;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;

@Slf4j(topic = "opal.DraftAccountControllerIntegrationTest")
@DisplayName("DraftAccountController Integration Tests")
class DraftAccountControllerGetIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Get Draft Account by ID [@PO-973, @PO-559]")
    void testGetDraftAccountById_success() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/1")
            .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_success: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"0\""))
            .andExpect(jsonPath("$.draft_account_id").value(1))
            .andExpect(jsonPath("$.business_unit_id").value(77))
            .andExpect(jsonPath("$.account_type").value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.submitted_by").value("user_001"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account_status_date").value("2024-12-10T16:27:01.023126Z"))
            .andExpect(jsonPath("$.submitted_by_name").value("John Smith"))
            .andExpect(jsonPath("$.account.originator_type").value("NEW"))
            .andExpect(jsonPath("$.version").doesNotExist())
            .andExpect(jsonPath("$.status_message").doesNotExist())
            .andExpect(jsonPath("$.validated_by_name").doesNotExist());

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNT_RESPONSE);
    }


    @Test
    @DisplayName("Get Draft Account by ID PDPL Logging")
    void testGetDraftAccountById_PdplLogging() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE + "/5")
            .header("authorization", "Bearer some_value")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(2)).personalDataAccessLogAsync(captor.capture());
        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(2, logs.size());

        PersonalDataProcessingLogDetails first = logs.get(0);
        assertEquals("Get Draft Account - Parent or Guardian", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, first.getCategory());
        assertEquals("1", first.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, first.getCreatedBy().getType());
        assertEquals("192.168.1.100", first.getIpAddress());
        assertEquals(1, first.getIndividuals().size());
        assertEquals("5", first.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, first.getIndividuals().get(0).getType());

        PersonalDataProcessingLogDetails second = logs.get(1);
        assertEquals("Get Draft Account - Defendant", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, second.getCategory());
        assertEquals("1", second.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, second.getCreatedBy().getType());
        assertEquals("192.168.1.100", second.getIpAddress());
        assertEquals(1, second.getIndividuals().size());
        assertEquals("5", second.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, second.getIndividuals().get(0).getType());
    }


    @Test
    @DisplayName("Get draft accounts summaries - No query params [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_noParams() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions =  mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_noParams: body:\n" + ToJsonString.toPrettyJson(body));
        JsonNode response = objectMapper.readTree(body);
        int count = response.get("count").asInt();
        int summariesSize = response.withArray("summaries").size();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[2].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[2].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[2].account_status").value("Publishing Failed"));

        assertEquals(count, summariesSize);
        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts - Should return list and call PDPLLoggingService")
    void testGetDraftAccounts_success_and_pdplServiceCalled() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short) 78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS,
                FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));
        when(loggingService.personalDataAccessLogAsync(any())).thenReturn(true);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor = ArgumentCaptor.forClass(
            PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(3)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(3, logs.size());

        Map<String, PersonalDataProcessingLogDetails> byBusiness =
            logs.stream().collect(Collectors.toMap(
                PersonalDataProcessingLogDetails::getBusinessIdentifier,
                c -> c
            ));

        PersonalDataProcessingLogDetails defendant =
            byBusiness.get("Get Draft Account - Defendant");

        assertEquals("1", defendant.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, defendant.getCreatedBy().getType());
        assertEquals("192.168.1.100", defendant.getIpAddress());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, defendant.getCategory());
        assertNull(defendant.getRecipient());

        List<String> defendantIds =
            defendant.getIndividuals().stream()
                .map(ParticipantIdentifier::getIdentifier)
                .toList();

        assertEquals(2, defendantIds.size());
        assertTrue(defendantIds.containsAll(List.of("202", "5")));

        PersonalDataProcessingLogDetails pg =
            byBusiness.get("Get Draft Account - Parent or Guardian");

        assertEquals("1", pg.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, pg.getCreatedBy().getType());
        assertEquals("192.168.1.100", pg.getIpAddress());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, pg.getCategory());

        assertEquals(1, pg.getIndividuals().size());
        assertEquals("5", pg.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, pg.getIndividuals().get(0).getType());

        PersonalDataProcessingLogDetails minor =
            byBusiness.get("Get Draft Account - Minor Creditor");

        assertEquals("1", minor.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, minor.getCreatedBy().getType());
        assertEquals("192.168.1.100", minor.getIpAddress());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, minor.getCategory());

        assertEquals(1, minor.getIndividuals().size());
        assertEquals("202", minor.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, minor.getIndividuals().get(0).getType());
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param business unit [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramBusinessUnit() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", BU_ID.toString())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramBusinessUnit: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Publishing Failed"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Params for status dates from and to")
    void testGetDraftAccountsSummaries_paramStatusDate() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        LocalDate fromDate = LocalDate.of(2025, 02, 03);
        LocalDate toDate = LocalDate.of(2025, 02, 03);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("account_status_date_from", fromDate.toString())
            .param("account_status_date_to", toDate.toString())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramStatusDate: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(7))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(78))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param status [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramStatus() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("status", DraftAccountStatus.PUBLISHING_FAILED.name())
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramStatus: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_003"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Publishing Failed"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param submitted by [@PO-973, @PO-606]")
    void testGetDraftAccountsSummaries_paramSubmittedBy() throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("submitted_by", "user_002")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramSubmittedBy: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[0].account_type")
                .value("Fixed Penalty Registration"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("user_002"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft accounts summaries - Param not submitted by [@PO-973, @PO-832]")
    void testGetDraftAccountsSummaries_paramNotSubmittedBy() throws Exception {


        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("not_submitted_by", "user_003")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_paramNotSubmittedBy: body:\n" + ToJsonString.toPrettyJson(body));
        JsonNode response = objectMapper.readTree(body);
        int count = response.get("count").asInt();
        int summariesSize = response.withArray("summaries").size();

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(5))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(78));

        assertEquals(count, summariesSize);
        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Single business unit permission, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1() throws Exception {

        UserState user = permissionUser(BU_ID, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, filtered to BU 73 [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", "73")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get draft account summaries - Multiple BU permissions, no filter [@PO-973, @PO-829]")
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits3() throws Exception {

        UserState user = permissionUser(new Short[] {BU_ID, (short)77}, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits3: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(3))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[1].draft_account_id").value(2))
            .andExpect(jsonPath("$.summaries[1].business_unit_id").value(77))
            .andExpect(jsonPath("$.summaries[2].draft_account_id").value(3))
            .andExpect(jsonPath("$.summaries[2].business_unit_id").value(73));

        jsonSchemaValidationService.validateOrError(body, GET_DRAFT_ACCOUNTS_RESPONSE);
    }

    @Test
    @DisplayName("Get Draft Account : Deterministic GET results include originator type")
    void testGetDraftAccountById_deterministic() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());
        ResultActions resultActions1 = mockMvc.perform(get(URL_BASE + "/1")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account.originator_type").value("NEW"));
        String body1 = resultActions1.andReturn().getResponse().getContentAsString();
        ResultActions resultActions2 = mockMvc.perform(get(URL_BASE + "/1")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account.originator_type").value("NEW"));
        String body2 = resultActions2.andReturn().getResponse().getContentAsString();
        assertEquals(body1, body2);
    }

    @Test
    @DisplayName("Search draft accounts - POST with draftAccountId - Should return matching draft account"
        + " [@PO-973, @PO-559]")
    void testSearchDraftAccountsPost() throws Exception {

        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"draftAccountId\":\"1\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost: body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draft_account_id").value(1))
            .andExpect(jsonPath("$[0].business_unit_id").value(77));

    }


    @Test
    void testSearchDraftAccountsPost_whenDraftAccountDoesNotExist() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(URL_BASE + "/search")
            .header("authorization", "Bearer some_value")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"draftAccountId\":\"999999\"}"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testSearchDraftAccountsPost_whenDraftAccountDoesNotExist: body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Get draft account by ID - User with wrong permission [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongPermission() throws Exception {

        UserState userState = permissionUser(BU_ID, FinesPermission.COLLECTION_ORDER);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions actions = mockMvc.perform(get(URL_BASE + "/2")
            .header("authorization", "Bearer some_value"));

        String body = actions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_trap403Response_wrongPermission: Response body:\n{}",
            ToJsonString.toPrettyJson(body));

        actions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value(
                "You do not have permission to access this resource"))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/forbidden"));
    }

    @Test
    @DisplayName("Get draft account by ID - user with wrong permission for business unit [@PO-973, @PO-828]")
    void testGetDraftAccountById_trap403Response_wrongBusinessUnit() throws Exception {

        UserState userState = permissionUser((short)005, FinesPermission.DRAFT_ACCOUNT_PERMISSIONS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions resultActions = mockMvc.perform(
            get(URL_BASE + "/2")
                .header("authorization", "Bearer some_value"));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountById_trap403Response_wrongBusinessUnit: Response body:\n"
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
    @DisplayName("Get draft account by ID - user with no permission [@PO-973, @PO-828]")
    void testGetDraftAccountsSummaries_trap403Response_noPermission() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, FinesPermission.COLLECTION_ORDER);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", businessId.toString())
            .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    @DisplayName("Get draft account by ID - Should return 404 Not Found [@PO-973, @PO-690]")
    void testGetDraftAccountById_trap404Response() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        mockMvc.perform(get(URL_BASE + "/99")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    @DisplayName("Get draft account by ID - Should return 406 Not Acceptable [@PO-973, @PO-690]")
    void testGetDraftAccountById_trap406Response() throws Exception {
        mockMvc.perform(get(URL_BASE + "/99")
                .header("Authorization", "Bearer " + "some_value")
                .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("Get draft account summaries - Should return 400 Bad Request [@PO-973, @PO-647]")
    void testGetDraftAccountsSummaries_trap400Response() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(get(URL_BASE)
            .header("authorization", "Bearer some_value")
            .param("business_unit", businessId.toString())
            .param("submitted_by", "Dave")
            .param("not_submitted_by", "Tony")
            .contentType(MediaType.APPLICATION_JSON));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testGetDraftAccountsSummaries_trap400Response: Response body:\n" + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail")
                .value("Invalid arguments were provided in the request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.type")
                .value("https://hmcts.gov.uk/problems/illegal-argument"));
    }

}
