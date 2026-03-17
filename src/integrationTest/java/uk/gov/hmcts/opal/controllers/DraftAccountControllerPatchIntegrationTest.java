package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noFinesPermissionUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.Permission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingCategory;
import uk.gov.hmcts.opal.logging.integration.dto.PersonalDataProcessingLogDetails;

@Slf4j(topic = "opal.DraftAccountControllerPatchIntegrationTest")
@DisplayName("DraftAccountControllerPatchIntegrationTest")
class DraftAccountControllerPatchIntegrationTest extends CommonDraftAccountControllerIntegrationTest {

    @Test
    @DisplayName("Update draft account - Should return updated account details [@PO-973, @PO-745]")
    void testUpdateDraftAccount_success() throws Exception {
        Long draftAccountId = 8L; // not touched by any other PATCH/PUT test
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.validated_by").value("USER01"))
            .andExpect(jsonPath("$.validated_by_name").value("normal@users.com"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);
    }

    @Test
    @DisplayName("Update draft account - If-Match Conflict [@PO-2117]")
    void testUpdateDraftAccount_conflict() throws Exception {
        Long draftAccountId = 6L;
        when(userStateService.checkForAuthorisedUser(any()))
            .thenReturn(permissionUser((short) 65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS));

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "\"9999999\"")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount_success: Response body:\n{}", ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.resourceType").value("uk.gov.hmcts.opal.entity.draft.DraftAccountEntity"))
            .andExpect(jsonPath("$.resourceId").value("6"))
            .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @DisplayName("Patch draft account - user with CHECK_VALIDATE permission should succeed [@PO-1820]")
    void testPatchDraftAccount_withCheckValidatePermission_shouldSucceed() throws Exception {
        Long draftAccountId = 7L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)78, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78","Rejected","A")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCheckValidatePermission_shouldSucceed: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"1\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Rejected"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));
    }

    @Test
    @DisplayName("Patch draft account - user with Publish Pending permission should succeed [@PO-991]")
    void testPatchDraftAccount_withPublishPending_shouldSucceed() throws Exception {
        Long draftAccountId = 9L; // not touched by any other PATCH/PUT test
        UserState user = permissionUser((short)65, FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending","D")));

        String response = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withPublishPending_shouldSucceed: PATCH Response body:\n{}",
            ToJsonString.toPrettyJson(response));

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        verify(securityEventLoggingService).logEvent(
            eq("Business Function - Approval of Draft Account"),
            eq("Success"),
            eq((short) 65),
            eq("Approval"),
            any(),
            any()
        );
    }

    @Test
    @DisplayName("Patch draft account - submitter cannot validate their own submission")
    void testPatchDraftAccount_submitterCannotValidate_returns403() throws Exception {
        Long draftAccountId = 7L; // submitted_by = user_003 in seed data

        BusinessUnitUser buUser = BusinessUnitUser.builder()
            .businessUnitUserId("user_003")
            .businessUnitId((short)78)
            .permissions(Set.of(Permission.builder()
                .permissionId(FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS.getId())
                .permissionName(FinesPermission.CHECK_VALIDATE_DRAFT_ACCOUNTS.getDescription())
                .build()))
            .build();
        UserState userState = UserState.builder()
            .userId(1L)
            .userName("normal@users.com")
            .businessUnitUser(Set.of(buUser))
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78", "Publishing Pending","A")));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.type").value("https://hmcts.gov.uk/problems/submitter-cannot-validate"))
            .andExpect(jsonPath("$.title").value("Submitter cannot validate"))
            .andExpect(jsonPath("$.detail")
                .value("A single user cannot submit and validate the same Draft Account"));

        verify(securityEventLoggingService).logEvent(
            eq("Business Function - Approval of Draft Account"),
            eq("Failure"),
            eq((short) 78),
            eq("Approval"),
            any(),
            any()
        );
    }


    @Test
    @DisplayName("Patch draft account - user with CREATE_MANAGE permission should be forbidden [@PO-1820]")
    void testPatchDraftAccount_withCreateManagePermission_shouldFail403() throws Exception {
        Long draftAccountId = 6L;
        UserState user = permissionUser((short)78, FinesPermission.CREATE_MANAGE_DRAFT_ACCOUNTS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("78", "Publishing Pending","PO1820")));

        String body = resultActions.andReturn().getResponse().getContentAsString();
        log.info(":testPatchDraftAccount_withCreateManagePermission_shouldFail403: Response body:\n"
            + ToJsonString.toPrettyJson(body));

        resultActions.andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Update draft account - user with no permission [@PO-973, @PO-831]")
    void testUpdateDraftAccount_trap403Response_noPermission() throws Exception {
        Long draftAccountId = 241L;
        String requestBody = "            {\n"
            + "                \"account_status\": \"PENDING\",\n"
            + "                \"validated_by\": \"BUUID1\",\n"
            + "                \"business_unit_id\": 5,\n"
            + "                \"timeline_data\": "
            + validTimelineDataJson()
            + "\n"
            + "            }";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noFinesPermissionUser());

        mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                .header("authorization", "Bearer some_value")
                .header("If-Match", "0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Re-submit - Defendant only -> Re-submit Draft Account - Defendant PDPL")
    void testResubmitDraftAccount_pdpl_defendantOnly() throws Exception {
        final long draftIdAccount = 105L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "X")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // For defendant-only we expect a single PDPL call (Re-submit Draft Account - Defendant)
        verify(loggingService, timeout(2000).times(1)).personalDataAccessLogAsync(captor.capture());

        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(1, logs.size());

        PersonalDataProcessingLogDetails log = logs.get(0);

        assertEquals("Re-submit Draft Account - Defendant", log.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, log.getCategory());

        assertNotNull(log.getCreatedBy());
        assertEquals("0", log.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, log.getCreatedBy().getType());

        assertEquals("192.168.1.100", log.getIpAddress());

        assertNull(log.getRecipient());

        assertEquals(1, log.getIndividuals().size());
        assertEquals("105", log.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, log.getIndividuals().get(0).getType());

        assertNotNull(log.getCreatedAt());
    }

    @Test
    @DisplayName("Re-submit - pgToPay -> Parent or Guardian then Defendant PDPLs (order)")
    void testResubmitDraftAccount_pdpl_parentOrGuardianThenDefendant() throws Exception {
        final long draftIdAccount = 104L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "Y")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        // Expect two calls: Parent or Guardian, then Defendant
        verify(loggingService, timeout(2000).times(2)).personalDataAccessLogAsync(captor.capture());
        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(2, logs.size());

        PersonalDataProcessingLogDetails l0 = logs.get(0);
        assertEquals("Re-submit Draft Account - Parent or Guardian", l0.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l0.getCategory());
        assertEquals("0", l0.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l0.getCreatedBy().getType());
        assertEquals("192.168.1.100", l0.getIpAddress());
        assertNull(l0.getRecipient());
        assertEquals(1, l0.getIndividuals().size());
        assertEquals("104", l0.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l0.getIndividuals().get(0).getType());
        assertNotNull(l0.getCreatedAt());

        PersonalDataProcessingLogDetails l1 = logs.get(1);
        assertEquals("Re-submit Draft Account - Defendant", l1.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, l1.getCategory());
        assertEquals("0", l1.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, l1.getCreatedBy().getType());
        assertEquals("192.168.1.100", l1.getIpAddress());
        assertNull(l1.getRecipient());
        assertEquals(1, l1.getIndividuals().size());
        assertEquals("104", l1.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, l1.getIndividuals().get(0).getType());
        assertNotNull(l1.getCreatedAt());
    }

    @Test
    @DisplayName("Re-submit - adultOrYouthOnly WITH minor -> Defendant + Minor Creditor PDPLs (order)")
    void testResubmitDraftAccount_pdpl_defendantAndMinor() throws Exception {
        final long draftIdAccount = 8L; // previously used in your suite; confirm or replace if needed

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        String ifMatch = getIfMatchForDraftAccount(draftIdAccount);
        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftIdAccount)
            .header("authorization", "Bearer some_value")
            .header("If-Match", ifMatch)
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "A")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftIdAccount))
            .andExpect(jsonPath("$.business_unit_id").value(65));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);
        ArgumentCaptor<PersonalDataProcessingLogDetails> captor =
            ArgumentCaptor.forClass(PersonalDataProcessingLogDetails.class);

        verify(loggingService, times(4)).personalDataAccessLogAsync(captor.capture());
        List<PersonalDataProcessingLogDetails> logs = captor.getAllValues();

        assertEquals(4, logs.size());

        PersonalDataProcessingLogDetails first = logs.get(0);
        assertEquals("Get Draft Account - Defendant", first.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, first.getCategory());
        assertEquals("0", first.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, first.getCreatedBy().getType());
        assertNull(first.getIpAddress());
        assertEquals(1, first.getIndividuals().size());
        assertEquals("8", first.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, first.getIndividuals().get(0).getType());

        PersonalDataProcessingLogDetails second = logs.get(1);
        assertEquals("Get Draft Account - Minor Creditor", second.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.CONSULTATION, second.getCategory());
        assertEquals("0", second.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, second.getCreatedBy().getType());
        assertNull(second.getIpAddress());
        assertEquals(1, second.getIndividuals().size());
        assertEquals("8", second.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, second.getIndividuals().get(0).getType());

        PersonalDataProcessingLogDetails third = logs.get(2);
        assertEquals("Re-submit Draft Account - Defendant", third.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, third.getCategory());
        assertEquals("0", third.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, third.getCreatedBy().getType());
        assertEquals("192.168.1.100", third.getIpAddress());
        assertEquals(1, third.getIndividuals().size());
        assertEquals("8", third.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, third.getIndividuals().get(0).getType());

        PersonalDataProcessingLogDetails fourth = logs.get(3);
        assertEquals("Re-submit Draft Account - Minor Creditor", fourth.getBusinessIdentifier());
        assertEquals(PersonalDataProcessingCategory.COLLECTION, fourth.getCategory());
        assertEquals("0", fourth.getCreatedBy().getIdentifier());
        assertEquals(PdplIdentifierType.OPAL_USER_ID, fourth.getCreatedBy().getType());
        assertEquals("192.168.1.100", fourth.getIpAddress());
        assertEquals(1, fourth.getIndividuals().size());
        assertEquals("8", fourth.getIndividuals().get(0).getIdentifier());
        assertEquals(PdplIdentifierType.DRAFT_ACCOUNT, fourth.getIndividuals().get(0).getType());

    }

    @Test
    @DisplayName("Update draft account (id=103) - company -> no PDPL logging occurs")
    void testUpdateDraftAccount_pdpl_id103_company_noPdpl() throws Exception {
        Long draftAccountId = 103L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allFinesPermissionUser());

        ResultActions resultActions = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
            .header("authorization", "Bearer some_value")
            .header("If-Match", "0")
            .header("X-User-IP", "192.168.1.100")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validUpdateRequestBody("65", "Publishing Pending", "B")));

        String response = resultActions.andReturn().getResponse().getContentAsString();

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string("ETag", "\"2\""))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(65))
            .andExpect(jsonPath("$.account_status").value("Published"))
            .andExpect(jsonPath("$.timeline_data[0].username").value("johndoe456"));

        jsonSchemaValidationService.validateOrError(response, GET_DRAFT_ACCOUNT_RESPONSE);

        verify(loggingService, times(0)).personalDataAccessLogAsync(any());

    }

}
