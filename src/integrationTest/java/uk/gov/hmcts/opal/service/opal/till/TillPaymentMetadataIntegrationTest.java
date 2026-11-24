package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.AbstractIntegrationTest;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.jpa.NoteSpecs;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;

@Sql("classpath:db/insertData/insert_into_till_payment_metadata.sql")
@Transactional
@Tag("ExtendedTest")
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SqlNoDataSourceInspection", "SqlResolve"})
class TillPaymentMetadataIntegrationTest extends AbstractIntegrationTest {

    @Autowired private PaymentInRepository paymentInRepository;
    @Autowired private MiscellaneousAccountRepository miscellaneousAccountRepository;
    @Autowired private NoteRepository noteRepository;

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"113651001,OF", "113651002,FA", "113651003,MS", "113651004,UN"})
    void retainsSuspenseMetadataAndNote(long tillId, String reason) {
        allocate(tillId);
        List<Transaction> transactions = transactions();
        assertThat(transactions).hasSize(1);
        Transaction transaction = transactions.getFirst();
        assertThat(transaction.itemType()).isEqualTo(reason);
        JsonNode metadata = metadata(transaction);
        assertThat(metadata.path("payment_received_from").asString()).isEqualTo("D");
        assertThat(metadata.path("suspense_reason_code").asString()).isEqualTo(reason);
        if (reason.equals("UN")) {
            assertThat(metadata.path("suspense_reason_detail").asString()).isEqualTo("Unidentified receipt");
            assertThat(metadata.path("payment_reference").asString()).isEmpty();
            assertThat(metadata.has("transfer_source")).isFalse();
            assertThat(transaction.associatedType()).isNull();
            assertThat(transaction.associatedId()).isEqualTo(Long.toString(tillId));
        } else {
            assertPayerAssociation(transaction);
            assertThat(metadata.path("payment_reference").asString()).isEqualTo("DB-REFERENCE");
            assertThat(metadata.path("transfer_source").asString()).isEqualTo("NATWEST");
        }
        deletePayments(tillId);
        assertThat(transactions()).containsExactlyElementsOf(transactions);
        assertNote(transaction.itemId());
    }

    @ParameterizedTest
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    @CsvSource({"113651005,2", "113651006,1"})
    void allocatesSplitAndExistingCourtFeePayments(long tillId, int paymentCount) {
        assertThat(paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(tillId))
            .hasSize(paymentCount);
        allocate(tillId);
        List<Transaction> transactions = transactions();
        assertThat(transactions).extracting(Transaction::type).containsExactly("CF", "OC");
        assertThat(transactions.get(0).amount()).isEqualByComparingTo("10.00");
        assertThat(transactions.get(1).amount()).isEqualByComparingTo("2.50");
        assertThat(transactions.get(0).itemId()).isEqualTo(transactions.get(1).itemId());
        assertThat(transactions).allSatisfy(transaction -> {
            assertPayerAssociation(transaction);
            assertThat(metadata(transaction).path("payment_received_from").asString()).isEqualTo("A");
            assertThat(metadata(transaction).path("payment_reference").asString()).isEqualTo("DB-REFERENCE");
            assertThat(metadata(transaction).path("transfer_source").asString()).isEqualTo("NATWEST");
        });
        deletePayments(tillId);
        assertThat(transactions()).containsExactlyElementsOf(transactions);
        assertThat(miscellaneousAccountRepository.findById(113651000L).orElseThrow().getParty().getSurname())
            .isEqualTo("Durable payer");
        assertNote(transactions.getFirst().itemId());
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void retainsCriminalThirdPartyName() {
        allocate(113651007L);
        List<Transaction> transactions = transactions();
        assertThat(transactions).hasSize(1);
        Transaction transaction = transactions.getFirst();
        assertThat(transaction.associatedType()).isEqualTo("defendant_accounts");
        assertThat(transaction.associatedId()).isEqualTo("113651000");
        assertThat(metadata(transaction).path("third_party_payer_name").asString()).isEqualTo("Criminal third party");
        deletePayments(113651007L);
        assertThat(transactions()).containsExactlyElementsOf(transactions);
    }

    @Test
    @JiraStory("PO-11365")
    @JiraEpic("PO-2439")
    void retainsAutomaticPaymentReferenceWithoutChangingAssociation() {
        allocate(113651008L);
        List<Transaction> transactions = transactions();
        assertThat(transactions).hasSize(1);
        Transaction transaction = transactions.getFirst();
        assertThat(transaction.associatedType()).isNull();
        assertThat(transaction.associatedId()).isEqualTo("113651009");
        assertThat(metadata(transaction).path("payment_reference").asString()).isEqualTo("AUTO-REFERENCE");
        assertThat(metadata(transaction).path("transfer_source").asString()).isEqualTo("ALLPAY");
        deletePayments(113651008L);
        assertThat(transactions()).containsExactlyElementsOf(transactions);
    }

    private void allocate(long tillId) {
        jdbcTemplate.update("CALL p_allocate_till(?, ?, ?, ?::smallint)", tillId, "DB11365", "DB test", 9365);
    }

    private void deletePayments(long tillId) {
        paymentInRepository.deleteAllInBatch(
            paymentInRepository.findByTillEntity_TillIdOrderByPaymentDateAscPaymentInIdAsc(tillId));
        paymentInRepository.flush();
    }

    private static void assertPayerAssociation(Transaction transaction) {
        assertThat(transaction.associatedType()).isEqualTo("miscellaneous_accounts");
        assertThat(transaction.associatedId()).isEqualTo("113651000");
    }

    private JsonNode metadata(Transaction transaction) {
        assertThat(transaction.legacyText()).isNull();
        assertThat(transaction.information()).doesNotContain("payer_type", "payer_details", "Transient payer",
            "Transient address", "court_fee_overpayment");
        return objectMapper.readTree(transaction.information());
    }

    private void assertNote(long itemId) {
        List<NoteEntity> notes = noteRepository.findAll(
            NoteSpecs.equalsAssociatedRecordType(AssociatedRecordType.SUSPENSE_ITEMS)
                .and(NoteSpecs.equalsAssociatedRecordId(Long.toString(itemId))));
        assertThat(notes).extracting(NoteEntity::getNoteText).containsExactly("DB payment note");
    }

    private List<Transaction> transactions() {
        return jdbcTemplate.query("""
            SELECT st.additional_information::text, st.text, st.amount, st.transaction_type::text,
                   st.associated_record_type::text, st.associated_record_id, st.suspense_item_id,
                   si.suspense_item_type::text
              FROM suspense_transactions st
              JOIN suspense_items si ON si.suspense_item_id = st.suspense_item_id
             WHERE si.suspense_account_id = 113651000
             ORDER BY st.suspense_transaction_id
            """, (row, index) -> new Transaction(row.getString(1), row.getString(2), row.getBigDecimal(3),
                row.getString(4), row.getString(5), row.getString(6), row.getLong(7), row.getString(8)));
    }

    private record Transaction(String information, String legacyText, BigDecimal amount, String type,
                               String associatedType, String associatedId, long itemId, String itemType) {
    }
}
