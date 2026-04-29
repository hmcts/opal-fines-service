package uk.gov.hmcts.opal.mapper.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

/**
 * Helper component for JSON parsing operations in MapStruct mappers. Using constructor injection to avoid Sonar
 * warnings about field injection.
 *
 * @author Krishna Sapkota
 */
@Component
@RequiredArgsConstructor
public class JsonMapperHelper {

    private final ObjectMapper objectMapper;

    /**
     * Parse JSON string to Map. Returns null for null, empty, or empty JSON object strings.
     */
    @SuppressWarnings("java:S1168")
    @Named("parseJsonToMap")
    public Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}

