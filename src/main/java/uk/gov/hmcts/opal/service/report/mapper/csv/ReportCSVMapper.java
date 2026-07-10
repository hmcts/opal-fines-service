package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.opal.service.report.ReportCSVMapperRegistry;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;

/**
 * Interface to allow different reports to be generated into CSV data. These are automatically added into the container
 * via {@link ReportCSVMapperRegistry}
 *
 * @param <RDIT> The ReportDataInterface type
 */
public interface ReportCSVMapper<RDIT extends ReportDataInterface> {

    Class<RDIT> getReportDataType();

    String reportToCSVString(RDIT rdi);

    default String dataListToFullCSVRow(List<String> dataRow) {
        return dataRow.stream().map(this::checkAndConvertSpecialCharacters).collect(Collectors.joining(COMMA))
            + NEW_LINE;
    }

    default String checkAndConvertSpecialCharacters(String value) {
        if (value.contains(",")) {
            return "\"" + value + "\"";
        } else {
            return value;
        }
    }

    default String getDataValue(Object value) {
        return value != null ? value.toString() : EMPTY_STRING;
    }
}
