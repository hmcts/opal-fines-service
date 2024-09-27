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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.opal.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler;
import uk.gov.hmcts.opal.dto.ReplaceDraftAccountRequestDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@ContextConfiguration(classes = {DraftAccountController.class, GlobalExceptionHandler.class})
@ActiveProfiles({"integration"})
class DraftAccountControllerIntegrationTest {

    private static final String URL_BASE = "/draft-accounts/";

    private static final Logger logger = Logger.getLogger(DraftAccountControllerIntegrationTest.class.getSimpleName());

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

        MvcResult result = mockMvc.perform(get(URL_BASE + "1")
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

        assertTrue(jsonSchemaValidationService.isValid(body, "getDraftAccountResponse.json"));
    }


    @Test
    void testGetDraftAccountById_WhenDraftAccountDoesNotExist() throws Exception {
        when(draftAccountService.getDraftAccount(2L)).thenReturn(null);

        mockMvc.perform(get(URL_BASE + "2").header("authorization", "Bearer some_value"))
            .andExpect(status().isNotFound());
    }

    @Test
    void testPostDraftAccountsSearch() throws Exception {
        DraftAccountEntity draftAccountEntity = createDraftAccountEntity();

        when(draftAccountService.searchDraftAccounts(any(DraftAccountSearchDto.class)))
            .thenReturn(singletonList(draftAccountEntity));

        mockMvc.perform(post(URL_BASE + "search")
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
        mockMvc.perform(post(URL_BASE + "search")
                            .header("authorization", "Bearer some_value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"criteria\":\"2\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn408WhenTimeout() throws Exception {
        // Simulating a timeout exception when the repository is called
        doThrow(new QueryTimeoutException()).when(draftAccountService).getDraftAccount(1L);

        mockMvc.perform(get(URL_BASE + "1")
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

        mockMvc.perform(get(URL_BASE + "1")
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
            .accountSnapshot("{}")
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


        mockMvc.perform(get(URL_BASE + "1")
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

        MvcResult result = mockMvc.perform(delete(URL_BASE + "1")
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

        MvcResult result = mockMvc.perform(put(URL_BASE + draftAccountId)
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

        assertTrue(jsonSchemaValidationService.isValid(body, "getDraftAccountResponse.json"));

        verify(draftAccountService).replaceDraftAccount(eq(draftAccountId), any(ReplaceDraftAccountRequestDto.class));
    }

}
