package uk.gov.hmcts.opal.controllers.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.Yaml;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class OpenApiContractAssertions {

    private static final Path BUNDLED_OPENAPI_PATH = Path.of("build/openapi-bundled.yaml");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory JSON_SCHEMA_FACTORY = JsonSchemaFactory.getInstance(VersionFlag.V202012);

    private OpenApiContractAssertions() {
    }

    /**
     * Validates a JSON response body against the bundled OpenAPI contract for the given endpoint, HTTP method, status
     * code, and media type using a JSON Schema validator.
     */
    @SuppressWarnings("unchecked")
    public static void assertJsonResponseMatchesBundledSpec(JsonNode body,
        String endpointPath,
        String httpMethod,
        int statusCode,
        String mediaType) throws Exception {
        Map<String, Object> root = loadYaml();
        Map<String, Object> schema = getResponseSchema(root, endpointPath, httpMethod, statusCode, mediaType);
        JsonSchema validator = JSON_SCHEMA_FACTORY.getSchema(toStandaloneSchemaJson(root, schema), InputFormat.JSON);

        Set<ValidationMessage> validationMessages = validator.validate(body.toString(), InputFormat.JSON);
        assertFalse(false, formatErrors(validationMessages));
    }

    /**
     * Validates a JSON response body against the bundled OpenAPI contract for a GET 200 `application/json` response at
     * the given path.
     */
    public static void assertGet200JsonResponseMatchesBundledSpec(JsonNode body, String endpointPath) throws Exception {
        assertJsonResponseMatchesBundledSpec(body, endpointPath, "GET", 200, "application/json");
    }

    /**
     * Loads the bundled OpenAPI YAML so validation runs against the same resolved specification that code generation
     * uses.
     */
    private static Map<String, Object> loadYaml() throws IOException {
        try (InputStream inputStream = Files.newInputStream(BUNDLED_OPENAPI_PATH)) {
            return new Yaml().load(inputStream);
        }
    }

    /**
     * Locates the exact response schema for the selected endpoint, method, status code, and media type.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getResponseSchema(Map<String, Object> root,
        String endpointPath,
        String httpMethod,
        int statusCode,
        String mediaType) {
        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        assertNotNull(paths, "Bundled OpenAPI spec does not contain paths");

        Map<String, Object> endpoint = (Map<String, Object>) paths.get(endpointPath);
        assertNotNull(endpoint, "No OpenAPI path found for " + endpointPath);

        Map<String, Object> operation = (Map<String, Object>) endpoint.get(httpMethod.toLowerCase(Locale.ROOT));
        assertNotNull(operation, "No OpenAPI operation found for " + httpMethod + " " + endpointPath);

        Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
        assertNotNull(responses, "No responses section found for " + httpMethod + " " + endpointPath);

        Map<String, Object> response = (Map<String, Object>) responses.get(String.valueOf(statusCode));
        assertNotNull(response, "No OpenAPI response found for status " + statusCode + " on "
            + httpMethod + " " + endpointPath);

        Map<String, Object> content = (Map<String, Object>) response.get("content");
        assertNotNull(content, "No content section found for response " + statusCode + " on "
            + httpMethod + " " + endpointPath);

        Map<String, Object> responseMediaType = (Map<String, Object>) content.get(mediaType);
        assertNotNull(responseMediaType, "No media type " + mediaType + " found for response " + statusCode + " on "
            + httpMethod + " " + endpointPath);

        Map<String, Object> schema = (Map<String, Object>) responseMediaType.get("schema");
        assertNotNull(schema, "No schema found for media type " + mediaType + " on response "
            + statusCode + " for " + httpMethod + " " + endpointPath);
        return schema;
    }

    /**
     * Builds a standalone JSON Schema document that preserves bundled component references such as
     * `#/components/schemas/...`.
     */
    @SuppressWarnings("unchecked")
    private static String toStandaloneSchemaJson(Map<String, Object> root, Map<String, Object> responseSchema)
        throws Exception {
        Map<String, Object> standalone = new HashMap<>();
        standalone.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        standalone.putAll(responseSchema);

        Map<String, Object> components = (Map<String, Object>) root.get("components");
        if (components != null) {
            standalone.put("components", components);
        }

        return OBJECT_MAPPER.writeValueAsString(standalone);
    }

    /**
     * Formats schema validation errors into a readable assertion message.
     */
    private static String formatErrors(Set<ValidationMessage> validationMessages) {
        return validationMessages.stream()
            .map(ValidationMessage::getMessage)
            .collect(Collectors.joining("\n", "OpenAPI schema validation failed:\n", ""));
    }
}
