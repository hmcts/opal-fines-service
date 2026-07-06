package uk.gov.hmcts.opal.controllers;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@ActiveProfiles({"integration", "opal"})
@TestPropertySource(properties = {
    "launchdarkly.enabled=false",
    "launchdarkly.default-flag-values.release-1b=true"
})
@DisplayName("Defendant Account Consolidated Accounts Controller Integration Tests")
class OpalDefendantAccountConsolidatedAccountsIntegrationTest extends AbstractOpalDefendantsIntegrationTest {

    private static final long MASTER_ACCOUNT_ID = 233300L;
    private static final long CHILD_ACCOUNT_ID = 233301L;
    private static final long OTHER_MASTER_ACCOUNT_ID = 233302L;
    private static final long OTHER_CHILD_ACCOUNT_ID = 233303L;
    private static final long EMPTY_MASTER_ACCOUNT_ID = 233304L;
    private static final String URL = URL_BASE + "/%d/consolidated-accounts";

    @BeforeEach
    void setupConsolidatedAccountsData() {
        authorise((short) 79, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        jdbcTemplate.update("""
            INSERT INTO business_units (business_unit_id, business_unit_name, business_unit_code, business_unit_type,
                                        welsh_language)
            VALUES (79, 'PO-2333 Business Unit', 'P3', 'Area', FALSE)
            ON CONFLICT (business_unit_id) DO NOTHING
            """);

        insertDefendantAccount(MASTER_ACCOUNT_ID, 79, "233300M", 12, "Master Court", "MASTER-REF");
        insertDefendantAccount(CHILD_ACCOUNT_ID, 79, "233301C", 3, "Child Court", "CHILD-REF");
        insertDefendantAccount(OTHER_MASTER_ACCOUNT_ID, 79, "233302M", 4, "Other Master Court", "OTHER-MASTER");
        insertDefendantAccount(OTHER_CHILD_ACCOUNT_ID, 79, "233303C", 5, "Other Child Court", "OTHER-REF");
        insertDefendantAccount(EMPTY_MASTER_ACCOUNT_ID, 79, "233304M", 6, "Empty Master Court", "EMPTY-REF");

        insertDefendantParty(CHILD_ACCOUNT_ID, "Alex", "Jones");
        insertDefendantParty(OTHER_CHILD_ACCOUNT_ID, "Casey", "Smith");

        insertConsolidationTransaction(23330001L, MASTER_ACCOUNT_ID, CHILD_ACCOUNT_ID);
        insertConsolidationTransaction(23330002L, OTHER_MASTER_ACCOUNT_ID, OTHER_CHILD_ACCOUNT_ID);
    }

    @AfterEach
    void deleteConsolidatedAccountsData() {
        jdbcTemplate.update("""
            DELETE FROM defendant_transactions
            WHERE defendant_transaction_id IN (23330001, 23330002)
            """);
        jdbcTemplate.update("""
            DELETE FROM defendant_account_parties
            WHERE defendant_account_party_id IN (233301, 233303)
            """);
        jdbcTemplate.update("""
            DELETE FROM parties
            WHERE party_id IN (233301, 233303)
            """);
        jdbcTemplate.update("""
            DELETE FROM defendant_accounts
            WHERE defendant_account_id IN (233300, 233301, 233302, 233303, 233304)
            """);
        jdbcTemplate.update("DELETE FROM business_units WHERE business_unit_id = 79");
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
            .andExpect(jsonPath("$.consolidated_accounts", hasSize(1)))
            .andExpect(jsonPath("$.consolidated_accounts[0].account_id").value(CHILD_ACCOUNT_ID))
            .andExpect(jsonPath("$.consolidated_accounts[0].account_number").value("233301C"))
            .andExpect(jsonPath("$.consolidated_accounts[0].first_name").value("Alex"))
            .andExpect(jsonPath("$.consolidated_accounts[0].last_name").value("Jones"))
            .andExpect(jsonPath("$.consolidated_accounts[0].date_imposed").value("2026-01-21"))
            .andExpect(jsonPath("$.consolidated_accounts[0].imposted_by").value("Child Court"))
            .andExpect(jsonPath("$.consolidated_accounts[0].reference").value("CHILD-REF"));
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
            .get("consolidated_accounts")
            .get(0);

        assertEquals(
            Set.of("account_id", "account_number", "first_name", "last_name", "date_imposed", "imposted_by",
                   "reference"),
            child.properties().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet())
        );
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
            .andExpect(jsonPath("$.consolidated_accounts[*].account_id", containsInAnyOrder((int) CHILD_ACCOUNT_ID)))
            .andExpect(jsonPath("$.consolidated_accounts[?(@.account_id == %d)]".formatted(OTHER_CHILD_ACCOUNT_ID))
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
            .andExpect(jsonPath("$.consolidated_accounts", hasSize(0)));
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
            .andExpect(jsonPath("$.retriable").value(false));
    }

    @Test
    @DisplayName("PO-2333: INT.08 permits user with Search and View Accounts in a different business unit")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenPermissionInDifferentBusinessUnit_returnsOk() throws Exception {
        authorise((short) 78, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);

        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.consolidated_accounts", hasSize(1)));
    }

    @Test
    @DisplayName("PO-2333: INT.09 returns 403 when user lacks Search and View Accounts")
    @JiraStory("PO-2333")
    @JiraEpic("PO-1286")
    void getConsolidatedAccounts_whenMissingPermission_returnsForbidden() throws Exception {
        userStateStub.setupWithNoPermissions();

        mockMvc.perform(get(URL.formatted(MASTER_ACCOUNT_ID))
                .with(userStateStub.getAuthenticaitonRequestPostProcessor())
                .header(HttpHeaders.AUTHORIZATION, userStateStub.getBearerToken()))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.retriable").value(false));
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

    private void insertDefendantAccount(long accountId, int businessUnitId, String accountNumber, long version,
                                        String imposedByName, String reference) {
        jdbcTemplate.update("""
            INSERT INTO defendant_accounts (
                defendant_account_id, version_number, business_unit_id, account_number, imposed_hearing_date,
                amount_paid, account_balance, amount_imposed, account_status, allow_writeoffs, allow_cheques,
                account_type, collection_order, payment_card_requested, originator_name, imposed_by_name,
                prosecutor_case_reference
            ) VALUES (?, ?, ?, ?, TIMESTAMP '2026-01-21 10:15:00', 0.00, 100.00, 100.00, 'L', 'N', 'N',
                'Fine', 'N', 'N', 'PO-2333 Court', ?, ?)
            """, accountId, version, businessUnitId, accountNumber, imposedByName, reference);
    }

    private void insertDefendantParty(long defendantAccountId, String firstName, String lastName) {
        jdbcTemplate.update("""
            INSERT INTO parties (party_id, organisation, forenames, surname)
            VALUES (?, FALSE, ?, ?)
            """, defendantAccountId, firstName, lastName);
        jdbcTemplate.update("""
            INSERT INTO defendant_account_parties (
                defendant_account_party_id, defendant_account_id, party_id, association_type, debtor
            ) VALUES (?, ?, ?, 'Defendant', TRUE)
            """, defendantAccountId, defendantAccountId, defendantAccountId);
    }

    private void insertConsolidationTransaction(long transactionId, long masterAccountId, long childAccountId) {
        jdbcTemplate.update("""
            INSERT INTO defendant_transactions (
                defendant_transaction_id, defendant_account_id, posted_date, posted_by, transaction_type,
                transaction_amount, status_date, associated_record_type, associated_record_id, status, posted_by_name
            ) VALUES (?, ?, TIMESTAMP '2026-01-21 12:00:00', 'po2333', 'CONSOL', 0.00,
                TIMESTAMP '2026-01-21 12:00:00', 'defendant_accounts', ?, 'P', 'PO-2333 User')
            """, transactionId, masterAccountId, String.valueOf(childAccountId));
    }
}
