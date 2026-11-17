package uk.gov.hmcts.opal.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@DisplayName("PO-10347 creditor transactions common procedure integration tests")
@Sql(scripts = "classpath:db/insertData/insert_p_insert_creditor_transactions_common.sql",
    executionPhase = BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/deleteData/delete_p_insert_creditor_transactions_common.sql",
    executionPhase = AFTER_TEST_CLASS)
class CreditorTransactionsCommonProcedureIntegrationTest extends AbstractIntegrationTest {

    private static final long CREDITOR_ACCOUNT_ID = 9_999_010_347_001L;
    private static final long UNKNOWN_CREDITOR_ACCOUNT_ID = 9_999_010_347_999L;

    @Test
    @JiraStory("PO-10347")
    @JiraEpic("PO-9048")
    @DisplayName("Inserts and returns a creditor transaction with all supplied values")
    void insertsAllSuppliedValues() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 11, 13, 9, 45, 30);
        LocalDateTime statusDate = LocalDateTime.of(2026, 11, 14, 10, 15, 45);

        Long transactionId = callProcedure(
            CREDITOR_ACCOUNT_ID,
            postedDate,
            "PO10347-FULL",
            "QA Integration User",
            "PAYMNT",
            new BigDecimal("123.45"),
            "QA10347",
            true,
            "QA10347",
            "C",
            statusDate,
            "creditor_accounts",
            Long.toString(CREDITOR_ACCOUNT_ID)
        );

        CreditorTransaction transaction = findTransaction(transactionId);

        assertThat(transaction.creditorAccountId()).isEqualTo(CREDITOR_ACCOUNT_ID);
        assertThat(transaction.postedDate()).isEqualTo(postedDate);
        assertThat(transaction.postedBy()).isEqualTo("PO10347-FULL");
        assertThat(transaction.postedByName()).isEqualTo("QA Integration User");
        assertThat(transaction.transactionType()).isEqualTo("PAYMNT");
        assertThat(transaction.transactionAmount()).isEqualByComparingTo("123.45");
        assertThat(transaction.impositionResultId()).isEqualTo("QA10347");
        assertThat(transaction.paymentProcessed()).isTrue();
        assertThat(transaction.paymentReference()).isEqualTo("QA10347");
        assertThat(transaction.status()).isEqualTo("C");
        assertThat(transaction.statusDate()).isEqualTo(statusDate);
        assertThat(transaction.associatedRecordType()).isEqualTo("creditor_accounts");
        assertThat(transaction.associatedRecordId()).isEqualTo(Long.toString(CREDITOR_ACCOUNT_ID));
    }

    @Test
    @JiraStory("PO-10347")
    @JiraEpic("PO-9048")
    @DisplayName("Inserts a creditor transaction when all optional values are null")
    void insertsNullOptionalValues() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 11, 13, 11, 30);

        Long transactionId = callProcedure(
            CREDITOR_ACCOUNT_ID,
            postedDate,
            null,
            null,
            "MADJ",
            new BigDecimal("0.01"),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        CreditorTransaction transaction = findTransaction(transactionId);

        assertThat(transaction.creditorAccountId()).isEqualTo(CREDITOR_ACCOUNT_ID);
        assertThat(transaction.postedDate()).isEqualTo(postedDate);
        assertThat(transaction.transactionType()).isEqualTo("MADJ");
        assertThat(transaction.transactionAmount()).isEqualByComparingTo("0.01");
        assertThat(transaction.postedBy()).isNull();
        assertThat(transaction.postedByName()).isNull();
        assertThat(transaction.impositionResultId()).isNull();
        assertThat(transaction.paymentProcessed()).isNull();
        assertThat(transaction.paymentReference()).isNull();
        assertThat(transaction.status()).isNull();
        assertThat(transaction.statusDate()).isNull();
        assertThat(transaction.associatedRecordType()).isNull();
        assertThat(transaction.associatedRecordId()).isNull();
    }

    @Test
    @JiraStory("PO-10347")
    @JiraEpic("PO-9048")
    @DisplayName("Propagates a foreign-key violation without inserting a creditor transaction")
    void rejectsUnknownCreditorAccountWithoutPartialInsert() {
        String postedBy = "PO10347-BAD-FK";

        assertThatThrownBy(() -> callProcedure(
            UNKNOWN_CREDITOR_ACCOUNT_ID,
            LocalDateTime.of(2026, 11, 13, 12, 0),
            postedBy,
            "QA Integration User",
            "PAYMNT",
            new BigDecimal("50.00"),
            null,
            false,
            null,
            null,
            null,
            null,
            null
        ))
            .isInstanceOf(DataAccessException.class)
            .satisfies(exception -> assertThat(sqlState(exception)).isEqualTo("23503"));

        Integer insertedRows = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM public.creditor_transactions WHERE posted_by = ?",
            Integer.class,
            postedBy
        );
        assertThat(insertedRows).isZero();
    }

    private Long callProcedure(
        long creditorAccountId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String transactionType,
        BigDecimal transactionAmount,
        String impositionResultId,
        Boolean paymentProcessed,
        String paymentReference,
        String status,
        LocalDateTime statusDate,
        String associatedRecordType,
        String associatedRecordId
    ) {
        return jdbcTemplate.queryForObject(
            """
                CALL public.p_insert_creditor_transactions_common(
                    ?, ?, ?, ?, CAST(? AS public.t_creditor_transaction_type_enum),
                    ?, ?, ?, ?, CAST(? AS public.t_creditor_transaction_status_enum),
                    ?, CAST(? AS public.t_associated_record_type_enum), ?, NULL
                )
                """,
            Long.class,
            creditorAccountId,
            postedDate,
            postedBy,
            postedByName,
            transactionType,
            transactionAmount,
            impositionResultId,
            paymentProcessed,
            paymentReference,
            status,
            statusDate,
            associatedRecordType,
            associatedRecordId
        );
    }

    private CreditorTransaction findTransaction(long transactionId) {
        return jdbcTemplate.queryForObject(
            """
                SELECT creditor_account_id, posted_date, posted_by, posted_by_name, transaction_type,
                       transaction_amount, imposition_result_id, payment_processed, payment_reference,
                       status, status_date, associated_record_type, associated_record_id
                  FROM public.creditor_transactions
                 WHERE creditor_transaction_id = ?
                """,
            (resultSet, rowNumber) -> new CreditorTransaction(
                resultSet.getLong("creditor_account_id"),
                resultSet.getObject("posted_date", LocalDateTime.class),
                resultSet.getString("posted_by"),
                resultSet.getString("posted_by_name"),
                resultSet.getString("transaction_type"),
                resultSet.getBigDecimal("transaction_amount"),
                resultSet.getString("imposition_result_id"),
                resultSet.getObject("payment_processed", Boolean.class),
                resultSet.getString("payment_reference"),
                resultSet.getString("status"),
                resultSet.getObject("status_date", LocalDateTime.class),
                resultSet.getString("associated_record_type"),
                resultSet.getString("associated_record_id")
            ),
            transactionId
        );
    }

    private String sqlState(Throwable exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof SQLException sqlException) {
                return sqlException.getSQLState();
            }
            cause = cause.getCause();
        }
        return null;
    }

    private record CreditorTransaction(
        long creditorAccountId,
        LocalDateTime postedDate,
        String postedBy,
        String postedByName,
        String transactionType,
        BigDecimal transactionAmount,
        String impositionResultId,
        Boolean paymentProcessed,
        String paymentReference,
        String status,
        LocalDateTime statusDate,
        String associatedRecordType,
        String associatedRecordId
    ) {
    }
}
