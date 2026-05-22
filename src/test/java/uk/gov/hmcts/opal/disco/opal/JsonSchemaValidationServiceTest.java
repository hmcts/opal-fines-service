package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JsonSchemaValidationServiceTest {

    @InjectMocks
    private JsonSchemaValidationService jsonSchemaValidationService;

    @Test
    void testIsValid_failBlankSchema() {
        // Act
        SchemaConfigurationException sce = assertThrows(
            SchemaConfigurationException.class,
            () -> jsonSchemaValidationService.isValid("", " ")
        );

        // Assert
        assertEquals("A schema filename is required to validate a JSON document.",
                     sce.getMessage());
    }

    @Test
    void testIsValid_failLoadSchema() {
        // Act
        SchemaConfigurationException sce = assertThrows(
            SchemaConfigurationException.class,
            () -> jsonSchemaValidationService.isValid("", "nonExistentSchema.json")
        );

        // Assert
        assertEquals("No JSON Schema file found at 'jsonSchemas/nonExistentSchema.json'",
                     sce.getMessage());
    }

    @Test
    void testIsValid_failIsValid() {
        assertFalse(jsonSchemaValidationService.isValid("", "testSchema.json"));
    }

    @Test
    void testIsValid_failValidate1() {
        Set<String> messages = jsonSchemaValidationService
            .validate("", "testSchema.json");
        assertEquals(1, messages.size());
        assertEquals("$: unknown found, object expected", messages
            .stream()
            .findFirst()
            .orElse(""));
    }

    @Test
    void testIsValid_failValidate2() {
        Set<String> messages = jsonSchemaValidationService
            .validate("{\"data\": 7}", "testSchema.json");
        assertEquals(4, messages.size());
    }

    @Test
    void testIsValid_failValidate3() {
        Set<String> messages = jsonSchemaValidationService
            .validate("Not valid JSON", "testSchema.json");
        assertEquals(1, messages.size());
        String msg = messages.stream().findFirst().orElse("");
        assertTrue(msg.startsWith("Unrecognized token 'Not': was expecting (JSON String, Number, Array, Object "));
    }

    @Test
    void testIsValid_failValidateOrError1() {
        // Act
        JsonSchemaValidationException sce = assertThrows(
            JsonSchemaValidationException.class,
            () -> jsonSchemaValidationService.validateOrError("Not Valid JSON", "testSchema.json")
        );

        // Assert
        assertTrue(sce.getMessage().startsWith("Validating against JSON schema 'testSchema.json', found 1 validation"));
    }

    @Test
    void testIsValid_failValidateOrError2() {
        // Act
        JsonSchemaValidationException sce = assertThrows(
            JsonSchemaValidationException.class,
            () -> jsonSchemaValidationService.validateOrError("{\"name\": 5}", "testSchema.json")
        );

        // Assert
        assertTrue(sce.getMessage().startsWith("Validating against JSON schema 'testSchema.json', found 4 validation"));
    }

    @Test
    void testIsValid_validDateTimeAndEmail_shouldPass() {
        String validJson = """
        {
          "submitted_at": "2025-06-09T14:00:00Z",
          "submitted_by_email": "john.doe@example.com"
        }
            """;

        Set<String> messages = jsonSchemaValidationService
            .validate(validJson, "formatValidationSchema.json");

        assertTrue(messages.isEmpty(), "Expected no validation errors, but got: " + messages);
    }

    @Test
    void testIsValid_invalidDateTimeAndEmail_shouldFail() {
        String invalidJson = """
        {
          "submitted_at": "09/06/2025 2PM",
          "submitted_by_email": "not-an-email"
        }
            """;

        Set<String> messages = jsonSchemaValidationService
            .validate(invalidJson, "formatValidationSchema.json");

        assertFalse(messages.isEmpty(), "Expected validation errors but got none.");
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("submitted_at")),
            "Expected error about 'submitted_at'");
        assertTrue(messages.stream().anyMatch(msg -> msg.contains("submitted_by_email")),
            "Expected error about 'submitted_by_email'");
    }

    @Test
    void testIsValid_defendantAccountSearchRequestWithConsolidationSearch_shouldPass() {
        String validJson = """
            {
              "active_accounts_only": true,
              "business_unit_ids": [78],
              "reference_number": null,
              "defendant": {
                "include_aliases": true,
                "organisation": false,
                "address_line_1": "Lumber",
                "postcode": "MA4 1AL",
                "organisation_name": null,
                "exact_match_organisation_name": null,
                "surname": "Graham",
                "exact_match_surname": true,
                "forenames": "Anna",
                "exact_match_forenames": true,
                "birth_date": "1980-02-03",
                "national_insurance_number": null
              },
              "consolidation_search": true
            }
            """;

        Set<String> messages = jsonSchemaValidationService
            .validate(validJson, "opal/defendant-account/postDefendantAccountsSearchRequest.json");

        assertTrue(messages.isEmpty(), "Expected no validation errors, but got: " + messages);
    }

    @Test
    void testIsValid_defendantAccountSearchResponseWithConsolidationFields_shouldPass() {
        String validJson = """
            {
              "count": 1,
              "defendant_accounts": [
                {
                  "defendant_account_id": "77",
                  "account_number": "177A",
                  "organisation": false,
                  "aliases": [],
                  "address_line_1": "Lumber House",
                  "postcode": "MA4 1AL",
                  "business_unit_name": "Business Unit",
                  "business_unit_id": "78",
                  "prosecutor_case_reference": null,
                  "last_enforcement_action": null,
                  "account_balance": 123.45,
                  "organisation_name": null,
                  "defendant_title": "Mr",
                  "defendant_firstnames": "Anna",
                  "defendant_surname": "Graham",
                  "birth_date": "1980-02-03",
                  "national_insurance_number": null,
                  "parent_guardian_surname": null,
                  "parent_guardian_firstnames": null,
                  "has_collection_order": true,
                  "account_version": 0,
                  "checks": {
                    "warnings": [],
                    "errors": [
                      {
                        "reference": "CON.ER.4",
                        "message": "Account has days in default"
                      }
                    ]
                  }
                }
              ]
            }
            """;

        Set<String> messages = jsonSchemaValidationService
            .validate(validJson, "opal/defendant-account/postDefendantAccountsSearchResponse.json");

        assertTrue(messages.isEmpty(), "Expected no validation errors, but got: " + messages);
    }

}
