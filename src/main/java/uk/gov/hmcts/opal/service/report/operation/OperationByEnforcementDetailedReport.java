package uk.gov.hmcts.opal.service.report.operation;

import java.util.List;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.service.report.ReportDataInterface;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Data
public class OperationByEnforcementDetailedReport implements ReportDataInterface {

    private DetailedReportDto detailedReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<DetailedAccountReportDto> transactionList = detailedReport.getAccountTransactionReports();
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}
