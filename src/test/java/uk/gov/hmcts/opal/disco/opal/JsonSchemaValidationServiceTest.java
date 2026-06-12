package uk.gov.hmcts.opal.disco.opal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;
import uk.gov.hmcts.opal.service.opal.JsonSchemaValidationService;

import java.io.IOException;
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
    void testIsValid_failReadSchema() {
        try (MockedConstruction<ClassPathResource> ignored = Mockito.mockConstruction(ClassPathResource.class,
            (mock, context) -> {
                Mockito.when(mock.exists()).thenReturn(true);
                Mockito.when(mock.getURI()).thenThrow(new IOException("Cannot read schema"));
            })) {

            SchemaConfigurationException sce = assertThrows(
                SchemaConfigurationException.class,
                () -> jsonSchemaValidationService.isValid("{}", "unreadableSchema.json")
            );

            assertEquals("Problem reading JSON Schema from 'jsonSchemas/unreadableSchema.json'", sce.getMessage());
        }
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
    void testIsValid_validBody_shouldReturnTrue() {
        String validJson = """
        {
          "test_long_id": 123456789,
          "test_short_id": 123,
          "test_date_time": "2025-06-09T14:00:00Z",
          "test_text_1": "required"
        }
            """;

        assertTrue(jsonSchemaValidationService.isValid(validJson, "testSchema.json"));
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
    void testIsValid_validJsonNode_shouldReturnTrue() throws Exception {
        JsonNode jsonNode = new ObjectMapper().readTree("""
            {
              "test_long_id": 123456789,
              "test_short_id": 123,
              "test_date_time": "2025-06-09T14:00:00Z",
              "test_text_1": "required"
            }
            """);

        assertTrue(jsonSchemaValidationService.isValid(jsonNode, "testSchema.json"));
    }

    @Test
    void testIsValid_invalidJsonNode_shouldReturnFalse() throws Exception {
        JsonNode jsonNode = new ObjectMapper().readTree("{\"data\":7}");

        assertFalse(jsonSchemaValidationService.isValid(jsonNode, "testSchema.json"));
    }

    @Test
    void testIsValid_jsonNodeValidationException_shouldReturnFalse() {
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        Mockito.when(jsonNode.toString()).thenThrow(new RuntimeException("Cannot serialise node"));

        assertFalse(jsonSchemaValidationService.isValid(jsonNode, "testSchema.json"));
    }

}
