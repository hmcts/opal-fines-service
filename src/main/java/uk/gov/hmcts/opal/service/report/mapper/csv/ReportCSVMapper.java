package uk.gov.hmcts.opal.service.report.mapper.csv;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.DOUBLE_QUOTE;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.NEW_LINE;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.opal.service.report.CommonReportHelper;
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
        return dataRow.stream().map(CommonReportHelper::escapeCsv).collect(Collectors.joining(COMMA))
            + NEW_LINE;
    }
}
