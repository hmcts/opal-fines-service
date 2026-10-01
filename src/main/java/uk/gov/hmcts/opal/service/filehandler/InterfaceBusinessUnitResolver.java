package uk.gov.hmcts.opal.service.filehandler;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;

@Component
@RequiredArgsConstructor
public class InterfaceBusinessUnitResolver {

    private static final String BANK_ACCOUNTS = "BANK_ACCOUNTS";
    private static final String SORT_CODE = "sort_code";
    private static final String ACCOUNT_NUMBER = "account_number";

    private final ConfigurationItemRepository configurationItemRepository;

    public BusinessUnitEntity resolve(InterfaceFileCommonDataExtract sourceData) {
        BankDetails bankDetails = sourceData.getDestinationDetails().getBankDetails();

        return configurationItemRepository.findByItemName(BANK_ACCOUNTS).stream()
            .filter(configurationItem -> matchesBankAccount(configurationItem.getItemValues(), bankDetails))
            .map(ConfigurationItemEntity::getBusinessUnit)
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No business unit found for destination bank account"));
    }

    private boolean matchesBankAccount(JsonNode bankAccounts, BankDetails bankDetails) {
        if (bankAccounts == null || !bankAccounts.isArray() || bankDetails == null) {
            return false;
        }

        return StreamSupport.stream(bankAccounts.spliterator(), false)
            .anyMatch(bankAccount -> Objects.equals(
                bankDetails.getSortCode(), bankAccount.path(SORT_CODE).asText())
                && Objects.equals(bankDetails.getAccountNumber(), bankAccount.path(ACCOUNT_NUMBER).asText()));
    }
}
