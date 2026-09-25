package uk.gov.hmcts.opal.service.opal.accountnumbertranslation;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.OriginatorDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.Transaction;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;

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
            if (transaction.isCheque()) {
                transformed.addTransaction(transaction);
            } else if (transaction.isValidTransaction()) {
                if (transaction.getTransactionCode().equals("15") || transaction.getTransactionCode().equals("93")) {
                    transaction.setTransactionCode("99");
                }

                String accountRef = transaction.getOriginatorDetails().getAccountReference();
                if (isOpalAccountReference(accountRef)) {
                    if (lookUpOpalAccountFromAccountReference(accountRef, businessUnit.getBusinessUnitId())) {
                        transformed.addTransaction(transaction);
                        continue;
                    }
                }

                accountRef = extractOpalAccountReference(accountRef);
                if (!accountRef.isBlank()) {
                    if (lookUpOpalAccountFromAccountReference(accountRef, businessUnit.getBusinessUnitId())) {
                        replaceAccountReference(transaction, accountRef);

                        transformed.addTransaction(transaction);
                        continue;
                    }
                }

                var apr = lookUpAPR(transaction.getOriginatorDetails().getAccountReference(),
                    businessUnit.getBusinessUnitCode());
                if (apr.isPresent()) {
                    String newReference = String.format("%d", apr.get().getDefendantAccount().getDefendantAccountId());
                    replaceAccountReference(transaction, newReference);

                    if (!apr.get().getDefendantAccount().getBusinessUnit().getBusinessUnitId().equals(
                        businessUnit.getBusinessUnitId())) {
                        removeBankDetails(transformed);
                    }

                    transformed.addTransaction(transaction);
                    continue;
                }

                apr = lookUpAPR(transaction.getOriginatorDetails().getName(), businessUnit.getBusinessUnitCode());
                if (apr.isPresent()) {
                    String newReference = String.format("%d", apr.get().getDefendantAccount().getDefendantAccountId());
                    replaceAccountReference(transaction, newReference);

                    if (!apr.get().getDefendantAccount().getBusinessUnit().getBusinessUnitId().equals(
                        businessUnit.getBusinessUnitId())) {
                        removeBankDetails(transformed);
                    }

                    transformed.addTransaction(transaction);
                }

            }
        }

        return transformed;
    }

    private void removeBankDetails(TransformedInterfaceFileData transformedInterfaceFileData) {
        DestinationDetails destDetails = transformedInterfaceFileData.getDestinationDetails();
        BankDetails bankDetails = destDetails.getBankDetails();
        bankDetails.setAccountNumber(null);
        bankDetails.setSortCode(null);
        destDetails.setBankDetails(bankDetails);
        transformedInterfaceFileData.setDestinationDetails(destDetails);
    }

    private void replaceAccountReference(Transaction transaction, String acctRef) {
        OriginatorDetails originatorDetails = transaction.getOriginatorDetails();
        originatorDetails.setAccountReference(acctRef);
        transaction.setOriginatorDetails(originatorDetails);
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
        return defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            accountReference,
            businessUnitId
        ).isPresent();
    }

    private Optional<AlternatePaymentReferenceEntity> lookUpAPR(String accountReference, String buCode) {
        return aprRepository.findByAprTextAndBusinessUnitCode(accountReference, buCode);
    }
}
