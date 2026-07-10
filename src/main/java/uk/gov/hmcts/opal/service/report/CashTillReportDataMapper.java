package uk.gov.hmcts.opal.service.report;

import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.MiscellaneousAccountRepository;

@Component
@RequiredArgsConstructor
public class CashTillReportDataMapper {

    private final DefendantAccountRepository defendantAccountRepository;
    private final MiscellaneousAccountRepository miscellaneousAccountRepository;

    public CashTillReportData map(boolean allocatedReport, TillEntity till, List<PaymentInEntity> payments) {
        Map<Long, String> defendantAccountNumbers = loadDefendantAccountNumbers(payments);
        Map<Long, String> miscellaneousAccountNumbers = loadMiscellaneousAccountNumbers(payments);
        List<CashTillReportRow> rows = payments.stream()
            .map(payment -> toReportRow(till, payment, defendantAccountNumbers, miscellaneousAccountNumbers))
            .toList();

        return CashTillReportData.builder()
            .allocatedReport(allocatedReport)
            .rows(rows)
            .reportMetaData(new ReportMetaData(pdpoParticipants(payments)))
            .build();
    }

    private static CashTillReportRow toReportRow(TillEntity till,
                                                 PaymentInEntity payment,
                                                 Map<Long, String> defendantAccountNumbers,
                                                 Map<Long, String> miscellaneousAccountNumbers) {
        return CashTillReportRow.builder()
            .businessUnit(getBusinessUnitName(till))
            .cashTillNumber(String.valueOf(till.getTillNumber()))
            .cashier(till.getOwnedBy())
            .paymentDateTime(payment.getPaymentDate())
            .destinationType(CashTillDestinationType.fromPaymentDestinationType(payment.getDestinationType()))
            .details(resolveDetails(payment, defendantAccountNumbers, miscellaneousAccountNumbers))
            .autoPayment(payment.isAutoPayment())
            .paymentMethod(payment.getPaymentMethod() == null
                ? null
                : CashTillPaymentMethod.fromValue(payment.getPaymentMethod().name()))
            .amount(payment.getPaymentAmount())
            .receipt(payment.isReceipt())
            .balance(payment.getPaymentAmount())
            .allocated(payment.isAllocated())
            .build();
    }

    private static String getBusinessUnitName(TillEntity till) {
        BusinessUnitEntity businessUnit = till.getBusinessUnit();
        if (businessUnit == null
            || businessUnit.getBusinessUnitName() == null
            || businessUnit.getBusinessUnitName().isBlank()) {
            throw new IllegalArgumentException("Cash Till report till is missing a business unit name");
        }
        return businessUnit.getBusinessUnitName();
    }

    private static String resolveDetails(PaymentInEntity payment,
                                         Map<Long, String> defendantAccountNumbers,
                                         Map<Long, String> miscellaneousAccountNumbers) {
        long associatedRecordId = parseAssociatedRecordId(payment);
        return switch (associatedRecordType(payment)) {
            case DEFENDANT_ACCOUNTS -> requireAccountNumber(
                defendantAccountNumbers.get(associatedRecordId),
                payment,
                "defendant account");
            case MISCELLANEOUS_ACCOUNTS -> requireAccountNumber(
                miscellaneousAccountNumbers.get(associatedRecordId),
                payment,
                "miscellaneous account");
            default -> throw new IllegalArgumentException(
                "Cash Till payment " + payment.getPaymentInId()
                    + " has unsupported associated_record_type " + payment.getAssociatedRecordType());
        };
    }

    private static long parseAssociatedRecordId(PaymentInEntity payment) {
        String associatedRecordId = payment.getAssociatedRecordId();
        if (associatedRecordId == null || associatedRecordId.isBlank()) {
            throw new IllegalArgumentException("Cash Till payment " + payment.getPaymentInId()
                + " is missing associated_record_id");
        }
        try {
            return Long.parseLong(associatedRecordId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cash Till payment " + payment.getPaymentInId()
                + " has invalid associated_record_id " + associatedRecordId, e);
        }
    }

    private static AssociatedRecordType associatedRecordType(PaymentInEntity payment) {
        AssociatedRecordType associatedRecordType = payment.getAssociatedRecordType();
        if (associatedRecordType == null) {
            throw new IllegalArgumentException("Cash Till payment " + payment.getPaymentInId()
                + " is missing associated_record_type");
        }
        return associatedRecordType;
    }

    private static String requireAccountNumber(String accountNumber, PaymentInEntity payment, String recordType) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new EntityNotFoundException("Cash Till payment " + payment.getPaymentInId()
                + " references a missing " + recordType);
        }
        return accountNumber;
    }

    private Map<Long, String> loadDefendantAccountNumbers(List<PaymentInEntity> payments) {
        List<Long> defendantAccountIds = payments.stream()
            .filter(payment -> associatedRecordType(payment) == AssociatedRecordType.DEFENDANT_ACCOUNTS)
            .map(CashTillReportDataMapper::parseAssociatedRecordId)
            .distinct()
            .toList();
        if (defendantAccountIds.isEmpty()) {
            return Map.of();
        }
        return defendantAccountRepository.findAllByDefendantAccountIdIn(defendantAccountIds).stream()
            .filter(account -> account.getDefendantAccountId() != null)
            .collect(LinkedHashMap::new,
                (map, account) -> map.put(account.getDefendantAccountId(), account.getAccountNumber()),
                LinkedHashMap::putAll);
    }

    private Map<Long, String> loadMiscellaneousAccountNumbers(List<PaymentInEntity> payments) {
        List<Long> miscellaneousAccountIds = payments.stream()
            .filter(payment -> associatedRecordType(payment) == AssociatedRecordType.MISCELLANEOUS_ACCOUNTS)
            .map(CashTillReportDataMapper::parseAssociatedRecordId)
            .distinct()
            .toList();
        if (miscellaneousAccountIds.isEmpty()) {
            return Map.of();
        }
        return miscellaneousAccountRepository.findAllByMiscellaneousAccountIdIn(miscellaneousAccountIds).stream()
            .filter(account -> account.getMiscellaneousAccountId() != null)
            .collect(LinkedHashMap::new,
                (map, account) -> map.put(account.getMiscellaneousAccountId(), account.getAccountNumber()),
                LinkedHashMap::putAll);
    }

    private static List<ParticipantIdentifier> pdpoParticipants(List<PaymentInEntity> payments) {
        return payments.stream()
            .filter(payment -> associatedRecordType(payment) == AssociatedRecordType.DEFENDANT_ACCOUNTS)
            .map(CashTillReportDataMapper::parseAssociatedRecordId)
            .distinct()
            .map(defendantAccountId -> new ParticipantIdentifier(
                String.valueOf(defendantAccountId),
                PdplIdentifierType.DEFENDANT_ACCOUNT))
            .toList();
    }
}
