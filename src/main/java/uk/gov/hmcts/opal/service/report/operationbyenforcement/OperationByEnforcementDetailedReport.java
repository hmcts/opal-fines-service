package uk.gov.hmcts.opal.service.report.operationbyenforcement;

import java.util.List;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Data
public class OperationByEnforcementDetailedReport implements ReportDataInterface {

    private OperationByEnforcementDetailedReportDto enforcementReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<OperationByEnforcementDetailedAccountReportDto> transactionList =
            enforcementReport.getAccountTransactionReports();
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}