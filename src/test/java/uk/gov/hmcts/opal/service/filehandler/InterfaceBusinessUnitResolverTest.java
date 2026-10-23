package uk.gov.hmcts.opal.service.filehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.configurationitem.ConfigurationItemEntity;
import uk.gov.hmcts.opal.repository.ConfigurationItemRepository;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;

@ExtendWith(MockitoExtension.class)
class InterfaceBusinessUnitResolverTest {

    @Mock
    private ConfigurationItemRepository configurationItemRepository;

    @InjectMocks
    private InterfaceBusinessUnitResolver resolver;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldResolveBusinessUnitForMatchingBankAccount() throws Exception {

        BankDetails bankDetails = mock(BankDetails.class);
        when(bankDetails.getSortCode()).thenReturn("112233");
        when(bankDetails.getAccountNumber()).thenReturn("12345678");

        DestinationDetails destinationDetails = mock(DestinationDetails.class);
        when(destinationDetails.getBankDetails())
            .thenReturn(bankDetails);

        InterfaceFileCommonDataExtract extract =  mock(InterfaceFileCommonDataExtract.class);
        when(extract.getDestinationDetails()).thenReturn(destinationDetails);

        BusinessUnitEntity businessUnit = mock(BusinessUnitEntity.class);

        String content = """
            [
              {
                "sort_code":"112233",
                "account_number":"12345678"
              }
            ]
            """;
        JsonNode itemValues = objectMapper.readTree(content);

        ConfigurationItemEntity configurationItem = mock(ConfigurationItemEntity.class);

        when(configurationItem.getItemValues()).thenReturn(itemValues);

        when(configurationItem.getBusinessUnit()).thenReturn(businessUnit);

        when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull("BANK_ACCOUNTS"))
            .thenReturn(Optional.of(configurationItem));
        BusinessUnitEntity result = resolver.resolve(extract);

        assertThat(result).isSameAs(businessUnit);
    }

    @Test
    void shouldThrowWhenNoMatchingBusinessUnitFound() throws Exception {

        BankDetails bankDetails = mock(BankDetails.class);

        lenient().when(bankDetails.getSortCode()).thenReturn("999999");

        lenient().when(bankDetails.getAccountNumber()).thenReturn("99999999");

        DestinationDetails destinationDetails = mock(DestinationDetails.class);

        lenient().when(destinationDetails.getBankDetails()).thenReturn(bankDetails);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        when(extract.getDestinationDetails()).thenReturn(destinationDetails);

        String content = """
            [
              {
                "sort_code":"112233",
                "account_number":"12345678"
              }
            ]
            """;
        JsonNode itemValues = objectMapper.readTree(content);

        ConfigurationItemEntity configurationItem = mock(ConfigurationItemEntity.class);

        lenient().when(configurationItem.getItemValues()).thenReturn(itemValues);

        when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull("BANK_ACCOUNTS"))
            .thenReturn(Optional.of(configurationItem));

        assertThatThrownBy(() -> resolver.resolve(extract))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No business unit found for destination bank account");

    }

    @Test
    void shouldThrowWhenItemValuesAreNull() {

        BankDetails bankDetails = mock(BankDetails.class);

        DestinationDetails destinationDetails = mock(DestinationDetails.class);
        when(destinationDetails.getBankDetails()).thenReturn(bankDetails);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);
        when(extract.getDestinationDetails()).thenReturn(destinationDetails);

        ConfigurationItemEntity configurationItem = mock(ConfigurationItemEntity.class);

        when(configurationItem.getItemValues()).thenReturn(null);

        when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull("BANK_ACCOUNTS"))
            .thenReturn(Optional.of(configurationItem));

        assertThatThrownBy(() -> resolver.resolve(extract))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No business unit found for destination bank account");
    }

    @Test
    void shouldThrowWhenBankDetailsAreNull() {

        DestinationDetails destinationDetails = mock(DestinationDetails.class);

        when(destinationDetails.getBankDetails()).thenReturn(null);

        InterfaceFileCommonDataExtract extract = mock(InterfaceFileCommonDataExtract.class);

        when(extract.getDestinationDetails()).thenReturn(destinationDetails);

        when(configurationItemRepository.findByItemNameAndBusinessUnitIdIsNull("BANK_ACCOUNTS"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.resolve(extract))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "No business unit found for destination bank account");

    }
}