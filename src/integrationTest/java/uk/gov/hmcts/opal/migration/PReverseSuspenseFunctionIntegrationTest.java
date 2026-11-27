package uk.gov.hmcts.opal.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("p_reverse_suspense_function integration tests")
@Sql(
    scripts = "classpath:db/insertData/insert_p_reverse_suspense_function.sql",
    executionPhase = BEFORE_TEST_CLASS
)
@Sql(
    scripts = "classpath:db/deleteData/delete_p_reverse_suspense_function.sql",
    executionPhase = AFTER_TEST_CLASS
)
@Transactional
class PReverseSuspenseFunctionIntegrationTest extends AbstractIntegrationTest {

    private static final long BUSINESS_UNIT_ID = 3977L;
    private static final long STANDARD_ITEM_ID = 99000001035001L;
    private static final long DISHONOURED_ITEM_ID = 99000001035002L;
    private static final long EMPTY_ITEM_ID = 99000001035003L;
    private static final long ZERO_SUM_ITEM_ID = 99000001035004L;
    private static final long NEGATIVE_ITEM_ID = 99000001035005L;
    private static final long STALE_VERSION_ITEM_ID = 99000001035006L;
    private static final long REPORT_FAILURE_ITEM_ID = 99000001035007L;
    private static final long CONCURRENT_ITEM_ID = 99000001035008L;
    private static final long INITIAL_VERSION = 1L;
    private static final String POSTED_BY = "PO10350_TEST";
    private static final String POSTED_BY_NAME = "PO-10350 Integration Test User";
    private static final String REPORT_ID = "reversed_payments_report";
    private static final String PROCEDURE_CALL = """
        CALL public.p_reverse_suspense_function(
            CAST(? AS BIGINT),
            CAST(? AS BIGINT),
            CAST(? AS VARCHAR),
            CAST(? AS VARCHAR),
            CAST(? AS BOOLEAN)
        )
        """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Reverses the balance of multiple transactions and creates the RV transaction and report entry")
    void reversesMultipleTransactionsAndCreatesRvReportEntry() {
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        callProcedure(STANDARD_ITEM_ID, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false);

        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        assertSuccessfulReversal(STANDARD_ITEM_ID, "RV", "-70.00", beforeCall, afterCall);
    }

    @Test
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Creates the DQ transaction and report entry for a dishonoured cheque")
    void createsDqTransactionAndReportEntryForDishonouredCheque() {
        LocalDateTime beforeCall = LocalDateTime.now().minusSeconds(1);

        callProcedure(DISHONOURED_ITEM_ID, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, true);

        LocalDateTime afterCall = LocalDateTime.now().plusSeconds(1);
        assertSuccessfulReversal(DISHONOURED_ITEM_ID, "DQ", "-75.00", beforeCall, afterCall);
    }

    @ParameterizedTest(name = "Rejects {0} balance without changing data")
    @MethodSource("nonPositiveBalanceItems")
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Rejects an empty or zero-total suspense item")
    void rejectsEmptyOrZeroTotalBalance(String scenario, long suspenseItemId) {
        ItemSnapshot before = snapshot(suspenseItemId);

        SQLException exception = expectSqlState(
            "P4006",
            () -> callProcedure(suspenseItemId, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false)
        );

        assertThat(exception.getMessage()).contains("must have a positive balance");
        assertUnchanged(suspenseItemId, before);
    }

    @Test
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Rejects a negative balance without changing transactions, reports or version")
    void rejectsNegativeBalanceWithoutChangingData() {
        ItemSnapshot before = snapshot(NEGATIVE_ITEM_ID);

        SQLException exception = expectSqlState(
            "P4006",
            () -> callProcedure(NEGATIVE_ITEM_ID, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false)
        );

        assertThat(exception.getMessage()).contains("must have a positive balance");
        assertUnchanged(NEGATIVE_ITEM_ID, before);
    }

    @Test
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Rejects a stale version and rolls back the reversal transaction and report entry")
    void rejectsStaleVersionAndRollsBackReversalAndReportEntry() {
        ItemSnapshot before = snapshot(STALE_VERSION_ITEM_ID);

        SQLException exception = expectSqlState(
            "P0001",
            () -> callProcedure(STALE_VERSION_ITEM_ID, 999L, POSTED_BY, POSTED_BY_NAME, false)
        );

        assertThat(exception.getMessage()).contains("Some information on this page may be out of date.");
        assertUnchanged(STALE_VERSION_ITEM_ID, before);
    }

    @ParameterizedTest(name = "Rejects null {0} with {1}")
    @MethodSource("nullMandatoryParameters")
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Rejects NULL for every mandatory parameter without changing data")
    void rejectsNullMandatoryParameter(
        String parameter,
        String expectedSqlState,
        Long suspenseItemId,
        Long version,
        String postedBy,
        String postedByName,
        Boolean dishonoured
    ) {
        ItemSnapshot before = snapshot(STANDARD_ITEM_ID);

        expectSqlState(
            expectedSqlState,
            () -> callProcedure(suspenseItemId, version, postedBy, postedByName, dishonoured)
        );

        assertUnchanged(STANDARD_ITEM_ID, before);
    }

    @Test
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Propagates a report insert failure and rolls back the preceding reversal transaction")
    void rollsBackReversalWhenReportInsertFails() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION public.po_10350_fail_report_entry_insert()
            RETURNS trigger
            LANGUAGE plpgsql
            AS $$
            BEGIN
                RAISE EXCEPTION 'forced PO-10350 report entry failure'
                    USING ERRCODE = '23514';
            END;
            $$
            """);
        jdbcTemplate.execute("""
            CREATE TRIGGER po_10350_fail_report_entry_insert_trigger
            BEFORE INSERT ON public.report_entries
            FOR EACH ROW
            WHEN (NEW.report_id = 'reversed_payments_report')
            EXECUTE FUNCTION public.po_10350_fail_report_entry_insert()
            """);

        ItemSnapshot before = snapshot(REPORT_FAILURE_ITEM_ID);
        SQLException exception = expectSqlState(
            "23514",
            () -> callProcedure(REPORT_FAILURE_ITEM_ID, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false)
        );

        assertThat(exception.getMessage()).contains("forced PO-10350 report entry failure");
        assertUnchanged(REPORT_FAILURE_ITEM_ID, before);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @JiraStory("PO-10350")
    @JiraEpic("PO-9048")
    @DisplayName("Serializes concurrent reversals and rejects the second call after the first commits")
    void concurrentCallsOnlyCommitOneReversal() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<SQLException> secondCall = null;

        try (Connection connectionA = dataSource.getConnection();
             Connection connectionB = dataSource.getConnection()) {
            connectionA.setAutoCommit(false);
            connectionB.setAutoCommit(false);

            executeProcedure(connectionA, CONCURRENT_ITEM_ID, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false);
            long connectionBPid = backendPid(connectionB);

            Connection finalConnectionB = connectionB;
            secondCall = executor.submit(() -> {
                try {
                    executeProcedure(
                        finalConnectionB,
                        CONCURRENT_ITEM_ID,
                        INITIAL_VERSION,
                        POSTED_BY,
                        POSTED_BY_NAME,
                        false
                    );
                    return null;
                } catch (SQLException exception) {
                    return exception;
                }
            });

            awaitLockWait(connectionBPid);
            connectionA.commit();

            SQLException exception = secondCall.get(10, TimeUnit.SECONDS);
            assertNotNull(exception);
            assertThat(exception.getSQLState()).isEqualTo("P0001");
            assertThat(exception.getMessage()).contains("Some information on this page may be out of date.");
            connectionB.rollback();

            assertThat(balance(CONCURRENT_ITEM_ID)).isEqualByComparingTo("0.00");
            assertThat(version(CONCURRENT_ITEM_ID)).isEqualTo(2L);
            assertThat(transactionCount(CONCURRENT_ITEM_ID, "RV")).isEqualTo(1);
            assertThat(reportEntryCount()).isEqualTo(1);
            assertThat(reportEntriesForTransaction(reversalTransactionId(CONCURRENT_ITEM_ID, "RV"))).hasSize(1);
        } finally {
            if (secondCall != null && !secondCall.isDone()) {
                secondCall.cancel(true);
            }
            executor.shutdownNow();
        }
    }

    private void assertSuccessfulReversal(
        long suspenseItemId,
        String transactionType,
        String expectedAmount,
        LocalDateTime beforeCall,
        LocalDateTime afterCall
    ) {
        assertThat(balance(suspenseItemId)).isEqualByComparingTo("0.00");
        assertThat(version(suspenseItemId)).isEqualTo(2L);
        assertThat(transactionCount(suspenseItemId, transactionType)).isEqualTo(1);

        Map<String, Object> transaction = jdbcTemplate.queryForMap("""
            SELECT suspense_transaction_id,
                   suspense_item_id,
                   posted_date,
                   posted_by,
                   posted_by_name,
                   transaction_type,
                   amount,
                   associated_record_type,
                   associated_record_id,
                   text,
                   reversed
            FROM public.suspense_transactions
            WHERE suspense_item_id = ?
              AND transaction_type = CAST(? AS public.t_suspense_transaction_type_enum)
            """, suspenseItemId, transactionType);

        long transactionId = ((Number) transaction.get("suspense_transaction_id")).longValue();
        Timestamp postedDate = (Timestamp) transaction.get("posted_date");
        assertThat(transaction.get("suspense_item_id")).isEqualTo(suspenseItemId);
        assertThat(transaction.get("posted_by")).isEqualTo(POSTED_BY);
        assertThat(transaction.get("posted_by_name")).isEqualTo(POSTED_BY_NAME);
        assertThat(transaction.get("transaction_type")).hasToString(transactionType);
        assertThat((BigDecimal) transaction.get("amount")).isEqualByComparingTo(expectedAmount);
        assertThat(postedDate).isNotNull();
        assertThat(postedDate.toLocalDateTime()).isBetween(beforeCall, afterCall);
        assertThat(transaction.get("associated_record_type")).isNull();
        assertThat(transaction.get("associated_record_id")).isNull();
        assertThat(transaction.get("text")).isNull();
        assertThat(transaction.get("reversed")).isNull();

        List<Map<String, Object>> entries = reportEntriesForTransaction(transactionId);
        assertThat(entries).hasSize(1);
        Map<String, Object> reportEntry = entries.getFirst();
        assertThat(((Number) reportEntry.get("business_unit_id")).longValue()).isEqualTo(BUSINESS_UNIT_ID);
        assertThat(reportEntry.get("report_id")).isEqualTo(REPORT_ID);
        assertThat(reportEntry.get("associated_record_type")).hasToString("suspense_transactions");
        assertThat(reportEntry.get("associated_record_id")).isEqualTo(Long.toString(transactionId));
        Timestamp entryTimestamp = (Timestamp) reportEntry.get("entry_timestamp");
        assertThat(entryTimestamp).isNotNull();
        assertThat(entryTimestamp.toLocalDateTime()).isBetween(beforeCall, afterCall);
        assertThat(reportEntry.get("reported_timestamp")).isNull();
        assertThat(reportEntry.get("report_instance_id")).isNull();
    }

    private SQLException expectSqlState(String expectedSqlState, Runnable procedureCall) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        Savepoint savepoint;
        RuntimeException callFailure = null;
        try {
            savepoint = connection.setSavepoint();
            try {
                procedureCall.run();
            } catch (RuntimeException exception) {
                callFailure = exception;
            }
            connection.rollback(savepoint);
            connection.releaseSavepoint(savepoint);
        } catch (SQLException exception) {
            throw new IllegalStateException(
                "Unable to recover the test transaction after procedure failure",
                exception
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

        SQLException sqlException = findSqlException(callFailure);
        assertNotNull(sqlException, "Expected the procedure to fail with SQLSTATE " + expectedSqlState);
        assertThat(sqlException.getSQLState()).isEqualTo(expectedSqlState);
        return sqlException;
    }

    private SQLException findSqlException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }

    private ItemSnapshot snapshot(long suspenseItemId) {
        return new ItemSnapshot(
            balance(suspenseItemId),
            transactionCount(suspenseItemId, null),
            version(suspenseItemId),
            reportEntryCount()
        );
    }

    private void assertUnchanged(long suspenseItemId, ItemSnapshot before) {
        ItemSnapshot after = snapshot(suspenseItemId);
        assertThat(after.balance()).isEqualByComparingTo(before.balance());
        assertThat(after.transactionCount()).isEqualTo(before.transactionCount());
        assertThat(after.version()).isEqualTo(before.version());
        assertThat(after.reportEntryCount()).isEqualTo(before.reportEntryCount());
    }

    private BigDecimal balance(long suspenseItemId) {
        return jdbcTemplate.queryForObject("""
            SELECT COALESCE(SUM(amount), 0)
            FROM public.suspense_transactions
            WHERE suspense_item_id = ?
            """, BigDecimal.class, suspenseItemId);
    }

    private long version(long suspenseItemId) {
        return jdbcTemplate.queryForObject(
            "SELECT version_number FROM public.suspense_items WHERE suspense_item_id = ?",
            Long.class,
            suspenseItemId
        );
    }

    private int transactionCount(long suspenseItemId, String transactionType) {
        if (transactionType == null) {
            return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.suspense_transactions WHERE suspense_item_id = ?",
                Integer.class,
                suspenseItemId
            );
        }
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM public.suspense_transactions
            WHERE suspense_item_id = ?
              AND transaction_type = CAST(? AS public.t_suspense_transaction_type_enum)
            """, Integer.class, suspenseItemId, transactionType);
    }

    private int reportEntryCount() {
        return jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM public.report_entries
            WHERE business_unit_id = ?
              AND report_id = ?
            """, Integer.class, BUSINESS_UNIT_ID, REPORT_ID);
    }

    private List<Map<String, Object>> reportEntriesForTransaction(long transactionId) {
        return jdbcTemplate.queryForList("""
            SELECT report_entry_id,
                   business_unit_id,
                   report_id,
                   entry_timestamp,
                   reported_timestamp,
                   associated_record_type,
                   associated_record_id,
                   report_instance_id
            FROM public.report_entries
            WHERE business_unit_id = ?
              AND report_id = ?
              AND associated_record_type = 'suspense_transactions'
              AND associated_record_id = ?
            """, BUSINESS_UNIT_ID, REPORT_ID, Long.toString(transactionId));
    }

    private long reversalTransactionId(long suspenseItemId, String transactionType) {
        return jdbcTemplate.queryForObject("""
            SELECT suspense_transaction_id
            FROM public.suspense_transactions
            WHERE suspense_item_id = ?
              AND transaction_type = CAST(? AS public.t_suspense_transaction_type_enum)
            """, Long.class, suspenseItemId, transactionType);
    }

    private void callProcedure(
        Long suspenseItemId,
        Long version,
        String postedBy,
        String postedByName,
        Boolean dishonoured
    ) {
        jdbcTemplate.update(PROCEDURE_CALL, suspenseItemId, version, postedBy, postedByName, dishonoured);
    }

    private void executeProcedure(
        Connection connection,
        long suspenseItemId,
        long version,
        String postedBy,
        String postedByName,
        boolean dishonoured
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(PROCEDURE_CALL)) {
            statement.setLong(1, suspenseItemId);
            statement.setLong(2, version);
            statement.setString(3, postedBy);
            statement.setString(4, postedByName);
            statement.setBoolean(5, dishonoured);
            statement.execute();
        }
    }

    private long backendPid(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_backend_pid()");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private void awaitLockWait(long backendPid) throws SQLException, InterruptedException {
        long timeoutAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        try (Connection monitorConnection = dataSource.getConnection();
             PreparedStatement statement = monitorConnection.prepareStatement("""
                 SELECT wait_event_type
                 FROM pg_stat_activity
                 WHERE pid = ?
                 """)) {
            while (System.nanoTime() < timeoutAt) {
                statement.setLong(1, backendPid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && "Lock".equals(resultSet.getString("wait_event_type"))) {
                        return;
                    }
                }
                Thread.sleep(25L);
            }
        }
        throw new AssertionError("Second PostgreSQL session did not wait on the suspense item row lock");
    }

    private static java.util.stream.Stream<Arguments> nonPositiveBalanceItems() {
        return java.util.stream.Stream.of(
            Arguments.of("an item with no transactions", EMPTY_ITEM_ID),
            Arguments.of("transactions totalling zero", ZERO_SUM_ITEM_ID)
        );
    }

    private static java.util.stream.Stream<Arguments> nullMandatoryParameters() {
        return java.util.stream.Stream.of(
            Arguments.of("suspense item ID", "P4001", null, INITIAL_VERSION, POSTED_BY, POSTED_BY_NAME, false),
            Arguments.of("suspense item version", "P4002", STANDARD_ITEM_ID, null, POSTED_BY, POSTED_BY_NAME, false),
            Arguments.of("posted by", "P4003", STANDARD_ITEM_ID, INITIAL_VERSION, null, POSTED_BY_NAME, false),
            Arguments.of("posted by name", "P4004", STANDARD_ITEM_ID, INITIAL_VERSION, POSTED_BY, null, false),
            Arguments.of(
                "dishonoured cheque flag",
                "P4005",
                STANDARD_ITEM_ID,
                INITIAL_VERSION,
                POSTED_BY,
                POSTED_BY_NAME,
                null
            )
        );
    }

    private record ItemSnapshot(BigDecimal balance, int transactionCount, long version, int reportEntryCount) {
    }
}
