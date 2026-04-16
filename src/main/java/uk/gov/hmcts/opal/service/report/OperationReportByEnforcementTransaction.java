package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.Data;

@Data
public class OperationReportByEnforcementTransaction implements ReportDataInterface {

    private List<EnforcementReportRowDto> transactionList;

    private ReportMetaData reportMetaData;

    @Override
    public short getNumberOfRecords() {
        return transactionList == null ? (short) 0 : (short) transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}