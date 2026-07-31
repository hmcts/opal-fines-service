package uk.gov.hmcts.opal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.opal.SchemaPaths.POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

@ExtendWith(MockitoExtension.class)
class MinorCreditorSearchRequestValidatorTest {

    @Mock
    private JsonSchemaValidationService jsonSchemaValidationService;

    private MinorCreditorSearchRequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MinorCreditorSearchRequestValidator(jsonSchemaValidationService);
    }

    @Test
    void validateAndCheckFeature_() {
        MinorCreditorSearch request = MinorCreditorSearch.builder().activeAccountsOnly(true).accountNumber("AC123").build();

        assertDoesNotThrow(() -> validator.validateAndCheckFeature(request));

        verify(jsonSchemaValidationService).validateOrError(
            contains("{\"active_accounts_only\":true,\"account_number\":\"AC123\"}"),
            eq(POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST)
        );
    }

    @Test
    void validateAndCheckFeature() {
        MinorCreditorSearch request = MinorCreditorSearch.builder()
            .businessUnitIds(List.of((short) 78))
            .activeAccountsOnly(true)
            .accountNumber("AC123")
            .creditor(Creditor.builder()
                .addressLine1("123 Fake Street")
                .postcode("AB1 2CD")
                .organisationName("The Organisation")
                .exactMatchOrganisationName(false)
                .forenames("John")
                .surname("Doe")
                .exactMatchSurname(false)
                .exactMatchForenames(false)
                .organisation(false)
                .build())
            .build();

        JsonSchemaValidationException exception = assertThrows(JsonSchemaValidationException.class,
            () -> validator.validateAndCheckFeature(request));
        assertTrue(exception.getMessage().contains("hkudv"));

        verify(jsonSchemaValidationService).validateOrError(
            contains("""
                "{
                    "business_unit_ids":[78],
                    "active_accounts_only":true,
                    "account_number":"AC123",
                    "creditor":{
                        "address_line_1":"123 Fake Street",
                        "postcode":"AB1 2CD",
                        "organisation_name":"The Organisation",
                        "exact_match_organisation_name":false,
                        "forenames":"John",
                        "surname":"Doe",
                        "exact_match_surname":false,
                        "exact_match_forenames":false,
                        "organisation":false
                    }
                }"
                """),
            eq(POST_MINOR_CREDITOR_ACCOUNTS_SEARCH_REQUEST)
        );
    }
}