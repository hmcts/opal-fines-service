package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

class MinorCreditorSearchRequestValidatorTest {

    private MinorCreditorSearchRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MinorCreditorSearchRequestValidator(new JsonSchemaValidationService());
    }

    @Test
    void validateAndCheckFeature_validatesSchemaWithOnlyAccountNumber_shouldPass() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .accountNumber("AC123")
            .build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));
    }

    @Test
    void validateAndCheckFeature_validatesSchemaWithAccountNumberAndCreditor_shouldFail() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .accountNumber("AC123")
            .creditor(Creditor.builder().build())
            .build();

        assertThrows(JsonSchemaValidationException.class, () -> validator.validateAndCheckFeature(request));
    }

    @Test
    void validateAndCheckFeature_validatesSchemaWithForenamesOnly_shouldFail() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .forenames("John")
                .exactMatchForenames(false)
                .organisation(false)
                .build())
            .build();

        assertThrows(JsonSchemaValidationException.class, () -> validator.validateAndCheckFeature(request));
    }

    @Test
    void validateAndCheckFeature_validatesSchemaWithSurnameOnly_shouldPass() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .surname("Doe")
                .organisation(false)
                .build())
            .build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));
    }

    @Test
    void validateAndCheckFeature_validatesSchemaWithSurnameAndForenames_shouldPass() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .forenames("John")
                .surname("Doe")
                .exactMatchSurname(true)
                .exactMatchForenames(true)
                .organisation(false)
                .build())
            .build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));
    }
    
    @Test
    void validateAndCheckFeature_validatesSchemaWithSurnameAndAddressAndPostcode_shouldPass() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .addressLine1("123 Fake St")
                .postcode("AB12 3CD")
                .surname("doe")
                .exactMatchSurname(false)
                .organisation(false)
                .build())
            .build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));
    }

    @Test
    void validateAndCheckFeature_validatesSchemaForOrganisation_shouldPass() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .activeAccountsOnly(true)
            .creditor(Creditor.builder()
                .addressLine1("Acme House")
                .postcode("MA4 1AL")
                .organisationName("Acme Supplies Ltd")
                .exactMatchOrganisationName(true)
                .organisation(true)
                .build())
            .build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));
    }
}
