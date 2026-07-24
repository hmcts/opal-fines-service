package uk.gov.hmcts.opal.service.report;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.ReportParameterService")
public class ReportParameterValidator {

    private final ObjectMapper mapper;

    public boolean validateReportInstanceParameterValues(Map<String, Object> reportInstanceParameters,
                                                         ReportEntity report) {
        List<ReportParameterData> reportParameterDataList = report.getReportParameters() == null
            ? List.of() : report.getReportParameters();
        Set<String> mandatoryReportParameters = reportParameterDataList.stream()
            .filter(ReportParameterData::mandatory)
            .map(ReportParameterData::name)
            .collect(Collectors.toSet());
        if (reportInstanceParameters == null) {
            return mandatoryReportParameters.isEmpty();
        }
        try {
            for (String parameterName : reportInstanceParameters.keySet()) {
                //get report param data for name
                ReportParameterData reportParameterData = reportParameterDataList.stream()
                    .filter(rpd -> parameterName.equals(rpd.name()))
                    .findFirst().orElseThrow(() -> new UnprocessableException(
                        String.format("Report Parameter %s for report %s doesn't exist", parameterName,
                            report.getReportId()), true));
                //see if given value for
                switch (ReportParameterType.fromParameterName(reportParameterData.type())) {
                    case BOOLEAN -> {
                        if (!(reportInstanceParameters.get(parameterName) instanceof Boolean)) {
                            return false;
                        }
                    }
                    case DATE -> {
                        if (reportInstanceParameters.get(parameterName) instanceof String dateString) {
                            if (isInvalidDate(dateString, reportParameterData)) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    case DECIMAL -> {
                        if (reportInstanceParameters.get(parameterName) instanceof Double value) {
                            if (isInvalidDecimal(value, reportParameterData)) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    case INTEGER -> {
                        if (isInvalidInteger(reportInstanceParameters, parameterName, reportParameterData)) {
                            return false;
                        }
                    }
                    case MENU_RADIO, MENU_CHECKBOX -> {
                        if (isInvalidMenu(reportInstanceParameters, parameterName, reportParameterData)) {
                            return false;
                        }
                    }
                    case MENU_AUTOCOMPLETE -> {
                        //TODO future ticket apidata implementation in report parameters
                    }
                    case TEXT_MAX_60 -> {
                        if (isInvalidText(reportInstanceParameters, parameterName, reportParameterData, 60)) {
                            return false;
                        }
                    }
                    case TEXT_MAX_100 -> {
                        if (isInvalidText(reportInstanceParameters, parameterName, reportParameterData, 100)) {
                            return false;
                        }
                    }
                    case TEXT_MAX_1000 -> {
                        if (isInvalidText(reportInstanceParameters, parameterName, reportParameterData, 1000)) {
                            return false;
                        }
                    }
                }
                //mandatory parameter has been set
                mandatoryReportParameters.remove(parameterName);
            }
        } catch (IllegalArgumentException | DateTimeParseException e) {
            log.warn("Error with validation of report instance parameter values", e);
            return false;
        }
        //check that all mandatory parameters have been set
        return mandatoryReportParameters.isEmpty();
    }

    private boolean isInvalidText(Map<String, Object> reportInstanceParameters, String parameterName,
        ReportParameterData reportParameterData, int defaultMaxValue) {
        if (reportInstanceParameters.get(parameterName) instanceof String text) {
            int min = reportParameterData.min() != null ? (Integer) reportParameterData.min() : 0;
            int max = reportParameterData.max() != null ? (Integer) reportParameterData.max() : defaultMaxValue;
            return text.length() < min || text.length() > max;
        } else {
            return true;
        }
    }

    private boolean isInvalidMenu(Map<String, Object> reportInstanceParameters, String parameterName,
        ReportParameterData reportParameterData) {
        List<String> values = mapper.convertValue(reportInstanceParameters.get(parameterName),
            TypeFactory.createDefaultInstance().constructCollectionType(List.class, String.class));
        int min = reportParameterData.min() != null ? (Integer) reportParameterData.min() : 0;
        int max = reportParameterData.max() != null ? (Integer) reportParameterData.max() : 1;
        return (values.size() > max || values.size() < min) || !new HashSet<>(
            reportParameterData.options()).containsAll(values);
    }

    private boolean isInvalidInteger(Map<String, Object> reportInstanceParameters, String parameterName,
        ReportParameterData reportParameterData) {
        Long value = convertNumberObjectToLongOrDefaultValue(reportInstanceParameters.get(parameterName), null);
        if (value != null) {
            long min = convertNumberObjectToLongOrDefaultValue(reportParameterData.min(), Long.MIN_VALUE);
            long max = convertNumberObjectToLongOrDefaultValue(reportParameterData.max(), Long.MAX_VALUE);
            return min > value || value > max;
        } else {
            return true;
        }
    }

    private boolean isInvalidDecimal(Double value, ReportParameterData reportParameterData) {
        double min = reportParameterData.min() != null ? (Double) reportParameterData.min() : Double.MIN_VALUE;
        double max = reportParameterData.max() != null ? (Double) reportParameterData.max() : Double.MAX_VALUE;
        return min > value || value > max;
    }

    private boolean isInvalidDate(String dateString, ReportParameterData reportParameterData) {
        LocalDate date = LocalDate.parse(dateString);
        LocalDate min = reportParameterData.min() != null ? LocalDate.parse(
            reportParameterData.min().toString()) : null;
        LocalDate max = reportParameterData.max() != null ? LocalDate.parse(
            reportParameterData.max().toString()) : null;
        return (min != null && !min.isAfter(date)) && (max != null && !max.isBefore(date));
    }

    private Long convertNumberObjectToLongOrDefaultValue(Object inputNumber, Long defaultValue) {
        if (inputNumber instanceof Integer integer) {
            return integer.longValue();
        } else if (inputNumber instanceof Long l) {
            return l;
        } else {
            return defaultValue;
        }
    }
}
