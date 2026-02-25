package uk.gov.hmcts.opal.service.opal;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j(topic = "opal.JsonSchemaValidationService")
@Service
public class JsonSchemaValidationService {

    private static final String PATH_ROOT = "jsonSchemas";

    private static final Map<String, Schema> schemaCache = HashMap.newHashMap(37);
    private static final SchemaRegistry SCHEMA_REGISTRY =
        SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

    public boolean isValid(String body, String jsonSchemaFileName) {
        Set<String> errors = validate(body, jsonSchemaFileName);
        if (!errors.isEmpty()) {
            log.error(":isValid: for JSON schema '{}', found {} validation errors.", jsonSchemaFileName, errors.size());
            for (String msg : errors) {
                log.error(":isValid: error: {}", msg);
            }
            return false;
        }
        return true;
    }

    public boolean isValid(JsonNode jsonNode, String jsonSchemaFileName) {
        Schema jsonSchema = getJsonSchema(jsonSchemaFileName);
        try {
            List<Error> msgs = jsonSchema.validate(jsonNode);
            if (!msgs.isEmpty()) {
                log.error(":isValid: for JSON schema '{}', found {} validation errors.", jsonSchemaFileName,
                    msgs.size());
                for (Error msg : msgs) {
                    log.error(":isValid: error: {}", msg.getMessage());
                }
            }
            return msgs.isEmpty();
        } catch (Exception ex) {
            log.error(":isValid: exception during schema validation", ex);
            return false;
        }
    }

    public void  validateOrError(String body, String jsonSchemaFileName) {
        Set<String> errors = validate(body, jsonSchemaFileName);
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
            appendContent(sb, body);
            throw new JsonSchemaValidationException(sb.toString());
        }
    }

    public Set<String> validate(String body, String jsonSchemaFileName) {
        Schema jsonSchema = getJsonSchema(jsonSchemaFileName);
        try {
            List<Error> msgs =  jsonSchema.validate(getJsonNodeFromStringContent(body));
            return msgs.stream().map(Error::getMessage).collect(Collectors.toSet());
        } catch (JsonSchemaValidationException jsve) {
            return Set.of(jsve.getMessage());
        }
    }

    private JsonNode getJsonNodeFromStringContent(String content) {
        try {
            return ToJsonString.getObjectMapper().readTree(content);
        } catch (JacksonException e) {
            StringBuilder sb = new StringBuilder(e.getMessage().length() + content.length() + 99);
            sb.append(e.getOriginalMessage());
            appendContent(sb, content);
            throw new JsonSchemaValidationException(sb.toString(), e);
        }
    }

    private void appendContent(StringBuilder sb, String content) {
        sb.append("\n\tContent to validate:\n\"\"\"\n")
            .append(content)
            .append("\n\"\"\"");
    }

    private Schema getJsonSchema(String schemaFileName) {
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
            // Key change: Use URL instead of string content
            SchemaLocation schemaLocation = SchemaLocation.of("classpath:" + filePath);
            Schema jsonSchema = SCHEMA_REGISTRY.getSchema(schemaLocation);


            schemaCache.put(schemaFileName, jsonSchema);
            return jsonSchema;
        } catch (Exception e) {
            throw new SchemaConfigurationException(
                format("Problem reading JSON Schema from '%s'", filePath), e);
        }
    }

}
