package uk.gov.hmcts.opal.service.opal.history.defendant;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.SPACED_PIPE;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;

/**
 * Class to generate the complex transaction details string from a transaction entity.
 */
@Service
public class DefendantTransactionDetailsService {

    /**
     * The Text generated is based on the type of the defendant transaction, and then it's values.
     *
     * @param transaction the defendant transaction entity
     * @param account the associated defendant account entity
     * @param imposition the imposition entity, will only be populated (and used) if the transaction associated record
     *                   type is IMPOSITIONS
     * @return the transaction's description/details as a String
     */
    public String generateTransactionDetails(DefendantTransactionEntity transaction,
        DefendantAccountEntity account, ImpositionEntity imposition) {
        return switch (transaction.getTransactionType()) {
            case CANCHQ -> "Cheque cancelled | Cheque number: " + transaction.getPaymentReference();
            case CHEQUE -> chequeData("Cheque issued", transaction);
            case CONSOL -> "Account consolidated | " + transaction.getDefendantAccountId()
                + " | Amount credited to master account";
            case DISHCQ -> "Cheque dishonoured | " + imposition(imposition);
            case FR_SUS -> "Transfer from suspense | " + transaction.getAssociatedRecordId();
            case MADJ -> "Manual adjustment | " + transaction.getAssociatedRecordId();
            case PAYMNT -> payment(transaction);
            case REPSUS -> "Repayment from suspense | " + transaction.getAssociatedRecordId();
            case REVPAY -> "Payment reversed | " + imposition(imposition);
            case RICHEQ -> chequeData("Cheque reissued", transaction);
            case RVWOFF -> "Write-off reversed " + transaction.getText();
            case TFO -> "TFO out | Transferred to: %s %s"
                .formatted(transaction.getText(), account.getOriginatorName());
            case TFO_IN -> "TFO in | Received from: " + account.getOriginatorName();
            case WRTOFF -> writeOff(transaction, imposition);
            case XFER -> transferToSuspense(transaction);
        };
    }

    private String chequeData(String prefix, DefendantTransactionEntity transaction) {
        String status = switch (transaction.getStatus()) {
            case D, X -> SPACED_PIPE + transaction.getStatus().getDisplayName() + " "
                + transaction.getStatusDate();
            default -> "";
        };
        return prefix + " | Cheque number: " + (transaction.getPaymentReference() != null
                ? transaction.getPaymentReference()
                : "Not yet written")
            + status;
    }

    private String payment(DefendantTransactionEntity transaction) {
        return "Payment received | " + transaction.getPaymentMethod().getDisplayName()
            + conditionalValue(SPACED_PIPE, transaction.getText())
            + conditionalValue(SPACED_PIPE, transaction.getPaymentReference());
    }

    private String writeOff(DefendantTransactionEntity transaction, ImpositionEntity imposition) {
        String writeOffDesc = conditionalValue(SPACED_PIPE, transaction.getWriteOffCode().getDisplayName());
        String details = switch (transaction.getAssociatedRecordType()) {
            case IMPOSITIONS ->  " | %s | %s%s".formatted(imposition.getImposedDate().toString(),
                imposition(imposition), writeOffDesc);
            case DEFENDANT_ACCOUNTS -> writeOffDesc + " - " + transaction.getDefendantAccountId();
            default -> SPACED_PIPE;
        };
        return "Write-off" + details + conditionalValue(SPACED_PIPE, transaction.getText());
    }

    private String imposition(ImpositionEntity imposition) {
        return "%s %s Created: %s | %s".formatted(imposition.getResultId(),
        imposition.getImposedAmount().toString(), imposition.getPostedDate().toString(),
            imposition.getImpositionId());
    }

    private String transferToSuspense(DefendantTransactionEntity transaction) {
        String reason = switch (transaction.getAssociatedRecordType()) {
            case SUSPENSE_TRANSACTIONS -> "Cheque cancelled to suspense";
            case CREDITOR_TRANSACTIONS -> "Cheque cancelled to Central Fund";
            default -> "";
        };
        return "Suspense transfer | " + reason;
    }

    private String conditionalValue(String prefix, String value) {
        return value != null && !value.isBlank() ? prefix + value : "";
    }
}
