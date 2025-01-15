package uk.gov.hmcts.opal.controllers;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.QueryTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.config.WebConfig;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler;
import uk.gov.hmcts.opal.dto.AddDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.UpdateDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountStatus;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.noPermissionsUser;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.permissionUser;
import static uk.gov.hmcts.opal.entity.DraftAccountStatus.ERROR_IN_PUBLISHING;
import static uk.gov.hmcts.opal.entity.DraftAccountStatus.SUBMITTED;


@WebMvcTest
@ContextConfiguration(classes = {DraftAccountController.class, GlobalExceptionHandler.class, WebConfig.class})
@ActiveProfiles({"integration"})
@Slf4j(topic = "DraftAccountControllerIntegrationTest")
class DraftAccountControllerIntegrationTest {
    private static final String URL_BASE = "/draft-accounts";
    private static final String GET_DRAFT_ACCOUNT_RESPONSE = "getDraftAccountResponse.json";
    private static final String GET_DRAFT_ACCOUNTS_RESPONSE = "getDraftAccountsResponse.json";

    private static final Short BU_ID = (short)007;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    @Qualifier("draftAccountService")
    DraftAccountService draftAccountService;

    @MockBean
    UserStateService userStateService;

    @MockBean
    AccessTokenService tokenService;

    @SpyBean
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    void testGetDraftAccountById_success() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity(BU_ID);

        when(draftAccountService.getDraftAccount(1L)).thenReturn(draftAccountEntity);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(get(URL_BASE + "/1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(1))
            .andExpect(jsonPath("$.business_unit_id").value(7))
            .andExpect(jsonPath("$.account_type").value("DRAFT"))
            .andExpect(jsonPath("$.submitted_by").value("Tony"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.status_message").value("Status is OK"))
            .andExpect(jsonPath("$.account_status_date").value("2024-11-11T11:11:00Z"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));
    }

    @Test
    void testGetDraftAccountById_trap403Response_wrongPermission() throws Exception {
        DraftAccountEntity entity = createDraftAccountEntity(BU_ID);
        when(draftAccountService.getDraftAccount(2L)).thenReturn(entity);

        UserState userState = permissionUser(BU_ID, Permissions.COLLECTION_ORDER);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        mockMvc.perform(
                get(URL_BASE + "/2")
                    .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS] "
                    + "permission(s) are not enabled for the user."));
    }

    @Test
    void testGetDraftAccountById_trap403Response_wrongBusinessUnit() throws Exception {
        DraftAccountEntity entity = createDraftAccountEntity(BU_ID);
        when(draftAccountService.getDraftAccount(2L)).thenReturn(entity);

        UserState userState = permissionUser((short)005, Permissions.DRAFT_ACCOUNT_PERMISSIONS);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(userState);

        mockMvc.perform(
                get(URL_BASE + "/2")
                    .header("authorization", "Bearer some_value"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS, CHECK_VALIDATE_DRAFT_ACCOUNTS] "
                    + "permission(s) are not enabled in business unit: 7"));
    }

    @Test
    void testGetDraftAccountById_trap404Response() throws Exception {
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        when(entity.getBusinessUnit()).thenThrow(new EntityNotFoundException());

        when(draftAccountService.getDraftAccount(2L)).thenReturn(entity);
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(
            get(URL_BASE + "/2")
                .header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound())
        ;
    }

    @Test
    void testGetDraftAccountById_trap406Response() throws Exception {
        when(draftAccountService.getDraftAccount(1L)).thenReturn(createDraftAccountEntity(BU_ID));
        shouldReturn406WhenResponseContentTypeNotSupported(get(URL_BASE + "/1"));
    }

    @Test
    void testGetDraftAccountById_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(get(URL_BASE + "/1"),
                                   when(draftAccountService.getDraftAccount(1L)));
    }

    @Test
    void testGetDraftAccountById_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(get(URL_BASE + "/1"),
                                                          when(draftAccountService.getDraftAccount(1L)));
    }

    @Test
    void testGetDraftAccountsSummaries_noParams() throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(createDraftAccountEntityList("Tony"));

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)), 1);

        log.info(":testGetDraftAccountsSummaries_noParams: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_paramBusinessUnit() throws Exception {
        List<DraftAccountEntity> draftAccountEntities = createDraftAccountEntityList("Tony");

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(draftAccountEntities);

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", BU_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)), 1);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_paramStatus() throws Exception {
        List<DraftAccountEntity> draftAccountEntities = createDraftAccountEntityList("Tony");

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(draftAccountEntities);

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("status", ERROR_IN_PUBLISHING.getLabel())
                            .contentType(MediaType.APPLICATION_JSON)), 1);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_paramSubmittedBy() throws Exception {
        List<DraftAccountEntity> draftAccountEntities = createDraftAccountEntityList("Tony");

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(draftAccountEntities);

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("submitted_by", "Tony")
                            .contentType(MediaType.APPLICATION_JSON)), "Tony", 1);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_paramNotSubmittedBy() throws Exception {
        List<DraftAccountEntity> draftAccountEntities = createDraftAccountEntityList("Dave");

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(draftAccountEntities);

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("not_submitted_by", "Tony")
                            .contentType(MediaType.APPLICATION_JSON)), "Dave", 1);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits1() throws Exception {
        DraftAccountEntity draftAccountEntity7 = createDraftAccountEntity(BU_ID);
        DraftAccountEntity draftAccountEntity6 = createDraftAccountEntity((short)6);

        UserState user = permissionUser(BU_ID, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(List.of(draftAccountEntity7, draftAccountEntity6));

        String body = checkStandardSummaryExpectations(
            mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)), 1);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_permissionRestrictedBusinessUnits2() throws Exception {
        DraftAccountEntity draftAccountEntity7 = createDraftAccountEntity(BU_ID);
        DraftAccountEntity draftAccountEntity6 = createDraftAccountEntity((short)6);

        UserState user = permissionUser(new Short[] {BU_ID, (short)6}, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(List.of(draftAccountEntity7, draftAccountEntity6));

        String body = checkStandardSummaryExpectations(
            mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", "7")
                            // .param("business_unit", BU_ID.toString())
                            .contentType(MediaType.APPLICATION_JSON)), 2);

        log.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_trap400Response() throws Exception {
        List<DraftAccountEntity> draftAccountEntities = createDraftAccountEntityList("Dave");
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(draftAccountEntities);

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .param("submitted_by", "Dave")
                            .param("not_submitted_by", "Tony")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                           .value("Cannot include both 'submitted_by' and 'not_submitted_by' parameters."));
    }

    @Test
    void testGetDraftAccountsSummaries_trap403Response_noPermission() throws Exception {
        final Short businessId = (short)1;

        UserState user = permissionUser(businessId, Permissions.COLLECTION_ORDER);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any(), any()))
            .thenReturn(createDraftAccountEntityList("Tony"));

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetDraftAccountsSummaries_trap406Response() throws Exception {
        shouldReturn406WhenResponseContentTypeNotSupported(get(URL_BASE));
    }

    @Test
    void testGetDraftAccountsSummaries_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(get(URL_BASE), when(draftAccountService.getDraftAccounts(any(), any(), any(),
                                                                                            any())));
    }

    @Test
    void testGetDraftAccountsSummaries_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(get(URL_BASE),
                                             when(draftAccountService.getDraftAccounts(any(), any(), any(),
                                                                                       any())));
    }

    private String checkStandardSummaryExpectations(ResultActions actions, int expectedCount) throws Exception {
        return checkStandardSummaryExpectations(actions, "Tony", expectedCount);
    }

    private String checkStandardSummaryExpectations(ResultActions actions,
                                                    String submittedBy, int expectedCount) throws Exception {
        return actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(expectedCount))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(7))
            .andExpect(jsonPath("$.summaries[0].account_type").value("DRAFT"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value(submittedBy))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"))
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testSearchDraftAccountsPost() throws Exception {
        when(draftAccountService.searchDraftAccounts(any(DraftAccountSearchDto.class)))
            .thenReturn(createDraftAccountEntityList("Tony"));

        mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"value\"}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].draft_account_id").value(1))
            .andExpect(jsonPath("$[0].business_unit_id").value(7))
            .andExpect(jsonPath("$[0].account_type").value("DRAFT"))
            .andExpect(jsonPath("$[0].submitted_by").value("Tony"))
            .andExpect(jsonPath("$[0].account_status").value("Submitted"));
    }

    @Test
    void testSearchDraftAccountsPost_whenDraftAccountDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }


    private DraftAccountEntity createDraftAccountEntity(short businessUnitId) {
        return createDraftAccountEntity("Tony", businessUnitId);
    }

    private DraftAccountEntity createDraftAccountEntity(String submittedBy) {
        return createDraftAccountEntity(submittedBy, BU_ID);
    }

    private DraftAccountEntity createDraftAccountEntity(String submittedBy, short businessUnit) {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId(businessUnit).build())
            .createdDate(LocalDate.of(2023, 1, 2).atStartOfDay())
            .submittedBy(submittedBy)
            .accountType("DRAFT")
            .accountStatus(SUBMITTED)
            .statusMessage("Status is OK")
            .accountStatusDate(LocalDateTime.of(2024, 11, 11, 11, 11))
            .account(validAccountJson())
            .accountSnapshot("{ \"data\": \"something snappy\"}")
            .timelineData(validTimelineDataJson())
            .build();
    }

    private List<DraftAccountEntity> createDraftAccountEntityList(String... submittedBys) {
        return Arrays.stream(submittedBys).map(this::createDraftAccountEntity).toList();
    }

    private List<DraftAccountEntity> createDraftAccountEntityList(Short... businessUnits) {
        return Arrays.stream(businessUnits).map(this::createDraftAccountEntity).toList();
    }

    @Test
    void testDeleteDraftAccountById_success() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity(BU_ID);

        when(draftAccountService.getDraftAccount(1L)).thenReturn(draftAccountEntity);

        MvcResult result = mockMvc.perform(delete(URL_BASE + "/1")
                                               .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Draft Account '1' deleted"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    void testReplaceDraftAccount_success() throws Exception {
        Long draftAccountId = 241L;

        LocalDateTime testDateTime = LocalDateTime.of(2024, 9, 26, 15, 0, 0);

        DraftAccountEntity updatedEntity = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 5)
                              .businessUnitName("Cambridgeshire").build())
            .createdDate(testDateTime)
            .submittedBy("BUUID1")
            .submittedByName("Tony Typist")
            .account(validAccountJson())
            .accountSnapshot("{\"defendant_name\":\"Company ABC\",\"created_date\":\"2024-09-26T15:00:00Z\","
                                 + "\"account_type\":\"Fine\",\"submitted_by_name\":\"Tony Typist\","
                                 + "\"business_unit_name\":\"Cambridgeshire\"}")
            .accountType("Fines")
            .accountStatus(DraftAccountStatus.RESUBMITTED)
            .timelineData(validTimelineDataJson())
            .build();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        when(draftAccountService.replaceDraftAccount(eq(draftAccountId), any(ReplaceDraftAccountRequestDto.class)))
            .thenReturn(updatedEntity);

        MvcResult result = mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validCreateRequestBody()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(5))
            .andExpect(jsonPath("$.created_at").value("2024-09-26T15:00:00Z"))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account.defendant.company_name")
                           .value("company"))
            .andExpect(jsonPath("$.account_snapshot.defendant_name").value("Company ABC"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Resubmitted"))
            .andExpect(jsonPath("$.timeline_data").isArray())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testReplaceDraftAccount: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));

        verify(draftAccountService).replaceDraftAccount(eq(draftAccountId), any(ReplaceDraftAccountRequestDto.class));
    }

    @Test
    void testReplaceDraftAccount_trap403Response_boPermission() throws Exception {
        Long draftAccountId = 241L;

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validCreateRequestBody()))
            .andExpect(status().isForbidden())
            .andReturn();

    }

    @Test
    void testUpdateDraftAccount_success() throws Exception {
        Long draftAccountId = 241L;

        LocalDateTime testDateTime = LocalDateTime.of(2024, 10, 3, 14, 30, 0);

        DraftAccountEntity updatedEntity = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 5)
                              .businessUnitName("Cambridgeshire").build())
            .createdDate(testDateTime.minusDays(1))
            .submittedBy("BUUID1")
            .validatedDate(testDateTime)
            .validatedBy("BUUID1")
            .validatedByName("Donald Tester")
            .account(validAccountJson())
            .accountSnapshot("{\"defendant_name\":\"Company ABC\",\"created_date\":\"2024-10-02T14:30:00Z\","
                                 + "\"account_type\":\"Fine\",\"submitted_by\":\"BUUID1\","
                                 + "\"business_unit_name\":\"Cambridgeshire\","
                                 + "\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .accountType("Fines")
            .accountStatus(DraftAccountStatus.PENDING)
            .timelineData(validTimelineDataJson())
            .build();

        when(draftAccountService.updateDraftAccount(eq(draftAccountId), any(UpdateDraftAccountRequestDto.class)))
            .thenReturn(updatedEntity);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(validUpdateRequestBody()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(5))
            .andExpect(jsonPath("$.created_at").value("2024-10-02T14:30:00Z"))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.validated_at").value("2024-10-03T14:30:00Z"))
            .andExpect(jsonPath("$.validated_by").value("BUUID1"))
            .andExpect(jsonPath("$.validated_by_name").value("Donald Tester"))
            .andExpect(jsonPath("$.account.defendant.company_name")
                           .value("company"))
            .andExpect(jsonPath("$.account_snapshot.defendant_name").value("Company ABC"))
            .andExpect(jsonPath("$.account_snapshot.approved_date")
                           .value("2024-10-03T14:30:00Z"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Pending"))
            .andExpect(jsonPath("$.timeline_data").isArray())
            .andReturn();

        String body = result.getResponse().getContentAsString();
        log.info(":testUpdateDraftAccount: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));

        verify(draftAccountService).updateDraftAccount(eq(draftAccountId), any(UpdateDraftAccountRequestDto.class));
    }

    @Test
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

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isForbidden())
            .andReturn();

    }

    @Test
    void testUpdateDraftAccount_trap406Response() throws Exception {
        when(draftAccountService.updateDraftAccount(any(), any())).thenReturn(createDraftAccountEntity(BU_ID));
        shouldReturn406WhenResponseContentTypeNotSupported(
            patch(URL_BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(validUpdateRequestBody())
        );
    }

    @Test
    void testUpdateDraftAccount_trap408Response() throws Exception {
        shouldReturn408WhenTimeout(
            patch(URL_BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(validUpdateRequestBody()),
            when(draftAccountService.updateDraftAccount(any(), any()))
        );
    }

    @Test
    void testUpdateDraftAccount_trap503Response() throws Exception {
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            patch(URL_BASE + "/1").contentType(MediaType.APPLICATION_JSON).content(validUpdateRequestBody()),
            when(draftAccountService.updateDraftAccount(any(), any()))
        );
    }

    @Test
    void testPostDraftAccount_trap400Response() throws Exception {

        String expectedErrorMessageStart =
            "JSON Schema Validation Error: Validating against JSON schema 'addDraftAccountRequest.json',"
                + " found 14 validation errors:";

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(post(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidCreateRequestBody()))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value(containsString(expectedErrorMessageStart)))
            .andExpect(jsonPath("$.message").value(containsString("required property 'account_type' not found")))
            .andExpect(jsonPath("$.message").value(containsString("required property 'submitted_by' not found")))
            .andExpect(jsonPath("$.message").value(containsString("required property 'submitted_by_name' not found")))
            .andExpect(jsonPath("$.message").value(containsString("required property 'timeline_data' not found")));

    }

    @Test
    void testPostDraftAccount_permission() throws Exception {

        String validRequestBody = setupValidPostRequest();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        MvcResult result = mockMvc.perform(post(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestBody))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andExpect(jsonPath("$.account.account_type")
                           .value("Fine"))
            .andExpect(jsonPath("$.account.defendant.surname")
                           .value("LNAME"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    void testPostDraftAccount_trap403Response_noPermission() throws Exception {

        String validRequestBody = setupValidPostRequest();
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        MvcResult result = mockMvc.perform(post(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS] permission(s) are not enabled for the user."))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    void testPostDraftAccount_trap403Response_wrongPermission() throws Exception {

        String validRequestBody = setupValidPostRequest();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(
            permissionUser((short)5, Permissions.CHECK_VALIDATE_DRAFT_ACCOUNTS, Permissions.ACCOUNT_ENQUIRY));

        MvcResult result = mockMvc.perform(post(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Forbidden"))
            .andExpect(jsonPath("$.message").value(
                "For user null, [CREATE_MANAGE_DRAFT_ACCOUNTS] permission(s) are not enabled for the user."))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        log.info(":testPostDraftAccount_permission: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    void testPostDraftAccount_trap406Response() throws Exception {
        String validRequestBody = setupValidPostRequest();
        shouldReturn406WhenResponseContentTypeNotSupported(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody));
    }

    @Test
    void testPostDraftAccount_trap408Response() throws Exception {
        String validRequestBody = setupValidPostRequest();
        shouldReturn408WhenTimeout(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody),
            when(draftAccountService.submitDraftAccount(any())));
    }

    @Test
    void testPostDraftAccount_trap503Response() throws Exception {
        String validRequestBody = setupValidPostRequest();
        shouldReturn503WhenDownstreamServiceIsUnavailable(
            post(URL_BASE).contentType(MediaType.APPLICATION_JSON).content(validRequestBody),
            when(draftAccountService.submitDraftAccount(any())));
    }

    private String setupValidPostRequest() {
        String validRequestBody = validCreateRequestBody();
        AddDraftAccountRequestDto dto = ToJsonString.toClassInstance(validRequestBody, AddDraftAccountRequestDto.class);
        LocalDateTime created = LocalDateTime.now();
        DraftAccountEntity entity = toEntity(dto, created);
        when(draftAccountService.submitDraftAccount(any())).thenReturn(entity);
        return validRequestBody;
    }

    void shouldReturn406WhenResponseContentTypeNotSupported(MockHttpServletRequestBuilder reqBuilder) throws Exception {
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        mockMvc.perform(reqBuilder
                            .header("Authorization", "Bearer " + "some_value")
                            .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }


    void shouldReturn408WhenTimeout(MockHttpServletRequestBuilder reqBuilder, OngoingStubbing<?> stubbing)
        throws Exception {
        // Simulating a timeout exception when the service is called
        stubbing.thenThrow(new QueryTimeoutException());

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(reqBuilder
                            .header("Authorization", "Bearer " + "some_value"))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "error": "Request Timeout",
                    "message": "The request did not receive a response from the database within the timeout period"
                }"""));
    }


    void shouldReturn503WhenDownstreamServiceIsUnavailable(MockHttpServletRequestBuilder reqBuilder,
                                                           OngoingStubbing<?> stubbing) throws Exception {
        stubbing.thenAnswer(
            invocation -> {
                throw new PSQLException("Connection refused", PSQLState.CONNECTION_FAILURE, new ConnectException());
            });

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(reqBuilder
                            .header("Authorization", "Bearer " + "some_value"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                                          {
                                              "error": "Service Unavailable",
                                              "message": "Opal Fines Database is currently unavailable"
                                          }"""));
    }

    private static String validCreateRequestBody() {
        return """
            {
              "business_unit_id": 77,
              "submitted_by": "BUUID1",
              "submitted_by_name": "John",
              "account": {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
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
            }
            ,
              "account_type": "Fines",
              "account_status": "Submitted",
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

    private static String validUpdateRequestBody() {
        return "{\n"
            + "    \"account_status\": \"PENDING\",\n"
            + "    \"validated_by\": \"BUUID1\",\n"
            + "    \"business_unit_id\": 5,\n"
            + "    \"timeline_data\": " + validTimelineDataJson() + "\n"
            + "}";
    }

    private static String validTimelineDataJson() {
        return """
            [
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
            ]""";
    }

    private final String validAccountJson() {
        return """
            {
              "account_type": "Fine",
              "defendant_type": "Adult",
              "originator_name": "Police Force",
              "originator_id": 12345,
              "enforcement_court_id": 101,
              "collection_order_made": true,
              "collection_order_made_today": false,
              "payment_card_request": true,
              "account_sentence_date": "2023-12-01",
              "defendant": {
                "company_flag": true,
                "company_name": "company",
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
            }""";
    }

    private DraftAccountEntity toEntity(AddDraftAccountRequestDto dto,  LocalDateTime created) {
        return DraftAccountEntity.builder()
            .businessUnit(BusinessUnitEntity.builder().build())
            .createdDate(created)
            .submittedBy(dto.getSubmittedBy())
            .account(dto.getAccount())
            .accountSnapshot("{ \"data\": \"something snappy\"}")
            .accountType(dto.getAccountType())
            .accountStatus(SUBMITTED)
            .timelineData(dto.getTimelineData())
            .build();
    }

    //CEP 1 CEP1 - Invalid Request Payload (400)
    @ParameterizedTest
    @MethodSource("endpointsWithInvalidBodiesProvider")
    void methodsShouldReturn400_whenRequestPayloadIsInvalid(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .header("Accept", "application/json")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                            .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> endpointsWithInvalidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), invalidCreateRequestBody()),
                         Arguments.of(put(URL_BASE + "/1"), invalidCreateRequestBody()),
                         Arguments.of(patch(URL_BASE + "/1"), invalidCreateRequestBody())
        );
    }

    //CEP2 - Invalid or No Access Token (401) - Security Context required - test elsewhere

    //CEP3 - Not Authorised to perform the requested action (403)
    @ParameterizedTest
    @MethodSource("testCasesRequiringAuthorizationProvider")
    void methodsShouldReturn403_whenUserLacksPermission(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(noPermissionsUser());

        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    private static Stream<Arguments> testCasesRequiringAuthorizationProvider() {
        return Stream.of(
            Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/1"), validUpdateRequestBody()),
            Arguments.of(get(URL_BASE), "")  // GET endpoints with empty body
        );
    }

    //CEP4 - Resource Not Found (404) - applies to GET PUT PATCH & DELETE
    @ParameterizedTest
    @MethodSource("testCasesForResourceNotFoundProvider")
    void methodsShouldReturn404_whenResourceNotFound(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {
        // Set up a non-existent ID
        long nonExistentId = 999L;

        // Mock the service behavior
        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());

        // For GET return null
        DraftAccountEntity entity = Mockito.mock(DraftAccountEntity.class);
        when(entity.getBusinessUnit()).thenThrow(new EntityNotFoundException());
        when(draftAccountService.getDraftAccount(nonExistentId)).thenReturn(entity);

        // For PUT, throw EntityNotFoundException
        when(draftAccountService.replaceDraftAccount(eq(nonExistentId), any(ReplaceDraftAccountRequestDto.class)))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Draft Account not found with id: "
                                                                           + nonExistentId));
        // For PATCH, throw EntityNotFoundException
        when(draftAccountService.updateDraftAccount(eq(nonExistentId), any(UpdateDraftAccountRequestDto.class)))
            .thenThrow(new jakarta.persistence.EntityNotFoundException("Draft Account not found with id: "
                                                                           + nonExistentId));
        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isNotFound());
    }

    private static Stream<Arguments> testCasesForResourceNotFoundProvider() {
        return Stream.of(
            Arguments.of(get(URL_BASE + "/999"), ""),
            Arguments.of(put(URL_BASE + "/999"), validCreateRequestBody()),
            Arguments.of(patch(URL_BASE + "/999"), validUpdateRequestBody())
        );
    }

    //CEP5 - Unsupported Content Type for Response (406)
    @ParameterizedTest
    @MethodSource("testCasesWithValidBodiesProvider")
    void methodsShouldReturn406_whenAcceptHeaderIsNotSupported(
        MockHttpServletRequestBuilder requestBuilder, String requestBody) throws Exception {

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(allPermissionsUser());
        mockMvc.perform(requestBuilder
                            .header("Authorization", "Bearer some_value")
                            .header("Accept", "application/xml")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                            .andExpect(status().isNotAcceptable());
    }

    private static Stream<Arguments> testCasesWithValidBodiesProvider() {
        return Stream.of(Arguments.of(post(URL_BASE), validCreateRequestBody()),
            Arguments.of(put(URL_BASE + "/1"), "{}"),
            Arguments.of(patch(URL_BASE + "/1"), "{}"),
            Arguments.of(get(URL_BASE + "/1"), "{}")
        );
    }


}
