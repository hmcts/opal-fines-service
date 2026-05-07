package uk.gov.hmcts.opal.mapper.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonMapperHelper {

    private final ObjectMapper objectMapper;

    @Named("parseJsonToMap")
    public Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in report_parameters: " + json, e);
        }
    }
}

