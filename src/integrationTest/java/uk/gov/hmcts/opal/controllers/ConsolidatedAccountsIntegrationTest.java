package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_consolidated_accounts.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "classpath:db/insertData/insert_into_consolidated_accounts.sql",
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_consolidated_accounts.sql",
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@DisplayName("Defendant Account Consolidated Accounts Controller Integration Tests")
class ConsolidatedAccountsIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long MASTER_ACCOUNT_ID = 233300L;
    private static final long CHILD_ACCOUNT_ID = 233301L;
    private static final long OTHER_MASTER_ACCOUNT_ID = 233302L;
    private static final long OTHER_CHILD_ACCOUNT_ID = 233303L;
    private static final long EMPTY_MASTER_ACCOUNT_ID = 233304L;
    private static final short BUSINESS_UNIT_ID = 78;
    private static final short DIFFERENT_BUSINESS_UNIT_ID = 77;
    private static final String URL = URL_BASE + "/%d/consolidated-accounts";

    @MockitoBean
    private UserStateService userStateService;

    @BeforeEach
    void setupConsolidatedAccountsData() {
        authorise(BUSINESS_UNIT_ID, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        mockUserWithPermission(BUSINESS_UNIT_ID);
    }

    @Test
    @DisplayName("PO-2333: INT.01 returns consolidated child accounts for a valid master account")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenMasterHasChildren_returnsOkWithPayload() throws Exception {
        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().string(HttpHeaders.ETAG, "\"12\""))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].account_id").value(CHILD_ACCOUNT_ID))
            .andExpect(jsonPath("$[0].account_number").value("233301C"))
            .andExpect(jsonPath("$[0].first_name").value("Alex"))
            .andExpect(jsonPath("$[0].last_name").value("Jones"))
            .andExpect(jsonPath("$[0].date_imposed").value("2026-01-21"))
            .andExpect(jsonPath("$[0].imposed_by").value("Child Court"))
            .andExpect(jsonPath("$[0].reference").value("CHILD-REF"));
    }

    @Test
    @DisplayName("PO-2333: INT.03 response contains only documented consolidated account fields")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_returnsOnlyDocumentedFields() throws Exception {
        MvcResult result = mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode child = objectMapper.readTree(result.getResponse().getContentAsString())
            .get(0);

        assertEquals(
            Set.of("account_id", "account_number", "first_name", "last_name", "date_imposed", "imposed_by",
                   "reference"),
            child.properties().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("PO-2333: INT.04 filters consolidated accounts by master account id")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_filtersByMasterAccountId() throws Exception {
        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].account_id", containsInAnyOrder((int) CHILD_ACCOUNT_ID)))
            .andExpect(jsonPath("$[?(@.account_id == %d)]".formatted(OTHER_CHILD_ACCOUNT_ID))
                .doesNotExist());
    }

    @Test
    @DisplayName("PO-2333: INT.05 returns empty array when master has no consolidated children")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenNoChildren_returnsEmptyArray() throws Exception {
        mockMvc.perform(get(URL.formatted(EMPTY_MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ETAG, "\"6\""))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("PO-2333: INT.06 returns 404 when defendant account does not exist")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenMasterDoesNotExist_returnsNotFound() throws Exception {
        mockMvc.perform(get(URL.formatted(999999999L))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.title").value("Defendant Account Not Found"))
            .andExpect(jsonPath("$.detail").value("Defendant account not found with id: 999999999"));
    }

    @Test
    @DisplayName("PO-2333: INT.08 permits user with Search and View Accounts in a different business unit")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenPermissionInDifferentBusinessUnit_returnsOk() throws Exception {
        authorise(DIFFERENT_BUSINESS_UNIT_ID, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        mockUserWithPermission(DIFFERENT_BUSINESS_UNIT_ID);

        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("PO-2333: INT.09 returns 403 when user lacks Search and View Accounts")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenMissingPermission_returnsForbidden() throws Exception {
        userStateStub.setupWithNoPermissions();
        doReturn(UserStateUtil.noPermissionsUser())
            .when(userStateService).getUserStateV1FromSecurityContext();

        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false))
            .andExpect(jsonPath("$.title").value("Forbidden"))
            .andExpect(jsonPath("$.detail").value("User requires permission: Search and View Accounts"));
    }

    @Test
    @DisplayName("PO-2333: INT.10 returns 401 when credentials are missing")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenCredentialsMissing_returnsUnauthorized() throws Exception {
        doThrow(new ResponseStatusException(UNAUTHORIZED, "Unauthorized"))
            .when(userStateService).getUserStateV1FromSecurityContext();

        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID)))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.detail").value("Unauthorized"))
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2333: INT.12 returns all consolidated accounts without pagination")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    @Sql(
        scripts = "classpath:db/insertData/insert_into_consolidated_accounts_bulk.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void getConsolidatedAccounts_whenManyChildrenExist_returnsFullArray() throws Exception {
        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(21)))
            .andExpect(jsonPath("$[0].account_id").value(233301))
            .andExpect(jsonPath("$[1].account_id").value(233400))
            .andExpect(jsonPath("$[20].account_id").value(233419))
            .andExpect(jsonPath(
                "$[*].account_id",
                containsInAnyOrder(
                    233301,
                    233400, 233401, 233402, 233403, 233404, 233405, 233406, 233407, 233408, 233409,
                    233410, 233411, 233412, 233413, 233414, 233415, 233416, 233417, 233418, 233419)))
            .andExpect(jsonPath("$[?(@.account_id == 233419)]", hasSize(1)));
    }

    @Test
    @DisplayName("PO-2333: INT.11 repeated GET returns identical body")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenRepeated_returnsIdenticalBody() throws Exception {
        MvcResult first = mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn();
        MvcResult second = mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andReturn();

        assertEquals(first.getResponse().getContentAsString(), second.getResponse().getContentAsString());
    }

    private void mockUserWithPermission(short businessUnitId) {
        doReturn(UserStateUtil.permissionUser(businessUnitId, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS))
            .when(userStateService).getUserStateV1FromSecurityContext();
    }
}
