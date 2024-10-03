package uk.gov.hmcts.opal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.QueryTimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
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
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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


@WebMvcTest
@ContextConfiguration(classes = {DraftAccountController.class, GlobalExceptionHandler.class})
@ActiveProfiles({"integration"})
class DraftAccountControllerIntegrationTest {

    private static final Logger logger = Logger.getLogger(DraftAccountControllerIntegrationTest.class.getSimpleName());
    private static final String URL_BASE = "/draft-accounts";
    private static final String GET_DRAFT_ACCOUNT_RESPONSE = "getDraftAccountResponse.json";
    private static final String GET_DRAFT_ACCOUNTS_RESPONSE = "getDraftAccountsResponse.json";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testGetDraftAccountById() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.getDraftAccount(1L)).thenReturn(draftAccountEntity);

        MvcResult result = mockMvc.perform(get(URL_BASE + "/1")
                            .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(1))
            .andExpect(jsonPath("$.business_unit_id").value(7))
            .andExpect(jsonPath("$.account_type").value("DRAFT"))
            .andExpect(jsonPath("$.submitted_by").value("Tony"))
            .andExpect(jsonPath("$.account_status").value("Submitted"))
            .andReturn();

        String body = result.getResponse().getContentAsString();

        logger.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));
    }


    @Test
    void testGetDraftAccountById_WhenDraftAccountDoesNotExist() throws Exception {
        when(draftAccountService.getDraftAccount(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "/2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetDraftAccountsSummaries_noParams() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(new UserState.DeveloperUserState());
        when(draftAccountService.getDraftAccounts(any(),any(), any()))
            .thenReturn(singletonList(draftAccountEntity));

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            // .param("business_unit", "1")
                            .contentType(MediaType.APPLICATION_JSON)));

        logger.info(":testGetDraftAccountsSummaries_noParams: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_permission() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();
        final Short businessId = (short)1;

        UserState user = UserStateUtil.permissionUser(businessId, Permissions.CREATE_MANAGE_DRAFT_ACCOUNTS);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any()))
            .thenReturn(singletonList(draftAccountEntity));

        String body = checkStandardSummaryExpectations(mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .contentType(MediaType.APPLICATION_JSON)));

        logger.info(":testGetDraftAccountsSummaries_permission: body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNTS_RESPONSE));
    }

    @Test
    void testGetDraftAccountsSummaries_noPermission() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();
        final Short businessId = (short)1;

        UserState user = UserStateUtil.permissionUser(businessId, Permissions.COLLECTION_ORDER);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(user);
        when(draftAccountService.getDraftAccounts(any(), any(), any()))
            .thenReturn(singletonList(draftAccountEntity));

        mockMvc.perform(get(URL_BASE)
                            .header("authorization", "Bearer some_value")
                            .param("business_unit", businessId.toString())
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private String checkStandardSummaryExpectations(ResultActions actions) throws Exception {
        return actions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.count").value(1))
            .andExpect(jsonPath("$.summaries[0].draft_account_id").value(1))
            .andExpect(jsonPath("$.summaries[0].business_unit_id").value(7))
            .andExpect(jsonPath("$.summaries[0].account_type").value("DRAFT"))
            .andExpect(jsonPath("$.summaries[0].submitted_by").value("Tony"))
            .andExpect(jsonPath("$.summaries[0].account_status").value("Submitted"))
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void testPostDraftAccountsSearch() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.searchDraftAccounts(any(DraftAccountSearchDto.class)))
            .thenReturn(singletonList(draftAccountEntity));

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
    void testPostDraftAccountsSearch_WhenDraftAccountDoesNotExist() throws Exception {
        mockMvc.perform(post(URL_BASE + "/search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn408WhenTimeout() throws Exception {
        // Simulating a timeout exception when the repository is called
        doThrow(new QueryTimeoutException()).when(draftAccountService).getDraftAccount(1L);

        mockMvc.perform(get(URL_BASE + "/1")
                            .header("Authorization", "Bearer " + "some_value"))
            .andExpect(status().isRequestTimeout())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "error": "Request Timeout",
                    "message": "The request did not receive a response from the database within the timeout period"
                }"""));
    }

    @Test
    void shouldReturn406WhenResponseContentTypeNotSupported() throws Exception {

        when(draftAccountService.getDraftAccount(1L)).thenReturn(createDraftAccountEntity());

        mockMvc.perform(get(URL_BASE + "/1")
                            .header("Authorization", "Bearer " + "some_value")
                            .accept("application/xml"))
            .andExpect(status().isNotAcceptable());
    }

    private DraftAccountEntity createDraftAccountEntity() {
        return DraftAccountEntity.builder()
            .draftAccountId(1L)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short)007).build())
            .createdDate(LocalDate.of(2023, 1, 2).atStartOfDay())
            .submittedBy("Tony")
            .accountType("DRAFT")
            .accountStatus(DraftAccountStatus.SUBMITTED)
            .account("{}")
            .accountSnapshot("{ \"data\": \"something snappy\"}")
            .timelineData("{}")
            .build();
    }

    @Test
    void shouldReturn503WhenDownstreamServiceIsUnavailable() throws Exception {

        Mockito.doAnswer(
                invocation -> {
                    throw new PSQLException("Connection refused", PSQLState.CONNECTION_FAILURE, new ConnectException());
                })
            .when(draftAccountService).getDraftAccount(1L);


        mockMvc.perform(get(URL_BASE + "/1")
                            .header("Authorization", "Bearer " + "some_value"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType("application/json"))
            .andExpect(content().json("""
                {
                    "error": "Service Unavailable",
                    "message": "Opal Fines Database is currently unavailable"
                }"""));
    }

    @Test
    void testDeleteDraftAccountById() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.getDraftAccount(1L)).thenReturn(draftAccountEntity);

        MvcResult result = mockMvc.perform(delete(URL_BASE + "/1")
                                               .header("authorization", "Bearer some_value"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("Draft Account '1' deleted"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        logger.info(":testGetDraftAccountById: Response body:\n" + ToJsonString.toPrettyJson(body));
    }

    @Test
    void testReplaceDraftAccount() throws Exception {
        Long draftAccountId = 241L;
        String requestBody = """
    {
        "account": {
            "accountCreateRequest": {
                "Defendant": {
                    "CompanyName": "Company ABC",
                    "Surname": "LNAME",
                    "Fornames": "FNAME",
                    "DOB": "2000-01-01"
                },
                "Account": {
                    "AccountType": "Fine"
                }
            }
        },
        "account_status": "",
        "account_summary_data": "",
        "account_type": "Fines",
        "business_unit_id": 5,
        "submitted_by": "BUUID1",
        "timeline_data": {
            "stuff": "yes"
        },
        "court": "test"
    }
            """;

        LocalDateTime testDateTime = LocalDateTime.of(2024, 9, 26, 15, 0, 0);

        DraftAccountEntity updatedEntity = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 5)
                              .businessUnitName("Cambridgeshire").build())
            .createdDate(testDateTime)
            .submittedBy("BUUID1")
            .account("{\"accountCreateRequest\":{\"Defendant\":{\"CompanyName\":\"Company ABC\",\"Surname\""
                         + ":\"LNAME\",\"Fornames\":\"FNAME\",\"DOB\":\"2000-01-01\"},\"Account\""
                         + ":{\"AccountType\":\"Fine\"}}}")
            .accountSnapshot("{\"defendant_name\":\"Company ABC\",\"created_date\":\"2024-09-26T15:00:00Z\","
                                 + "\"account_type\":\"Fine\",\"submitted_by\":\"BUUID1\","
                                 + "\"business_unit_name\":\"Cambridgeshire\"}")
            .accountType("Fines")
            .accountStatus(DraftAccountStatus.RESUBMITTED)
            .timelineData("{\"stuff\":\"yes\"}")
            .build();

        when(draftAccountService.replaceDraftAccount(eq(draftAccountId), any(ReplaceDraftAccountRequestDto.class)))
            .thenReturn(updatedEntity);

        MvcResult result = mockMvc.perform(put(URL_BASE + "/" + draftAccountId)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(5))
            .andExpect(jsonPath("$.created_at").value("2024-09-26T15:00:00Z"))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.account.accountCreateRequest.Defendant.CompanyName")
                           .value("Company ABC"))
            .andExpect(jsonPath("$.account_snapshot.defendant_name").value("Company ABC"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Resubmitted"))
            .andExpect(jsonPath("$.timeline_data.stuff").value("yes"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        logger.info(":testReplaceDraftAccount: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));

        verify(draftAccountService).replaceDraftAccount(eq(draftAccountId), any(ReplaceDraftAccountRequestDto.class));
    }

    @Test
    void testUpdateDraftAccount() throws Exception {
        Long draftAccountId = 241L;
        String requestBody = """
            {
                "account_status": "PENDING",
                "validated_by": "BUUID1",
                "business_unit_id": 5,
                "timeline_data": {"test":"yes"}
            }
            """;

        LocalDateTime testDateTime = LocalDateTime.of(2024, 10, 3, 14, 30, 0);

        DraftAccountEntity updatedEntity = DraftAccountEntity.builder()
            .draftAccountId(draftAccountId)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 5)
                              .businessUnitName("Cambridgeshire").build())
            .createdDate(testDateTime.minusDays(1))
            .submittedBy("BUUID1")
            .validatedDate(testDateTime)
            .validatedBy("BUUID1")
            .account("{\"accountCreateRequest\":{\"Defendant\":{\"CompanyName\":\"Company ABC\",\"Surname\":\"LNAME\","
                         + "\"Fornames\":\"FNAME\",\"DOB\":\"2000-01-01\"},\"Account\":{\"AccountType\":\"Fine\"}}}")
            .accountSnapshot("{\"defendant_name\":\"Company ABC\",\"created_date\":\"2024-10-02T14:30:00Z\","
                                 + "\"account_type\":\"Fine\",\"submitted_by\":\"BUUID1\","
                                 + "\"business_unit_name\":\"Cambridgeshire\","
                                 + "\"approved_date\":\"2024-10-03T14:30:00Z\"}")
            .accountType("Fines")
            .accountStatus(DraftAccountStatus.PENDING)
            .timelineData("{\"test\":\"yes\"}")
            .build();

        when(draftAccountService.updateDraftAccount(eq(draftAccountId), any(UpdateDraftAccountRequestDto.class)))
            .thenReturn(updatedEntity);

        MvcResult result = mockMvc.perform(patch(URL_BASE + "/" + draftAccountId)
                                               .header("authorization", "Bearer some_value")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.draft_account_id").value(draftAccountId))
            .andExpect(jsonPath("$.business_unit_id").value(5))
            .andExpect(jsonPath("$.created_at").value("2024-10-02T14:30:00Z"))
            .andExpect(jsonPath("$.submitted_by").value("BUUID1"))
            .andExpect(jsonPath("$.validated_at").value("2024-10-03T14:30:00Z"))
            .andExpect(jsonPath("$.validated_by").value("BUUID1"))
            .andExpect(jsonPath("$.account.accountCreateRequest.Defendant.CompanyName")
                           .value("Company ABC"))
            .andExpect(jsonPath("$.account_snapshot.defendant_name").value("Company ABC"))
            .andExpect(jsonPath("$.account_snapshot.approved_date")
                           .value("2024-10-03T14:30:00Z"))
            .andExpect(jsonPath("$.account_type").value("Fines"))
            .andExpect(jsonPath("$.account_status").value("Pending"))
            .andExpect(jsonPath("$.timeline_data.test").value("yes"))
            .andReturn();

        String body = result.getResponse().getContentAsString();
        logger.info(":testUpdateDraftAccount: Response body:\n" + ToJsonString.toPrettyJson(body));

        assertTrue(jsonSchemaValidationService.isValid(body, GET_DRAFT_ACCOUNT_RESPONSE));

        verify(draftAccountService).updateDraftAccount(eq(draftAccountId), any(UpdateDraftAccountRequestDto.class));
    }
}
