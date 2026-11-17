package uk.gov.hmcts.opal.openapi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLFactory;

public final class OpenApiRequestSchemaExtractor {

    private OpenApiRequestSchemaExtractor() {
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        Map<String, Object> root = yaml.readValue(Path.of(args[0]).toFile(), Map.class);
        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        Map<String, Object> endpoint = (Map<String, Object>) paths.get(args[1]);
        Map<String, Object> operation = (Map<String, Object>) endpoint.get(args[2]);
        Map<String, Object> request = (Map<String, Object>) operation.get("requestBody");
        Map<String, Object> content = (Map<String, Object>) request.get("content");
        Map<String, Object> json = (Map<String, Object>) content.get("application/json");
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        schema.putAll((Map<String, Object>) json.get("schema"));
        schema.put("components", root.get("components"));
        Path output = Path.of(args[3]);
        Files.createDirectories(output.getParent());
        new ObjectMapper().writeValue(output.toFile(), schema);
    }
}
