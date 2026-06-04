package uk.gov.hmcts.opal.service.opal.history.defendant;

import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.dto.history.PaymentTermsDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryAmendmentDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryDefendantTransactionDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryEnforcementDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryNoteDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryPaymentTermsDetails;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryPostedDetails;

public final class DefendantAccountHistoryModelAdapter {

    private DefendantAccountHistoryModelAdapter() {
    }

    public static AccountHistoryFilter toCoreFilter(DefendantAccountHistoryFilter filter) {
        return AccountHistoryFilter.builder()
            .dateFrom(filter.getDateFrom())
            .dateTo(filter.getDateTo())
            .itemTypes(filter.getItemTypes() == null ? null : filter.getItemTypes().stream()
                .map(DefendantAccountHistoryModelAdapter::toCoreItemType)
                .toList())
            .build();
    }

    public static AccountHistoryItem toCoreItem(DefendantAccountHistoryItem item) {
        return AccountHistoryItem.builder()
            .postedDetails(toCorePostedDetails(item.getPostedDetails()))
            .type(toCoreItemType(item.getType()))
            .details(toCoreDetails(item.getDetails()))
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }

    public static DefendantAccountHistoryItem toDefendantItem(AccountHistoryItem item) {
        return DefendantAccountHistoryItem.builder()
            .postedDetails(toDefendantPostedDetails(item.getPostedDetails()))
            .type(toDefendantItemType(item.getType()))
            .details(toDefendantDetails(item.getDetails()))
            .amount(item.getAmount())
            .eventDateTime(item.getEventDateTime())
            .sourceId(item.getSourceId())
            .build();
    }

    public static AccountHistoryDetails toCoreDetails(Object details) {
        if (details == null) {
            return null;
        }

        if (details instanceof AmendmentDetails amendmentDetails) {
            return AccountHistoryAmendmentDetails.builder()
                .attributeName(amendmentDetails.getAttributeName())
                .oldValue(amendmentDetails.getOldValue())
                .newValue(amendmentDetails.getNewValue())
                .build();
        }
        if (details instanceof EnforcementDetails enforcementDetails) {
            return AccountHistoryEnforcementDetails.builder()
                .enforcementAction(enforcementDetails.getEnforcementAction())
                .daysInDefault(enforcementDetails.getDaysInDefault())
                .warrantNumber(enforcementDetails.getWarrantNumber())
                .hearingDate(enforcementDetails.getHearingDate())
                .hearingCourt(enforcementDetails.getHearingCourt())
                .caseNumber(enforcementDetails.getCaseNumber())
                .reason(enforcementDetails.getReason())
                .earliestDateOfRelease(enforcementDetails.getEarliestDateOfRelease())
                .build();
        }
        if (details instanceof NoteDetails noteDetails) {
            return AccountHistoryNoteDetails.builder()
                .noteText(noteDetails.getNoteText())
                .build();
        }
        if (details instanceof PaymentTermsDetails paymentTermsDetails) {
            return AccountHistoryPaymentTermsDetails.builder()
                .daysInDefault(paymentTermsDetails.getDaysInDefault())
                .dateDaysInDefaultImposed(paymentTermsDetails.getDateDaysInDefaultImposed())
                .reasonForExtension(paymentTermsDetails.getReasonForExtension())
                .paymentTermsType(paymentTermsDetails.getPaymentTermsType())
                .effectiveDate(paymentTermsDetails.getEffectiveDate())
                .instalmentPeriod(paymentTermsDetails.getInstalmentPeriod())
                .lumpSumAmount(paymentTermsDetails.getLumpSumAmount())
                .instalmentAmount(paymentTermsDetails.getInstalmentAmount())
                .build();
        }
        if (details instanceof DefendantTransactionDetails transactionDetails) {
            return AccountHistoryDefendantTransactionDetails.builder()
                .transactionType(transactionDetails.getTransactionType())
                .paymentMethod(transactionDetails.getPaymentMethod())
                .paymentReference(transactionDetails.getPaymentReference())
                .additionalInformation(transactionDetails.getAdditionalInformation())
                .writeOff(transactionDetails.getWriteOff())
                .status(transactionDetails.getStatus())
                .statusDate(transactionDetails.getStatusDate())
                .associatedRecordType(transactionDetails.getAssociatedRecordType())
                .associatedRecordId(transactionDetails.getAssociatedRecordId())
                .accountNumber(transactionDetails.getAccountNumber())
                .sendingCourt(transactionDetails.getSendingCourt())
                .impositionDate(transactionDetails.getImpositionDate())
                .impositionCode(transactionDetails.getImpositionCode())
                .amountImposed(transactionDetails.getAmountImposed())
                .build();
        }

        throw new IllegalArgumentException(
            "Unsupported defendant history details type: " + details.getClass().getName()
        );
    }

    public static Object toDefendantDetails(AccountHistoryDetails details) {
        if (details == null) {
            return null;
        }

        if (details instanceof AccountHistoryAmendmentDetails amendmentDetails) {
            return AmendmentDetails.builder()
                .attributeName(amendmentDetails.getAttributeName())
                .oldValue(amendmentDetails.getOldValue())
                .newValue(amendmentDetails.getNewValue())
                .build();
        }
        if (details instanceof AccountHistoryEnforcementDetails enforcementDetails) {
            return EnforcementDetails.builder()
                .enforcementAction(enforcementDetails.getEnforcementAction())
                .daysInDefault(enforcementDetails.getDaysInDefault())
                .warrantNumber(enforcementDetails.getWarrantNumber())
                .hearingDate(enforcementDetails.getHearingDate())
                .hearingCourt(enforcementDetails.getHearingCourt())
                .caseNumber(enforcementDetails.getCaseNumber())
                .reason(enforcementDetails.getReason())
                .earliestDateOfRelease(enforcementDetails.getEarliestDateOfRelease())
                .build();
        }
        if (details instanceof AccountHistoryNoteDetails noteDetails) {
            return NoteDetails.builder()
                .noteText(noteDetails.getNoteText())
                .build();
        }
        if (details instanceof AccountHistoryPaymentTermsDetails paymentTermsDetails) {
            return PaymentTermsDetails.builder()
                .daysInDefault(paymentTermsDetails.getDaysInDefault())
                .dateDaysInDefaultImposed(paymentTermsDetails.getDateDaysInDefaultImposed())
                .reasonForExtension(paymentTermsDetails.getReasonForExtension())
                .paymentTermsType(paymentTermsDetails.getPaymentTermsType())
                .effectiveDate(paymentTermsDetails.getEffectiveDate())
                .instalmentPeriod(paymentTermsDetails.getInstalmentPeriod())
                .lumpSumAmount(paymentTermsDetails.getLumpSumAmount())
                .instalmentAmount(paymentTermsDetails.getInstalmentAmount())
                .build();
        }
        if (details instanceof AccountHistoryDefendantTransactionDetails transactionDetails) {
            return DefendantTransactionDetails.builder()
                .transactionType(transactionDetails.getTransactionType())
                .paymentMethod(transactionDetails.getPaymentMethod())
                .paymentReference(transactionDetails.getPaymentReference())
                .additionalInformation(transactionDetails.getAdditionalInformation())
                .writeOff(transactionDetails.getWriteOff())
                .status(transactionDetails.getStatus())
                .statusDate(transactionDetails.getStatusDate())
                .associatedRecordType(transactionDetails.getAssociatedRecordType())
                .associatedRecordId(transactionDetails.getAssociatedRecordId())
                .accountNumber(transactionDetails.getAccountNumber())
                .sendingCourt(transactionDetails.getSendingCourt())
                .impositionDate(transactionDetails.getImpositionDate())
                .impositionCode(transactionDetails.getImpositionCode())
                .amountImposed(transactionDetails.getAmountImposed())
                .build();
        }

        throw new IllegalArgumentException("Unsupported core history details type: " + details.getClass().getName());
    }

    public static AccountHistoryPostedDetails toCorePostedDetails(uk.gov.hmcts.opal.dto.PostedDetails postedDetails) {
        if (postedDetails == null) {
            return null;
        }

        return AccountHistoryPostedDetails.builder()
            .postedDate(postedDetails.getPostedDate())
            .postedBy(postedDetails.getPostedBy())
            .postedByName(postedDetails.getPostedByName())
            .build();
    }

    public static uk.gov.hmcts.opal.dto.PostedDetails toDefendantPostedDetails(
        AccountHistoryPostedDetails postedDetails
    ) {
        if (postedDetails == null) {
            return null;
        }

        return uk.gov.hmcts.opal.dto.PostedDetails.builder()
            .postedDate(postedDetails.getPostedDate())
            .postedBy(postedDetails.getPostedBy())
            .postedByName(postedDetails.getPostedByName())
            .build();
    }

    public static AccountHistoryItemType toCoreItemType(HistoryItemType type) {
        return switch (type) {
            case AMENDMENT -> AccountHistoryItemType.AMENDMENT;
            case ENFORCEMENT -> AccountHistoryItemType.ENFORCEMENT;
            case FINANCIAL -> AccountHistoryItemType.FINANCIAL;
            case NOTE -> AccountHistoryItemType.NOTE;
            case PAYMENT_TERMS -> AccountHistoryItemType.PAYMENT_TERMS;
        };
    }

    public static HistoryItemType toDefendantItemType(AccountHistoryItemType type) {
        return switch (type) {
            case AMENDMENT -> HistoryItemType.AMENDMENT;
            case ENFORCEMENT -> HistoryItemType.ENFORCEMENT;
            case FINANCIAL -> HistoryItemType.FINANCIAL;
            case NOTE -> HistoryItemType.NOTE;
            case PAYMENT_TERMS -> HistoryItemType.PAYMENT_TERMS;
        };
    }
}
