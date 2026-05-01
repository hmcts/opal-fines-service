package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.Data;

@Data
public class OperationReportByEnforcementTransaction implements ReportDataInterface {

    private EnforcementReportDto enforcementReport;

    private ReportMetaData reportMetaData;

    @Override
    public int getNumberOfRecords() {
        List<EnforcementReportRowDto> transactionList = enforcementReport.getTransactionList();
        return transactionList == null ?  0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}