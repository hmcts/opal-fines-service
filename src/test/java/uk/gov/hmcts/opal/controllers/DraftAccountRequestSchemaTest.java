package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class DraftAccountRequestSchemaTest {

    private static final Set<String> TOKEN_DERIVED_FIELDS = Set.of(
        "submitted_by",
        "submitted_by_name",
        "validated_by",
        "validated_by_name"
    );
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @ParameterizedTest
    @MethodSource("draftAccountRequestSchemas")
    void draftAccountRequestSchemas_doNotExposeTokenDerivedFields(String schemaPath) throws Exception {
        JsonNode properties = readSchema(schemaPath).get("properties");

        assertNotNull(properties);
        assertAll(TOKEN_DERIVED_FIELDS.stream()
            .map(field -> () -> assertFalse(properties.has(field), schemaPath + " must not expose " + field)));
    }

    private static Stream<String> draftAccountRequestSchemas() {
        return Stream.of(
            "jsonSchemas/opal/draft-account/addDraftAccountRequest.json",
            "jsonSchemas/opal/draft-account/replaceDraftAccountRequest.json",
            "jsonSchemas/opal/draft-account/updateDraftAccountRequest.json"
        );
    }

    private static JsonNode readSchema(String schemaPath) throws Exception {
        try (InputStream inputStream = DraftAccountRequestSchemaTest.class.getClassLoader()
            .getResourceAsStream(schemaPath)) {
            assertNotNull(inputStream, schemaPath + " must exist");
            return OBJECT_MAPPER.readTree(inputStream);
        }
    }
}
