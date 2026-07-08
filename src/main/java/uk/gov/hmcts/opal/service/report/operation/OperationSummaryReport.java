package uk.gov.hmcts.opal.service.report.operation;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Builder
@Data
public class OperationSummaryReport implements OperationReportDataInterface {

    private SummaryReportDto summaryReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<SummaryOperationReportRowDto> transactionList = summaryReport.getReportSummaryRows();
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}
