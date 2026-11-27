package uk.gov.hmcts.opal.mapper;

import static org.springframework.util.StringUtils.hasText;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemType;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.projection.SuspenseTransactionHistoryProjection;
import uk.gov.hmcts.opal.generated.model.AdjustmentDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.AutoPaymentDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.CompanyNameCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.CreditorSummaryCommon;
import uk.gov.hmcts.opal.generated.model.DefendantDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualNameCommon;
import uk.gov.hmcts.opal.generated.model.ManualPaymentCourtFeeDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ManualPaymentPayerDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PayerDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ReversalDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ThirdPartyRepayDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.ManualPaymentUnidentifiedDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.generated.model.PaymentDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PaymentDetailsCommonStrict.ReceivedFromEnum;
import uk.gov.hmcts.opal.generated.model.PaymentSourceCommonStrict;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.generated.model.SuspenseNoteCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseTransactionCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseTransactionCommonStrictDetails;
import uk.gov.hmcts.opal.generated.model.TransactionTypeCodeCommonStrict;
import uk.gov.hmcts.opal.generated.model.TransactionTypeCommonStrict;

@Component
public class SuspenseItemHistoryMapper {

    public SuspenseNoteCommonStrict toSuspenseNote(NoteEntity entity) {
        NoteCommon note = new NoteCommon()
            .recordType(NoteCommon.RecordTypeEnum.fromValue(
                entity.getAssociatedRecordType().getLabel()))
            .recordId(entity.getAssociatedRecordId())
            .noteText(entity.getNoteText())
            .noteType(NoteCommon.NoteTypeEnum.fromValue(
                entity.getNoteType().name()));

        PostedDetailsCommon postedDetails = new PostedDetailsCommon()
            .postedDate(entity.getPostedDate())
            .postedBy(entity.getBusinessUnitUserId())
            .postedByName(entity.getPostedByUsername());

        return new SuspenseNoteCommonStrict()
            .note(note)
            .postedDetails(postedDetails);
    }

    public SuspenseTransactionCommonStrict toSuspenseTransaction(SuspenseTransactionHistoryProjection row) {
        PostedDetailsCommon postedDetails = new PostedDetailsCommon()
            .postedDate(row.getPostedDate())
            .postedBy(row.getPostedBy())
            .postedByName(row.getPostedByName());

        return new SuspenseTransactionCommonStrict()
            .transactionId(row.getTransactionId())
            .transactionType(
                TransactionTypeCommonStrict
                    .builder()
                    .transactionTypeCode(TransactionTypeCodeCommonStrict.valueOf(row.getTransactionType()))
                    .transactionTypeDisplayName(Arrays.stream(SuspenseItemType.values())
                        .filter(type -> type.name().equals(row.getTransactionType()))
                        .map(SuspenseItemType::getDescription)
                        .findFirst().orElse(row.getTransactionType()))
                    .build())
            .postedDetails(postedDetails)
            .details(getTransactionDetails(row))
            .amount(row.getAmount());
    }

    private @Nullable SuspenseTransactionCommonStrictDetails getTransactionDetails(
        SuspenseTransactionHistoryProjection row) {

        // Provisional selection rules: docs/suspense-history-details.md.
        return switch (row.getTransactionType()) {
            case "RV" -> null;
            case "DQ" -> row.getText() == null ? null
                : new ReversalDetailsCommonStrict().reversalDescription(row.getText());
            case "IN" -> toAdjustmentDetails(row);
            case "RP" -> toRepaymentDetails(row);
            case "XP" -> null;
            default -> row.getPaymentInId() == null ? toAccountDetails(row) : toPaymentDetails(row);
        };
    }

    private @Nullable SuspenseTransactionCommonStrictDetails toPaymentDetails(
        SuspenseTransactionHistoryProjection row) {
        if ("CF".equals(row.getTransactionType()) || "OC".equals(row.getTransactionType())) {
            PayerDetailsCommonStrict payer = toPayer(row);
            return hasText(row.getCourtFeeCode()) && payer != null
                ? new ManualPaymentCourtFeeDetailsCommonStrict()
                    .courtFeeCode(row.getCourtFeeCode())
                    .paymentDetails(toPayment(row))
                    .payerDetails(payer) : null;
        }
        if ("UN".equals(row.getTransactionType()) && "UN".equals(row.getSuspenseItemType())
            && !StringUtils.isEmpty(row.getOriginatorReference())) {
            return new ManualPaymentUnidentifiedDetailsCommonStrict()
                .paymentDetails(toPayment(row))
                .relevantDetails(row.getRelevantDetails());
        }
        if ("OF".equals(row.getTransactionType()) && "OF".equals(row.getSuspenseItemType())) {
            Long defendantAccountNumber = StringUtils.isEmpty(row.getDefendantAccountNumber())
                ? Long.valueOf(0) : toAccountNumber(row.getDefendantAccountNumber());
            if (defendantAccountNumber == null || row.getPaymentSource() == null) {
                return null;
            }
            return new AutoPaymentDetailsCommonStrict()
                .defendantAccountNumber(defendantAccountNumber)
                .originatorReference(row.getOriginatorReference())
                .paymentSource(row.getPaymentSource());
        }
        PayerDetailsCommonStrict payer = toPayer(row);
        return payer == null ? null : new ManualPaymentPayerDetailsCommonStrict()
            .paymentDetails(toPayment(row)).payerDetails(payer);
    }

    private PaymentDetailsCommonStrict toPayment(SuspenseTransactionHistoryProjection row) {
        PaymentDetailsCommonStrict payment = new PaymentDetailsCommonStrict()
            .paymentReference(row.getPaymentReference())
            .receivedFrom(toReceivedFrom(row.getPaymentReceivedFrom()));
        String method = row.getPaymentMethod();
        if (method != null) {
            payment.paymentMethod(PaymentDetailsCommonStrict.PaymentMethodEnum.fromValue(method));
        }
        if (hasText(row.getPaymentSource())) {
            payment.paymentSource(new PaymentSourceCommonStrict().paymentSource(
                PaymentSourceCommonStrict.PaymentSourceEnum.fromValue(row.getPaymentSource())));
        }
        return payment;
    }

    private @Nullable PayerDetailsCommonStrict toPayer(SuspenseTransactionHistoryProjection row) {
        if (!("Individual".equals(row.getPayerType()) || "Organisation".equals(row.getPayerType()))
            || !hasText(row.getPayerName())) {
            return null;
        }
        return new PayerDetailsCommonStrict()
            .payerType(PayerDetailsCommonStrict.PayerTypeEnum.fromValue(row.getPayerType()))
            .payerName(row.getPayerName())
            .payerAddress(row.getPayerAddress());
    }

    private @Nullable SuspenseTransactionCommonStrictDetails toAccountDetails(
        SuspenseTransactionHistoryProjection row) {
        // An imposition reference is only supported for adjustments.
        if (row.getImpositionId() != null) {
            return null;
        }
        if (row.getCreditorAccountId() != null) {
            CreditorSummaryCommon creditor = toCreditor(row);
            AddressDetailsCommon address = toCreditorAddress(row);
            return creditor == null || address == null ? null : new CreditorDetailsCommonStrict()
                .creditorAccountNumber(row.getCreditorAccountNumber())
                .creditorSummary(creditor).creditorAddress(address);
        }
        return toDefendantDetails(row);
    }

    private @Nullable DefendantDetailsCommonStrict toDefendantDetails(SuspenseTransactionHistoryProjection row) {
        Long number = toAccountNumber(row.getDefendantAccountNumber());
        PartyDetailsCommonStrict name = toDefendantName(row);
        return number == null || name == null ? null : new DefendantDetailsCommonStrict()
            .defendantAccountNumber(number).defendantName(name);
    }

    private @Nullable AdjustmentDetailsCommonStrict toAdjustmentDetails(SuspenseTransactionHistoryProjection row) {
        Long number = toAccountNumber(row.getDefendantAccountNumber());
        PartyDetailsCommonStrict name = toDefendantName(row);
        CreditorSummaryCommon creditor = toCreditor(row);
        AddressDetailsCommon address = toCreditorAddress(row);
        if (row.getImpositionId() == null || number == null || name == null || creditor == null || address == null) {
            return null;
        }
        return new AdjustmentDetailsCommonStrict()
            .impositionId(row.getImpositionId()).defendantAccountNumber(number).defendantName(name)
            .creditorSummary(creditor).creditorAddress(address);
    }

    private @Nullable SuspenseTransactionCommonStrictDetails toRepaymentDetails(
        SuspenseTransactionHistoryProjection row) {
        if ("defendant_accounts".equals(row.getAssociatedRecordType())) {
            return toDefendantDetails(row);
        }
        if (!("creditor_accounts".equals(row.getAssociatedRecordType())
            || "creditor_transactions".equals(row.getAssociatedRecordType()))) {
            return null;
        }
        CreditorSummaryCommon creditor = toCreditor(row);
        AddressDetailsCommon address = toCreditorAddress(row);
        if (!Boolean.TRUE.equals(row.getRepaymentCreditor()) || creditor == null || address == null) {
            return null;
        }
        return new ThirdPartyRepayDetailsCommonStrict()
            .creditorAccountNumber(row.getCreditorAccountNumber())
            .creditorSummary(creditor).creditorAddress(address)
            .creditorPaymentMethod(Boolean.TRUE.equals(row.getPayByBacs())
                ? ThirdPartyRepayDetailsCommonStrict.CreditorPaymentMethodEnum.PAY_BY_BACS
                : ThirdPartyRepayDetailsCommonStrict.CreditorPaymentMethodEnum.PAY_BY_CHEQUE);
    }

    private @Nullable PartyDetailsCommonStrict toDefendantName(SuspenseTransactionHistoryProjection row) {
        if (row.getDefendantPartyId() == null || row.getDefendantOrganisation() == null) {
            return null;
        }
        PartyDetailsCommonStrict name = new PartyDetailsCommonStrict()
            .partyId(row.getDefendantPartyId().toString()).organisationFlag(row.getDefendantOrganisation());
        if (Boolean.TRUE.equals(row.getDefendantOrganisation())) {
            return hasText(row.getDefendantOrganisationName()) ? name.organisationDetails(
                new OrganisationDetailsCommonStrict().organisationName(row.getDefendantOrganisationName())) : null;
        }
        return hasText(row.getDefendantSurname()) ? name.individualDetails(new IndividualDetailsCommonStrict()
            .title(row.getDefendantTitle()).forenames(row.getDefendantForenames()).surname(row.getDefendantSurname()))
            : null;
    }

    private @Nullable CreditorSummaryCommon toCreditor(SuspenseTransactionHistoryProjection row) {
        if (row.getCreditorAccountId() == null || row.getCreditorAccountType() == null) {
            return null;
        }
        CreditorAccountType type = CreditorAccountType.valueOf(row.getCreditorAccountType());
        CreditorSummaryCommon summary = new CreditorSummaryCommon()
            .creditorAccountId(row.getCreditorAccountId())
            .creditorAccountTypeReference(new CreditorAccountTypeReferenceCommon()
                .creditorAccountType(CreditorAccountTypeReferenceCommon.CreditorAccountTypeEnum.fromValue(type.name()))
                .creditorAccountDisplayName(CreditorAccountTypeReferenceCommon.CreditorAccountDisplayNameEnum
                    .fromValue(type.getLabel())));
        if (type == CreditorAccountType.MJ) {
            return hasText(row.getMajorCreditorName()) ? summary.majorCreditorName(row.getMajorCreditorName()) : null;
        }
        if (type == CreditorAccountType.MN) {
            return toMinorCreditor(row, summary);
        }
        return summary;
    }

    private @Nullable CreditorSummaryCommon toMinorCreditor(SuspenseTransactionHistoryProjection row,
                                                           CreditorSummaryCommon summary) {
        if (row.getCreditorPartyId() == null || row.getCreditorOrganisation() == null) {
            return null;
        }
        summary.minorCreditorOrganisationFlag(row.getCreditorOrganisation());
        if (Boolean.TRUE.equals(row.getCreditorOrganisation())) {
            return hasText(row.getCreditorOrganisationName()) ? summary.companyName(
                new CompanyNameCommon().organisationName(row.getCreditorOrganisationName())) : null;
        }
        return hasText(row.getCreditorSurname()) ? summary.individualName(new IndividualNameCommon()
            .forenames(row.getCreditorForenames()).surname(row.getCreditorSurname())) : null;
    }

    private @Nullable AddressDetailsCommon toCreditorAddress(SuspenseTransactionHistoryProjection row) {
        if (!hasText(row.getCreditorAddressLine1()) && !hasText(row.getCreditorAddressLine2())
            && !hasText(row.getCreditorAddressLine3()) && !hasText(row.getCreditorAddressLine4())
            && !hasText(row.getCreditorAddressLine5()) && !hasText(row.getCreditorPostcode())) {
            return null;
        }
        return new AddressDetailsCommon().addressLine1(row.getCreditorAddressLine1())
            .addressLine2(row.getCreditorAddressLine2()).addressLine3(row.getCreditorAddressLine3())
            .addressLine4(row.getCreditorAddressLine4()).addressLine5(row.getCreditorAddressLine5())
            .postcode(row.getCreditorPostcode());
    }

    private @Nullable Long toAccountNumber(@Nullable String number) {
        if (number == null) {
            return null;
        }
        try {
            return Long.valueOf(number);
        } catch (NumberFormatException exception) {
            // DB account numbers are strings; this API only accepts int64 values.
            return null;
        }
    }

    private @Nullable ReceivedFromEnum toReceivedFrom(@Nullable String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "D" -> ReceivedFromEnum.DEFENDANT;
            case "A" -> ReceivedFromEnum.APPLICANT;
            case "T" -> ReceivedFromEnum.THIRD_PARTY;
            default -> null;
        };
    }

}
