package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;
import uk.gov.hmcts.opal.exception.SchemaConfigurationException;

import java.io.IOException;
import java.nio.charset.Charset;
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

    private static final Map<String, JsonSchema> schemaCache = HashMap.newHashMap(37);

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
        JsonSchema jsonSchema = getJsonSchema(jsonSchemaFileName);
        try {
            Set<ValidationMessage> msgs =  jsonSchema.validate(getJsonNodeFromStringContent(body));
            return msgs.stream().map(ValidationMessage::getMessage).collect(Collectors.toSet());
        } catch (JsonSchemaValidationException jsve) {
            return Set.of(jsve.getMessage());
        }
    }

    private JsonNode getJsonNodeFromStringContent(String content) {
        try {
            return ToJsonString.getObjectMapper().readTree(content);
        } catch (JsonProcessingException e) {
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

    private JsonSchema getJsonSchema(String schemaFileName) {
        if (schemaCache.containsKey(schemaFileName)) {
            return schemaCache.get(schemaFileName);
        }
        String fileContents = readJsonSchemaFileContents(schemaFileName);
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(fileContents);
        schemaCache.put(schemaFileName, jsonSchema);
        return jsonSchema;
    }

    private String readJsonSchemaFileContents(String schemaFileName) {
        if (schemaFileName.isBlank()) {
            throw new SchemaConfigurationException("A schema filename is required to validate a JSON document.");
        }
        String filePath = Path.of(PATH_ROOT, schemaFileName).toString();
        ClassPathResource cpr = new ClassPathResource(filePath);
        if (!cpr.exists()) {
            throw new SchemaConfigurationException(format("No JSON Schema file found at '%s'", cpr.getPath()));
        }
        try {
            return StreamUtils.copyToString(cpr.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new SchemaConfigurationException(format("Problem opening InputStream at '%s'", filePath), e);
        }
    }

}
