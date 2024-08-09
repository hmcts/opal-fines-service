package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.exception.JsonSchemaValidationException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Set;

import static java.lang.String.format;

@Slf4j(topic = "JsonSchemaValidationService")
@Service
public class JsonSchemaValidationService {

    private static final String PATH_ROOT = "jsonSchemas";

    public boolean isValid(String body, String jsonSchemaFileName) {
        Set<ValidationMessage> errors = validate(body, jsonSchemaFileName);
        if (!errors.isEmpty()) {
            log.error(":isValid: for JSON schema '{}', found {} validation errors.", jsonSchemaFileName, errors.size());
            for (ValidationMessage msg : errors) {
                log.error(":isValid: error: {}", msg.getMessage());
            }
            return false;
        }
        return true;
    }

    public Set<ValidationMessage> validate(String body, String jsonSchemaFileName) {
        String jsonSchemaContents = readJsonSchema(jsonSchemaFileName);
        var jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(jsonSchemaContents);
        return jsonSchema.validate(getJsonNodeFromStringContent(body));
    }

    private JsonNode getJsonNodeFromStringContent(String content) {
        try {
            return ToJsonString.getObjectMapper().readTree(content);
        } catch (JsonProcessingException e) {
            throw new JsonSchemaValidationException(e.getMessage(), e);
        }
    }

    private String readJsonSchema(String schemaFileName) {
        if (schemaFileName.isBlank()) {
            throw new JsonSchemaValidationException("A schema filename is required to validate a JSON document.");
        }
        String filePath = Path.of(PATH_ROOT, schemaFileName).toString();
        ClassPathResource cpr = new ClassPathResource(filePath);
        if (!cpr.exists()) {
            throw new JsonSchemaValidationException(format("No JSON Schema file found at '%s'", cpr.getPath()));
        }
        try {
            return StreamUtils.copyToString(cpr.getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new JsonSchemaValidationException(format("Problem opening InputStream at '%s'", filePath), e);
        }
    }

}
