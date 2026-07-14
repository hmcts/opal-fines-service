package uk.gov.hmcts.opal.service.report.operation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OperationDetailedReport implements OperationReportDataInterface {

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
