package uk.gov.hmcts.opal.mapper.helper;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.service.report.ReportParameterData;

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
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON in report_parameters: " + json, e);
        }
    }

    @Named("reportParametersToMap")
    public Map<String, Object> reportParametersToMap(List<ReportParameterData> reportParameterDataList) {
        if (reportParameterDataList == null || reportParameterDataList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String,ReportParameterData> reportParameterDataMap = reportParameterDataList.stream()
            .collect(Collectors.toMap(ReportParameterData::name, Function.identity()));
        return objectMapper.convertValue(reportParameterDataMap, new TypeReference<>() {});
    }
}

