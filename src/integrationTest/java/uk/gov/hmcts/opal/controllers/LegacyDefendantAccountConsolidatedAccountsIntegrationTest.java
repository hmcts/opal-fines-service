package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.legacy.LegacyConsolidatedAccount;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountConsolidatedAccountsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.repository.ConsolidatedAccountRepository;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "legacy"})
@DisplayName("Legacy Defendant Account Consolidated Accounts Integration Tests")
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
class LegacyDefendantAccountConsolidatedAccountsIntegrationTest extends AbstractIntegrationTest {

    private static final String URL_BASE = "/defendant-accounts";
    private static final String AUTH_HEADER = "Bearer test-token";

    @MockitoBean
    private UserStateService userStateService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private UserStateClientService userStateClientService;

    @MockitoBean
    private GatewayService gatewayService;

    @MockitoBean
    private ConsolidatedAccountRepository consolidatedAccountRepository;

    @BeforeEach
    void setupUserState() {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.allPermissionsUser());
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.01 returns consolidated child accounts from legacy gateway")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenLegacyMode_returnsGatewayPayload() throws Exception {
        ArgumentCaptor<LegacyGetDefendantAccountRequest> requestCaptor =
            ArgumentCaptor.forClass(LegacyGetDefendantAccountRequest.class);

        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            requestCaptor.capture(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

        performGetConsolidatedAccounts(233300L)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"9\""))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].account_id").value(233301))
            .andExpect(jsonPath("$[0].account_number").value("233301C"))
            .andExpect(jsonPath("$[0].first_name").value("Alex"))
            .andExpect(jsonPath("$[0].last_name").value("Jones"))
            .andExpect(jsonPath("$[0].date_imposed").value("2026-01-21"))
            .andExpect(jsonPath("$[0].imposed_by").value("Child Court"))
            .andExpect(jsonPath("$[0].reference").value("CHILD-REF"));

        verify(gatewayService).postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            eq(requestCaptor.getValue()),
            isNull()
        );
        assertEquals("233300", requestCaptor.getValue().getDefendantAccountId());
        verifyNoInteractions(consolidatedAccountRepository);
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.05 permits user with Search and View Accounts in the same business unit")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenPermissionInSameBusinessUnit_returnsOk() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 78, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));
        mockLegacyResponse();

        performGetConsolidatedAccounts(233300L)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.06 permits user with Search and View Accounts in a different business unit")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenPermissionInDifferentBusinessUnit_returnsOk() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext())
            .thenReturn(UserStateUtil.permissionUser((short) 77, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS));
        mockLegacyResponse();

        performGetConsolidatedAccounts(233300L)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.03 returns empty array when legacy has no child accounts")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenLegacyReturnsNoChildren_returnsEmptyArray() throws Exception {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            any(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(
            HttpStatus.OK,
            LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
                .version(2L)
                .consolidatedAccounts(List.of())
                .build(),
            null,
            null
        ));

        performGetConsolidatedAccounts(233300L)
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.04 returns 404 when legacy gateway returns not found")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenLegacyGatewayReturnsNotFound_returnsNotFound() throws Exception {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            any(),
            isNull()
        )).thenThrow(HttpClientErrorException.create(
            HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        performGetConsolidatedAccounts(999999999L)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.title").value("Defendant Account Not Found"))
            .andExpect(jsonPath("$.detail").value("Defendant account not found with id: 999999999"));
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.07 returns 403 when user lacks Search and View Accounts")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenUserLacksPermission_returnsForbidden() throws Exception {
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(UserStateUtil.noPermissionsUser());

        performGetConsolidatedAccounts(233300L)
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("User requires permission: Search and View Accounts"));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.08 returns 401 when credentials are missing")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenCredentialsMissing_returnsUnauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).getUserStateV1FromSecurityContext();

        mockMvc.perform(get(URL_BASE + "/233300/consolidated-accounts"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));

        verifyNoInteractions(gatewayService);
    }

    @Test
    @DisplayName("PO-2335 Legacy: INT.09 returns only documented consolidated account fields")
    @JiraStory("PO-2335")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_returnsOnlyDocumentedFields() throws Exception {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            any(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));

        MvcResult result = performGetConsolidatedAccounts(233300L)
            .andExpect(status().isOk())
            .andReturn();

        JsonNode child = objectMapper.readTree(result.getResponse().getContentAsString())
            .get(0);

        assertEquals(
            Set.of("account_id", "account_number", "first_name", "last_name", "date_imposed", "imposed_by",
                   "reference"),
            child.properties().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet())
        );
    }

    private ResultActions performGetConsolidatedAccounts(Long defendantAccountId) throws Exception {
        return mockMvc.perform(get(URL_BASE + "/" + defendantAccountId + "/consolidated-accounts")
                                   .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER)
                                   .accept(MediaType.APPLICATION_JSON));
    }

    private void mockLegacyResponse() {
        when(gatewayService.postToGateway(
            eq(LegacyDefendantAccountService.GET_CONSOLIDATED_ACCOUNTS),
            eq(LegacyGetDefendantAccountConsolidatedAccountsResponse.class),
            any(),
            isNull()
        )).thenReturn(new GatewayService.Response<>(HttpStatus.OK, legacyResponse(), null, null));
    }

    private LegacyGetDefendantAccountConsolidatedAccountsResponse legacyResponse() {
        return LegacyGetDefendantAccountConsolidatedAccountsResponse.builder()
            .version(9L)
            .consolidatedAccounts(List.of(
                legacyAccount(233302L, "233302C"),
                legacyAccount(233301L, "233301C")
            ))
            .build();
    }

    private LegacyConsolidatedAccount legacyAccount(Long accountId, String accountNumber) {
        return LegacyConsolidatedAccount.builder()
            .accountId(accountId)
            .accountNumber(accountNumber)
            .firstName("Alex")
            .lastName("Jones")
            .dateImposed(LocalDate.parse("2026-01-21"))
            .imposedBy("Child Court")
            .reference("CHILD-REF")
            .build();
    }
}
