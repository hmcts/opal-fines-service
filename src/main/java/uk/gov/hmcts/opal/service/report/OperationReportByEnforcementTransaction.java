package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.Data;

@Data
public class OperationReportByEnforcementTransaction implements ReportDataInterface {

    private List<EnforcementReportRowDto> transactionList;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        return transactionList == null ? 0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}