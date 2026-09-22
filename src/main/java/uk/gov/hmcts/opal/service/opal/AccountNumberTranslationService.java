package uk.gov.hmcts.opal.service.opal;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.interfacefile.BankDetails;
import uk.gov.hmcts.opal.entity.interfacefile.DestinationDetails;
import uk.gov.hmcts.opal.entity.interfacefile.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.entity.interfacefile.OriginatorDetails;
import uk.gov.hmcts.opal.entity.interfacefile.Transaction;
import uk.gov.hmcts.opal.entity.interfacefile.TransformedInterfaceFileData;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@Service
@AllArgsConstructor
public class AccountNumberTranslationService {
    private final Pattern opalAccountReferencePattern = Pattern.compile("(?<accountReference>([0-9]{8}[a-zA-Z]))");

    private final DefendantAccountRepository defendantAccountRepository;
    private final AlternatePaymentReferenceRepository aprRepository;

    public TransformedInterfaceFileData process(InterfaceFileEntity interfaceFileEntity,
        InterfaceFileCommonDataExtract interfaceFileCommonDataExtract,
        BusinessUnitEntity businessUnit) {
        TransformedInterfaceFileData transformed = new TransformedInterfaceFileData(interfaceFileCommonDataExtract);

        for (Transaction transaction : interfaceFileCommonDataExtract.getTransactions()) {
            if (isCheque(transaction)) {
                transformed.setTotalAmount(transformed.getTotalAmount() + transaction.getAmount());
            } else if (isValidTransaction(transaction)) {
                if (transaction.getTransactionCode().equals("15") || transaction.getTransactionCode().equals("93")) {
                    transaction.setTransactionCode("99");
                }

                String accountRef = transaction.getOriginatorDetails().getAccountReference();
                if (isOpalAccountReference(accountRef)) {
                    if (lookUpOpalAccountFromAccountReference(accountRef, businessUnit.getBusinessUnitId())) {
                        transformed.setTotalAmount(transformed.getTotalAmount() + transaction.getAmount());
                        transformed.addTransaction(transaction);
                        continue;
                    }
                }

                accountRef = extractOpalAccountReference(accountRef);
                if (!accountRef.isBlank()) {
                    if (lookUpOpalAccountFromAccountReference(accountRef, businessUnit.getBusinessUnitId())) {
                        OriginatorDetails details = transaction.getOriginatorDetails();
                        details.setAccountReference(accountRef);
                        transaction.setOriginatorDetails(details);

                        transformed.setTotalAmount(transformed.getTotalAmount() + transaction.getAmount());
                        transformed.addTransaction(transaction);
                        continue;
                    }
                }

                // Check APR
                var apr = lookUpAPR(transaction.getOriginatorDetails().getAccountReference());
                if (apr.isPresent()) {
                    OriginatorDetails originatorDetails = transaction.getOriginatorDetails();
                    originatorDetails.setAccountReference(String.format("%d", apr.get().getDefendantAccountId()));
                    transaction.setOriginatorDetails(originatorDetails);

                    if (!apr.get().getBusinessUnitCode().equals(businessUnit.getBusinessUnitCode())) {
                        DestinationDetails destDetails = transformed.getDestinationDetails();
                        BankDetails bankDetails = destDetails.getBankDetails();
                        bankDetails.setAccountNumber(null);
                        bankDetails.setSortCode(null);
                        destDetails.setBankDetails(bankDetails);
                        transformed.setDestinationDetails(destDetails);
                    }

                    transformed.setTotalAmount(transformed.getTotalAmount() + transaction.getAmount());
                    transformed.addTransaction(transaction);
                    continue;
                }

                // repeat with originator name
                apr = lookUpAPR(transaction.getOriginatorDetails().getName());
                if (apr.isPresent()) {
                    OriginatorDetails originatorDetails = transaction.getOriginatorDetails();
                    originatorDetails.setAccountReference(String.format("%d", apr.get().getDefendantAccountId()));
                    transaction.setOriginatorDetails(originatorDetails);

                    if (!apr.get().getBusinessUnitCode().equals(businessUnit.getBusinessUnitCode())) {
                        DestinationDetails destDetails = transformed.getDestinationDetails();
                        BankDetails bankDetails = destDetails.getBankDetails();
                        bankDetails.setAccountNumber(null);
                        bankDetails.setSortCode(null);
                        destDetails.setBankDetails(bankDetails);
                        transformed.setDestinationDetails(destDetails);
                    }

                    transformed.setTotalAmount(transformed.getTotalAmount() + transaction.getAmount());
                    transformed.addTransaction(transaction);
                }

            }
        }
        interfaceFileEntity.setRecordCount((short)transformed.getTransactions().size());

        return transformed;
    }

    private boolean isCheque(Transaction transaction) {
        return transaction.getTransactionCode().equals("11");
    }

    private boolean isValidTransaction(Transaction transaction) {
        return Set.of("00", "15", "68", "93", "99").contains(transaction.getTransactionCode());
    }

    private boolean isOpalAccountReference(String opalAccountId) {
        return opalAccountReferencePattern.matcher(opalAccountId).matches() && opalAccountId.length() == 9;
    }

    private String extractOpalAccountReference(String fullAccountReference) {
        Matcher matcher = opalAccountReferencePattern.matcher(fullAccountReference);
        if (matcher.find()) {
            return matcher.group("accountReference");
        }
        return "";
    }

    private boolean lookUpOpalAccountFromAccountReference(String accountReference, short businessUnitId) {
        return defendantAccountRepository.findByAccountNumberAndBusinessUnitId(
            accountReference,
            businessUnitId
        ).isPresent();
    }

    private Optional<AlternatePaymentReferenceEntity> lookUpAPR(String accountReference) {
        return aprRepository.findByAprText(accountReference);
    }
}
