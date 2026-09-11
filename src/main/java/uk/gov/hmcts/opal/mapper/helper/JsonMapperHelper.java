package uk.gov.hmcts.opal.mapper.helper;

import static uk.gov.hmcts.opal.dto.ToJsonString.getObjectMapper;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.service.report.ReportParameterData;

public final class JsonMapperHelper {

    private JsonMapperHelper() {
        // Utility class.
    }

    @Named("parseJsonToMap")
    public static Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return Collections.emptyMap();
        }
        try {
            return getObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid JSON in report_parameters: " + json, e);
        }
    }

    @Named("reportParametersToMap")
    public static Map<String, Object> reportParametersToMap(List<ReportParameterData> reportParameterDataList) {
        if (reportParameterDataList == null || reportParameterDataList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ReportParameterData> reportParameterDataMap = reportParameterDataList.stream()
            .collect(Collectors.toMap(ReportParameterData::name, Function.identity()));
        return getObjectMapper().convertValue(reportParameterDataMap, new TypeReference<>() {});
    }
}
