package uk.gov.hmcts.opal.service.report;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;

@Component
@RequiredArgsConstructor
public class CashListReportAssembler {

    private static final String FINE_DESTINATION_TYPE = "F";
    private static final String SUSPENSE_DESTINATION_TYPE = "S";
    private static final String FINE_REPORT_TYPE = "FA";
    private static final String SUSPENSE_REPORT_TYPE = "SA";
    private static final String SUSPENSE_ACCOUNT_NUMBER = "Suspense Ref";

    private final CashListPaymentLinkService cashListPaymentLinkService;

    public CashListReportData toReportData(TillEntity till, BusinessUnitEntity businessUnit,
        List<PaymentInEntity> payments) {
        return CashListReportData.builder()
            .tillDetails(toTillDetails(till, businessUnit))
            .entries(toEntries(payments))
            .total(total(payments))
            .build();
    }

    private static CashListReportData.TillDetails toTillDetails(TillEntity till, BusinessUnitEntity businessUnit) {
        return CashListReportData.TillDetails.builder()
            .tillId(till.getTillId())
            .tillNumber(till.getTillNumber())
            .ownedBy(till.getOwnedBy())
            .businessUnitId(businessUnit.getBusinessUnitId())
            .businessUnitName(businessUnit.getBusinessUnitName())
            .businessUnitCode(businessUnit.getBusinessUnitCode())
            .build();
    }

    private List<CashListReportData.CashListEntry> toEntries(List<PaymentInEntity> payments) {
        return IntStream.range(0, payments.size())
            .mapToObj(index -> toEntry(index + 1, payments.get(index)))
            .toList();
    }

    private CashListReportData.CashListEntry toEntry(int entryNumber, PaymentInEntity payment) {
        return switch (payment.getDestinationType()) {
            case FINE_DESTINATION_TYPE -> toFineEntry(entryNumber, payment);
            case SUSPENSE_DESTINATION_TYPE -> toSuspenseEntry(entryNumber, payment);
            default -> throw new IllegalArgumentException(
                "Payment " + payment.getPaymentInId() + " has unsupported destination_type: "
                    + payment.getDestinationType());
        };
    }

    private CashListReportData.CashListEntry toFineEntry(int entryNumber, PaymentInEntity payment) {
        DefendantAccountEntity defendantAccount = cashListPaymentLinkService.getDefendantAccount(payment);
        PartyEntity defendant = findDefendantParty(defendantAccount)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant party not found for defendant_account_id: "
                    + defendantAccount.getDefendantAccountId()));

        return baseEntry(entryNumber, payment)
            .type(FINE_REPORT_TYPE)
            .accountNumber(defendantAccount.getAccountNumber())
            .name(toDefendantName(defendant))
            .build();
    }

    private CashListReportData.CashListEntry toSuspenseEntry(int entryNumber, PaymentInEntity payment) {
        SuspenseItemEntity suspenseItem = cashListPaymentLinkService.getSuspenseItem(payment);

        return baseEntry(entryNumber, payment)
            .type(SUSPENSE_REPORT_TYPE)
            .suspense(suspenseItem.getSuspenseItemType().name())
            .accountNumber(SUSPENSE_ACCOUNT_NUMBER)
            .name(String.valueOf(suspenseItem.getSuspenseItemNumber()))
            .nameAdditionalInformation(toAdditionalInformation(payment))
            .build();
    }

    private static CashListReportData.CashListEntry.CashListEntryBuilder baseEntry(int entryNumber,
                                                                                   PaymentInEntity payment) {
        return CashListReportData.CashListEntry.builder()
            .entry(entryNumber)
            .paymentMethod(payment.getPaymentMethod())
            .amount(payment.getPaymentAmount());
    }

    private static Optional<PartyEntity> findDefendantParty(DefendantAccountEntity defendantAccount) {
        List<DefendantAccountPartiesEntity> parties = defendantAccount.getParties();
        if (parties == null || parties.isEmpty()) {
            return Optional.empty();
        }

        return findParty(parties, party -> AssociationType.DEFENDANT.equals(party.getAssociationType()))
            .or(() -> findParty(parties, party -> Boolean.TRUE.equals(party.getDebtor())))
            .or(() -> parties.stream().findFirst())
            .map(DefendantAccountPartiesEntity::getParty);
    }

    private static Optional<DefendantAccountPartiesEntity> findParty(
        List<DefendantAccountPartiesEntity> parties, Predicate<DefendantAccountPartiesEntity> predicate) {
        return parties.stream().filter(predicate).findFirst();
    }

    private static String toDefendantName(PartyEntity party) {
        if (party.isOrganisation()) {
            return party.getOrganisationName();
        }
        String surname = Optional.ofNullable(party.getSurname()).orElse("").toUpperCase(Locale.ROOT);
        String forenames = Optional.ofNullable(party.getForenames()).orElse("");
        return Stream.of(surname, forenames)
            .filter(namePart -> !namePart.isBlank())
            .reduce((left, right) -> left + " " + right)
            .orElse(null);
    }

    private static String toAdditionalInformation(PaymentInEntity payment) {
        String creationMethod = payment.isAutoPayment() ? "Auto" : "Manual";
        String paymentInformation = payment.getAdditionalInformation();
        if (paymentInformation == null || paymentInformation.isBlank()) {
            return creationMethod;
        }
        return creationMethod + " - " + paymentInformation;
    }

    private static BigDecimal total(List<PaymentInEntity> payments) {
        return payments.stream()
            .map(PaymentInEntity::getPaymentAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
