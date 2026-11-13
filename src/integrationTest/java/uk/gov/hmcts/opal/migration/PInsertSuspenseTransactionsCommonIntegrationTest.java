package uk.gov.hmcts.opal.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("p_insert_suspense_transactions_common integration tests")
@Sql(
    scripts = "classpath:db/insertData/insert_p_insert_suspense_transactions_common.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_p_insert_suspense_transactions_common.sql",
    executionPhase = AFTER_TEST_CLASS
)
@Transactional
class PInsertSuspenseTransactionsCommonIntegrationTest extends AbstractIntegrationTest {

    private static final long SUSPENSE_ITEM_ID = 99000001034601L;
    private static final LocalDateTime POSTED_DATE = LocalDateTime.of(2026, 11, 3, 12, 34, 56, 123_000_000);
    private static final String PROCEDURE_CALL = """
        CALL public.p_insert_suspense_transactions_common(
            ?, ?, ?, ?,
            CAST(? AS public.t_suspense_transaction_type_enum),
            ?,
            CAST(? AS public.t_associated_record_type_enum),
            ?, ?,
            CAST(? AS public.t_reversed_enum),
            NULL
        )
        """;

    @Test
    @JiraStory("PO-10346")
    @JiraEpic("PO-9048")
    @DisplayName("Flyway creates the procedure with ten input parameters and one output parameter")
    void shouldCreateProcedureWithExpectedSignature() {
        Map<String, Object> procedure = jdbcTemplate.queryForMap("""
            SELECT p.pronargs AS input_count,
                   (SELECT COUNT(*) FROM unnest(p.proargmodes) mode WHERE mode = 'o') AS output_count
            FROM pg_proc p
            JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE n.nspname = 'public'
              AND p.proname = 'p_insert_suspense_transactions_common'
              AND p.prokind = 'p'
            """);

        assertAll(
            () -> assertThat(((Number) procedure.get("input_count")).intValue()).isEqualTo(10),
            () -> assertThat(((Number) procedure.get("output_count")).intValue()).isEqualTo(1)
        );
    }

    @Test
    @JiraStory("PO-10346")
    @JiraEpic("PO-9048")
    @DisplayName("Inserts every supplied value and returns the generated transaction ID")
    void shouldInsertAllValuesAndReturnGeneratedId() {
        String postedBy = "U".repeat(20);
        String postedByName = "N".repeat(100);
        BigDecimal amount = new BigDecimal("1234567890123456.78");
        String associatedRecordId = "QA-ASSOCIATED-RECORD-000000001";
        String text = "Full field mapping test. ".repeat(20);

        long transactionId = callProcedure(
            SUSPENSE_ITEM_ID,
            POSTED_DATE,
            postedBy,
            postedByName,
            "UN",
            amount,
            "suspense_items",
            associatedRecordId,
            text,
            "R"
        );

        Map<String, Object> transaction = findTransaction(transactionId);

        assertAll(
            () -> assertThat(transactionId).isPositive(),
            () -> assertThat(transaction.get("suspense_item_id")).isEqualTo(SUSPENSE_ITEM_ID),
            () -> assertThat(((Timestamp) transaction.get("posted_date")).toLocalDateTime()).isEqualTo(POSTED_DATE),
            () -> assertThat(transaction.get("posted_by")).isEqualTo(postedBy),
            () -> assertThat(transaction.get("posted_by_name")).isEqualTo(postedByName),
            () -> assertThat(transaction.get("transaction_type")).hasToString("UN"),
            () -> assertThat(transaction.get("amount")).isEqualTo(amount),
            () -> assertThat(transaction.get("associated_record_type")).hasToString("suspense_items"),
            () -> assertThat(transaction.get("associated_record_id")).isEqualTo(associatedRecordId),
            () -> assertThat(transaction.get("text")).isEqualTo(text),
            () -> assertThat(transaction.get("reversed")).hasToString("R")
        );
    }

    @Test
    @JiraStory("PO-10346")
    @JiraEpic("PO-9048")
    @DisplayName("Accepts null for every optional value")
    void shouldInsertNullOptionalValues() {
        long transactionId = callProcedure(
            SUSPENSE_ITEM_ID,
            POSTED_DATE,
            null,
            null,
            "UN",
            BigDecimal.ZERO,
            null,
            null,
            null,
            null
        );

        Map<String, Object> transaction = findTransaction(transactionId);

        assertAll(
            () -> assertThat(transaction.get("amount")).isEqualTo(new BigDecimal("0.00")),
            () -> assertThat(transaction.get("posted_by")).isNull(),
            () -> assertThat(transaction.get("posted_by_name")).isNull(),
            () -> assertThat(transaction.get("associated_record_type")).isNull(),
            () -> assertThat(transaction.get("associated_record_id")).isNull(),
            () -> assertThat(transaction.get("text")).isNull(),
            () -> assertThat(transaction.get("reversed")).isNull()
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mandatoryNullParameters")
    @JiraStory("PO-10346")
    @JiraEpic("PO-9048")
    @DisplayName("Propagates PostgreSQL errors for null mandatory values")
    void shouldPropagateNotNullViolation(
        String scenario,
        Long suspenseItemId,
        LocalDateTime postedDate,
        String transactionType,
        BigDecimal amount
    ) {
        assertSqlState(
            "23502",
            () -> callProcedure(
                suspenseItemId,
                postedDate,
                "PO10346QA",
                scenario,
                transactionType,
                amount,
                null,
                null,
                null,
                null
            )
        );
    }

    @Test
    @JiraStory("PO-10346")
    @JiraEpic("PO-9048")
    @DisplayName("Propagates the foreign-key violation for an unknown suspense item")
    void shouldPropagateForeignKeyViolation() {
        assertSqlState(
            "23503",
            () -> callProcedure(
                -1L,
                POSTED_DATE,
                "PO10346QA",
                "Unknown suspense item",
                "UN",
                BigDecimal.ONE,
                null,
                null,
                null,
                null
            )
        );
    }

    private long callProcedure(
        Long suspenseItemId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String transactionType,
        BigDecimal amount,
        String associatedRecordType,
        String associatedRecordId,
        String text,
        String reversed
    ) {
        return jdbcTemplate.queryForObject(
            PROCEDURE_CALL,
            Long.class,
            suspenseItemId,
            postedDate,
            postedBy,
            postedByName,
            transactionType,
            amount,
            associatedRecordType,
            associatedRecordId,
            text,
            reversed
        );
    }

    private Map<String, Object> findTransaction(long transactionId) {
        return jdbcTemplate.queryForMap("""
            SELECT suspense_item_id,
                   posted_date,
                   posted_by,
                   posted_by_name,
                   transaction_type,
                   amount,
                   associated_record_type,
                   associated_record_id,
                   text,
                   reversed
            FROM suspense_transactions
            WHERE suspense_transaction_id = ?
            """, transactionId);
    }

    private void assertSqlState(String expectedSqlState, Runnable procedureCall) {
        assertThatThrownBy(procedureCall::run)
            .isInstanceOf(DataIntegrityViolationException.class)
            .rootCause()
            .isInstanceOfSatisfying(
                SQLException.class,
                exception -> assertThat(exception.getSQLState()).isEqualTo(expectedSqlState)
            );
    }

    private static Stream<Arguments> mandatoryNullParameters() {
        return Stream.of(
            Arguments.of("null suspense item ID", null, POSTED_DATE, "UN", BigDecimal.ONE),
            Arguments.of("null posted date", SUSPENSE_ITEM_ID, null, "UN", BigDecimal.ONE),
            Arguments.of("null transaction type", SUSPENSE_ITEM_ID, POSTED_DATE, null, BigDecimal.ONE),
            Arguments.of("null amount", SUSPENSE_ITEM_ID, POSTED_DATE, "UN", null)
        );
    }
}
