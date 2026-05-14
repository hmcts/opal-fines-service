package uk.gov.hmcts.opal.mapper.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JsonMapper {
    ObjectMapper objectMapper = new ObjectMapper();

    @Named("jsonStringToMap")
    default Map<String, Object> jsonStringToMap(String jsonString) throws JsonProcessingException {
        if (jsonString == null) {
            return null;
        }
        return objectMapper.readValue(jsonString, new TypeReference<>() {});
    }

    @Named("mapToJsonString")
    default String mapToJsonString(Map<String, String> map) throws JsonProcessingException {
        if (map == null) {
            return null;
        }
        return objectMapper.writeValueAsString(map);
    }
}
