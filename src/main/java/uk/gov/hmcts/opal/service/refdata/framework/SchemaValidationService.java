package uk.gov.hmcts.opal.service.refdata.framework;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j(topic = "opal.SchemaValidationService")
public class SchemaValidationService {

    private static final String PATH_ROOT = "jsonSchemas";

    private static final Map<String, JsonSchema> schemaCache = HashMap.newHashMap(37);

    public boolean isValid(JsonNode jsonNode, String jsonSchemaFileName) {
        return validate(jsonNode, jsonSchemaFileName).isEmpty();
    }

    public void validateOrError(JsonNode jsonNode, String jsonSchemaFileName) {
        Set<String> errors = validate(jsonNode, jsonSchemaFileName);
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder(errors.size() >> 7);
            sb.append("Validating against JSON schema '")
                .append(jsonSchemaFileName)
                .append("', found ")
                .append(errors.size())
                .append(" validation errors:");
            for (String msg : errors) {
                sb.append("\n\t").append(msg);
            }
            appendContent(sb, jsonNode.toString());
            throw new JsonSchemaValidationException(sb.toString());
        }
    }

    public Set<String> validate(JsonNode jsonNode, String jsonSchemaFileName) {
        JsonSchema jsonSchema = getJsonSchema(jsonSchemaFileName);
        Set<ValidationMessage> msgs = jsonSchema.validate(jsonNode.toString(), InputFormat.JSON);
        if (!msgs.isEmpty()) {
            log.error(":isValid: for JSON schema '{}', found {} validation errors.", jsonSchemaFileName,
                msgs.size());
            for (ValidationMessage msg : msgs) {
                log.error(":isValid: error: {}", msg.getMessage());
            }
        }
        return msgs.stream().map(ValidationMessage::getMessage).collect(Collectors.toSet());
    }

    private void appendContent(StringBuilder sb, String content) {
        sb.append("\n\tContent to validate:\n\"\"\"\n")
            .append(content)
            .append("\n\"\"\"");
    }

    private JsonSchema getJsonSchema(String schemaFileName) {
        if (schemaFileName.isBlank()) {
            throw new SchemaConfigurationException("A schema filename is required to validate a JSON document.");
        }

        if (schemaCache.containsKey(schemaFileName)) {
            return schemaCache.get(schemaFileName);
        }

        String filePath = Path.of(PATH_ROOT, schemaFileName).toString();
        ClassPathResource cpr = new ClassPathResource(filePath);

        if (!cpr.exists()) {
            throw new SchemaConfigurationException(format("No JSON Schema file found at '%s'", cpr.getPath()));
        }

        try {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
            JsonSchema jsonSchema = factory.getSchema(cpr.getURI());

            schemaCache.put(schemaFileName, jsonSchema);
            return jsonSchema;
        } catch (IOException e) {
            throw new SchemaConfigurationException(
                format("Problem reading JSON Schema from '%s'", filePath), e);
        }
    }
}
