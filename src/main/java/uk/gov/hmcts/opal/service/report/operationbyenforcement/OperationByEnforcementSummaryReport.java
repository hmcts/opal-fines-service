package uk.gov.hmcts.opal.service.report.operationbyenforcement;

import java.util.List;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportDto;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Data
public class OperationByEnforcementSummaryReport implements ReportDataInterface {

    private OperationByEnforcementSummaryReportDto enforcementReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<OperationByEnforcementSummaryReportRowDto> transactionList = enforcementReport.getReportSummaryRows();
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}