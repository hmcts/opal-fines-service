package uk.gov.hmcts.opal.service.report;

import java.util.List;
import lombok.Data;
import uk.gov.hmcts.opal.dto.report.EnforcementReportDto;
import uk.gov.hmcts.opal.dto.report.EnforcementReportRowDto;

@Data
public class OperationReportByEnforcementTransaction implements ReportDataInterface {

    private EnforcementReportDto enforcementReport;

    private ReportMetaData reportMetaData;

    @Override
    public long getNumberOfRecords() {
        List<EnforcementReportRowDto> transactionList = enforcementReport.getTransactionList();
        return transactionList == null ?  0 : transactionList.size();
    }

    @Override
    public ReportMetaData getReportMetaData() {
        return reportMetaData;
    }
}