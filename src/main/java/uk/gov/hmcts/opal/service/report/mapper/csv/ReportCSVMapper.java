package uk.gov.hmcts.opal.service.report.mapper.csv;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;

/**
 * Interface to allow different reports to be generated into CSV data. Add the implementation to the configuration
 * class here {@link uk.gov.hmcts.opal.config.ReportConversionConfiguration#reportToCSVStringMapperMap}.
 *
 * @param <RDIT> The ReportDataInterface type
 */
public interface ReportCSVMapper<RDIT extends ReportDataInterface> {
    String EMPTY_VALUE = "";
    String NEW_ROW_DELIMITER = "\n";

    String reportToCSVString(RDIT rdi);

    default String dataListToFullCSVRow(List<String> dataRow) {
        return dataRow.stream().map(this::checkAndConvertSpecialCharacters).collect(Collectors.joining(","))
            + NEW_ROW_DELIMITER;
    }

    default String checkAndConvertSpecialCharacters(String value) {
        if (value.contains(",")) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }

    default String getDataValue(Object value) {
        return value != null ? value.toString() : EMPTY_VALUE;
    }
}
