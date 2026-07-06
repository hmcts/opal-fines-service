package uk.gov.hmcts.opal.service.report.operation;

import java.util.List;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Data
public class OperationByEnforcementSummaryReport implements ReportDataInterface {

    private SummaryReportDto enforcementReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<SummaryOperationReportRowDto> transactionList = enforcementReport.getReportSummaryRows();
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}