package uk.gov.hmcts.opal.service.opal.history.defendant;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionStatus;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionType;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionWriteOffCode;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

class DefendantTransactionDetailsServiceTest {

    private static final LocalDateTime POSTED_DATE = LocalDateTime.of(2024, 1, 2, 10, 15);
    private static final LocalDateTime IMPOSED_DATE = LocalDateTime.of(2024, 1, 1, 9, 30);
    private static final LocalDateTime STATUS_DATE = LocalDateTime.of(2024, 1, 3, 11, 45);
    private static final String ACCOUNT_NUMBER = "ACC-123";
    private static final String ORIGINATOR_NAME = "Originator Court";

    private DefendantTransactionDetailsService service;

    @BeforeEach
    void setUp() {
        service = new DefendantTransactionDetailsService();
    }

    @Test
    void generateTransactionDetails_canchq_returnsChequeCancelledDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.CANCHQ);
        transaction.setPaymentReference("CHQ-001");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque cancelled | Cheque number: CHQ-001");
    }

    @Test
    void generateTransactionDetails_cheque_withDishonouredStatus_returnsStatusSuffix() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.CHEQUE);
        transaction.setPaymentReference("CHQ-002");
        transaction.setStatus(DefendantTransactionStatus.D);
        transaction.setStatusDate(STATUS_DATE);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque issued | Cheque number: CHQ-002 | Dishonoured " + STATUS_DATE);
    }

    @Test
    void generateTransactionDetails_cheque_withCancelledStatusAndNoReference_returnsNotYetWritten() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.CHEQUE);
        transaction.setStatus(DefendantTransactionStatus.X);
        transaction.setStatusDate(STATUS_DATE);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque issued | Cheque number: Not yet written | Cancelled " + STATUS_DATE);
    }

    @Test
    void generateTransactionDetails_cheque_withClearStatus_returnsNoStatusSuffix() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.CHEQUE);
        transaction.setPaymentReference("CHQ-003");
        transaction.setStatus(DefendantTransactionStatus.C);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque issued | Cheque number: CHQ-003");
    }

    @Test
    void generateTransactionDetails_consol_returnsAccountConsolidatedDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.CONSOL);
        transaction.setDefendantAccountId(77L);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Account consolidated | 77 | Amount credited to master account");
    }

    @Test
    void generateTransactionDetails_dishcq_returnsImpositionDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.DISHCQ);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque dishonoured | FCOMP 50.00 Created: 2024-01-02T10:15 | 9001");
    }

    @Test
    void generateTransactionDetails_frSus_returnsAssociatedRecordId() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.FR_SUS);
        transaction.setAssociatedRecordId("SUS-1007");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Transfer from suspense | SUS-1007");
    }

    @Test
    void generateTransactionDetails_madj_returnsAssociatedRecordId() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.MADJ);
        transaction.setAssociatedRecordId("MADJ-1008");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Manual adjustment | MADJ-1008");
    }

    @Test
    void generateTransactionDetails_paymnt_withTextAndReference_returnsAllParts() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.PAYMNT);
        transaction.setPaymentMethod(PaymentMethod.CT);
        transaction.setText("Payment by credit transfer");
        transaction.setPaymentReference("PR1009");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Payment received | Credit Transfer | Payment by credit transfer | PR1009");
    }

    @Test
    void generateTransactionDetails_paymnt_withBlankTextAndNoReference_omitsOptionalParts() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.PAYMNT);
        transaction.setPaymentMethod(PaymentMethod.CQ);
        transaction.setText(" ");
        transaction.setPaymentReference(null);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Payment received | Cheque");
    }

    @Test
    void generateTransactionDetails_repsus_returnsAssociatedRecordId() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.REPSUS);
        transaction.setAssociatedRecordId("REP-1010");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Repayment from suspense | REP-1010");
    }

    @Test
    void generateTransactionDetails_revpay_returnsImpositionDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.REVPAY);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Payment reversed | FCOMP 50.00 Created: 2024-01-02T10:15 | 9001");
    }

    @Test
    void generateTransactionDetails_richeq_returnsChequeReissuedDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.RICHEQ);
        transaction.setPaymentReference("CHQ-004");
        transaction.setStatus(DefendantTransactionStatus.C);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Cheque reissued | Cheque number: CHQ-004");
    }

    @Test
    void generateTransactionDetails_rvwoff_returnsWriteOffReversedText() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.RVWOFF);
        transaction.setText("Reinstated after review");

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Write-off reversed Reinstated after review");
    }

    @Test
    void generateTransactionDetails_tfo_returnsTransferredToText() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.TFO);
        transaction.setText("Central Office");

        assertThat(service.generateTransactionDetails(transaction, account(ORIGINATOR_NAME), imposition()))
            .isEqualTo("TFO out | Transferred to: Central Office " + ORIGINATOR_NAME);
    }

    @Test
    void generateTransactionDetails_tfoIn_returnsReceivedFromText() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.TFO_IN);

        assertThat(service.generateTransactionDetails(transaction, account(ORIGINATOR_NAME), imposition()))
            .isEqualTo("TFO in | Received from: " + ORIGINATOR_NAME);
    }

    @Test
    void generateTransactionDetails_wrtoff_withImpositions_returnsFullWriteOffDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.WRTOFF);
        transaction.setText("Written off after judgment");
        transaction.setAssociatedRecordType(AssociatedRecordType.IMPOSITIONS);
        transaction.setWriteOffCode(DefendantTransactionWriteOffCode.JCAM_A);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Write-off | 2024-01-01T09:30 | FCOMP 50.00 Created: 2024-01-02T10:15 | 9001"
                + " | Unknown whereabouts | Written off after judgment");
    }

    @Test
    void generateTransactionDetails_wrtoff_withDefendantAccounts_returnsAccountDetails() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.WRTOFF);
        transaction.setAssociatedRecordType(AssociatedRecordType.DEFENDANT_ACCOUNTS);
        transaction.setDefendantAccountId(77L);
        transaction.setWriteOffCode(DefendantTransactionWriteOffCode.TRNOUT);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Write-off | Transferred out - 77");
    }

    @Test
    void generateTransactionDetails_wrtoff_withOtherRecordType_returnsDefaultBranch() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.WRTOFF);
        transaction.setAssociatedRecordType(AssociatedRecordType.ENFORCEMENTS);
        transaction.setWriteOffCode(DefendantTransactionWriteOffCode.TRNOUT);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Write-off | ");
    }

    @Test
    void generateTransactionDetails_transferToSuspense_forSuspenseTransaction_returnsSuspenseReason() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.XFER);
        transaction.setAssociatedRecordType(AssociatedRecordType.SUSPENSE_TRANSACTIONS);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Suspense transfer | Cheque cancelled to suspense");
    }

    @Test
    void generateTransactionDetails_transferToSuspense_fromCreditorTransactions_returnsCentralFundReason() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.XFER);
        transaction.setAssociatedRecordType(AssociatedRecordType.CREDITOR_TRANSACTIONS);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Suspense transfer | Cheque cancelled to Central Fund");
    }

    @Test
    void generateTransactionDetails_transferToSuspense_withOtherRecordType_returnsEmptyReason() {
        DefendantTransactionEntity transaction = transaction(DefendantTransactionType.XFER);
        transaction.setAssociatedRecordType(AssociatedRecordType.ENFORCEMENTS);

        assertThat(service.generateTransactionDetails(transaction, account(null), imposition()))
            .isEqualTo("Suspense transfer | ");
    }

    private static DefendantTransactionEntity transaction(DefendantTransactionType type) {
        return DefendantTransactionEntity.builder()
            .postedDate(POSTED_DATE.toLocalDate())
            .postedByUsername("test.user")
            .transactionType(type)
            .transactionAmount(new BigDecimal("50.00"))
            .build();
    }

    private static DefendantAccountEntity account(String originatorName) {
        return DefendantAccountEntity.builder()
            .accountNumber(ACCOUNT_NUMBER)
            .originatorName(originatorName)
            .build();
    }

    private static ImpositionEntity imposition() {
        return ImpositionEntity.builder()
            .impositionId(9001L)
            .resultId("FCOMP")
            .imposedAmount(new BigDecimal("50.00"))
            .postedDate(POSTED_DATE)
            .imposedDate(IMPOSED_DATE)
            .build();
    }
}
