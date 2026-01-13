package uk.gov.hmcts.opal.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

@ActiveProfiles({"integration"})
@Slf4j(topic = "opal.DefendantAccountSummaryViewIntegrationTest")
@DisplayName("Defendant Account Summary View Integration Tests")
@Sql(
    scripts = "classpath:db/insertData/insert_into_defendant_accounts.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_from_defendant_accounts.sql",
    executionPhase = AFTER_TEST_CLASS
)
class DefendantAccountSummaryViewIntegrationTest extends AbstractIntegrationTest {

    private static final String VIEW = "v_defendant_accounts_summary";

    /**
     * Seed assumptions (PO-2629 isolated dataset).
     * - Account 262901: multiple payment_terms (1 active + 1 inactive) -> should produce ONE summary row
     * - Account 262902: inactive-only payment_terms -> should produce NO summary row
     *
     */

    private static final long ACCOUNT_MULTI_TERMS_ONE_ACTIVE = 262901L;
    private static final long ACCOUNT_NO_ACTIVE_TERMS = 262902L;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("PO-2629 INT.01 – Active payment terms returns one row")
    void int01_activePaymentTerms_returnsOneRow() {
        List<Map<String, Object>> rows = byAccountId(ACCOUNT_MULTI_TERMS_ONE_ACTIVE);

        assertThat(rows).hasSize(1);

        Map<String, Object> row = rows.get(0);
        assertThat(longVal(row, "defendant_account_id")).isEqualTo(ACCOUNT_MULTI_TERMS_ONE_ACTIVE);
        assertThat(row.get("account_number")).isNotNull();
    }

    @Test
    @DisplayName("PO-2629 INT.02 – Multiple payment terms does not duplicate summary rows")
    void int02_multiplePaymentTerms_doesNotDuplicateSummaryRows() {

        // Prove the account really has multiple payment_terms (so the test is meaningful)
        Long termCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM payment_terms WHERE defendant_account_id = ?",
            Long.class,
            ACCOUNT_MULTI_TERMS_ONE_ACTIVE
        );
        assertThat(termCount)
            .as("Seed must include >1 payment_terms row for account " + ACCOUNT_MULTI_TERMS_ONE_ACTIVE)
            .isNotNull()
            .isGreaterThan(1);

        List<Map<String, Object>> rows = byAccountId(ACCOUNT_MULTI_TERMS_ONE_ACTIVE);
        assertThat(rows).hasSize(1);
    }

    @Test
    @DisplayName("PO-2629 INT.03 – No active payment terms yields no summary row")
    void int03_noActivePaymentTerms_yieldsNoRow() {
        List<Map<String, Object>> rows = byAccountId(ACCOUNT_NO_ACTIVE_TERMS);

        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("PO-2629 INT.04 – View contains at most one row per defendant_account_id")
    void int04_viewHasAtMostOneRowPerDefendantAccount() {

        List<Long> ids = jdbcTemplate.queryForList(
            "SELECT defendant_account_id FROM " + VIEW,
            Long.class
        );

        Map<Long, Long> counts =
            ids.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        assertThat(counts.values()).allMatch(c -> c == 1L);
    }

    @Test
    @DisplayName("PO-2629 INT.05 – Regression: repeat query for same account is stable")
    void int05_regression_repeatQueryStable() {
        List<Map<String, Object>> first = byAccountId(ACCOUNT_MULTI_TERMS_ONE_ACTIVE);
        List<Map<String, Object>> second = byAccountId(ACCOUNT_MULTI_TERMS_ONE_ACTIVE);

        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("PO-2629 INT.06 – Regression: inactive-only account stays excluded")
    void int06_regression_inactiveOnlyAccountExcluded() {
        List<Map<String, Object>> first = byAccountId(ACCOUNT_NO_ACTIVE_TERMS);
        List<Map<String, Object>> second = byAccountId(ACCOUNT_NO_ACTIVE_TERMS);

        assertThat(first).isEmpty();
        assertThat(second).isEmpty();
    }

    // Helpers
    private List<Map<String, Object>> byAccountId(long defendantAccountId) {
        String sql = "SELECT * FROM " + VIEW + " WHERE defendant_account_id = ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, defendantAccountId);
        log.info("Query {} for defendant_account_id={}, rows={}", VIEW, defendantAccountId, rows.size());
        return rows;
    }

    private static Long longVal(Map<String, Object> row, String column) {
        Object v = row.get(column);
        return v == null ? null : ((Number) v).longValue();
    }
}
