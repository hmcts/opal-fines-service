package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.time.LocalDate;
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
public class ReportParameterService {

    private final ObjectMapper mapper;

    public boolean validateReportInstanceParameterValues(Map<String, Object> reportInstanceParameters, ReportEntity report) {

        List<ReportParameterData> reportParameterDataList = report.getReportParameters() == null ? List.of() : report.getReportParameters();

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
                            report.getReportId())));
                //see if given value for
                switch (ReportParameterType.fromParameterName(reportParameterData.type())) {
                    case DATE -> {
                        if (reportInstanceParameters.get(parameterName) instanceof String dateString) {
                            LocalDate date = LocalDate.parse(dateString);
                            LocalDate min = reportParameterData.min() != null ? LocalDate.parse(
                                reportParameterData.min().toString()) : null;
                            LocalDate max = reportParameterData.max() != null ? LocalDate.parse(
                                reportParameterData.max().toString()) : null;
                            if ((min != null && !min.isAfter(date)) && (max != null && !max.isBefore(date))) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    case DECIMAL -> {
                        if (reportInstanceParameters.get(parameterName) instanceof Double value) {
                            //todo look into 2dp validation, I don't think its really possible to ensure...
                            double min = reportParameterData.min() != null ? (Double) reportParameterData.min()
                                : Double.MIN_VALUE;
                            double max = reportParameterData.max() != null ? (Double) reportParameterData.max()
                                : Double.MAX_VALUE;
                            if (min > value || value > max) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    case INTEGER -> {
                        Long value = convertNumberObjectToLongOrDefaultValue(reportInstanceParameters.get(parameterName), null);
                        if (value != null) {
                            long min = convertNumberObjectToLongOrDefaultValue(reportParameterData.min(), Long.MIN_VALUE);
                            long max = convertNumberObjectToLongOrDefaultValue(reportParameterData.max(), Long.MAX_VALUE);
                            if (min > value || value > max) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    case MENU_RADIO, MENU_CHECKBOX -> {
                        List<String> values = mapper.convertValue(reportInstanceParameters.get(parameterName),
                            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                        int min = reportParameterData.min() != null ? (Integer) reportParameterData.min() : 0;
                        int max = reportParameterData.max() != null ? (Integer) reportParameterData.max() : 1;
                        if ((values.size() > max || values.size() < min) || !new HashSet<>(
                            reportParameterData.options()).containsAll(values)) {
                            return false;
                        }
                    }
                    case MENU_AUTOCOMPLETE -> {
                        /*
                        List<String> values = mapper.convertValue(reportInstanceParameters.get(parameterName),
                            TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
                        int min = reportParameterData.getMin() != null ? (Integer) reportParameterData.getMin() : 0;
                        int max = reportParameterData.getMax() != null ? (Integer) reportParameterData.getMax() : 1;
                        List<String> apidata = new ArrayList<>();//todo apidata? call an endpoint?
                        if ((values.size() > max || values.size() < min) || !new HashSet<>(apidata).containsAll(
                            values)) {
                            return false;
                        }
                        */
                    }
                    case TEXT_MAX_60, TEXT_MAX_100, TEXT_MAX_1000 -> {
                        if (reportInstanceParameters.get(parameterName) instanceof String text) {
                            int min = reportParameterData.min() != null ? (Integer) reportParameterData.min() : 0;
                            int max =
                                reportParameterData.max() != null ? (Integer) reportParameterData.max() : 1000;
                            if (text.length() < min || text.length() > max) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
                //mandatory parameter has been set
                mandatoryReportParameters.remove(parameterName);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Error with validation of report instance parameter values", e);
            return false;
        }

        //check that all mandatory parameters have been set
        return mandatoryReportParameters.isEmpty();
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
