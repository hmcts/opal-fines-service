package uk.gov.hmcts.opal.service.report.operationbyenforcement;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementDetailedReportDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@ExtendWith(MockitoExtension.class)
class OperationByEnforcementDetailedReportTest {

    @Mock
    ReportMetaData reportMetaData;

    @Mock
    OperationByEnforcementDetailedReportDto enforcementReport;

    @InjectMocks
    OperationByEnforcementDetailedReport operationByEnforcementDetailedReport;

    @Test
    void getNumberOfRecords_2records_return2() {
        OperationByEnforcementDetailedAccountReportDto row1 =
            OperationByEnforcementDetailedAccountReportDto.builder().build();
        OperationByEnforcementDetailedAccountReportDto row2 =
            OperationByEnforcementDetailedAccountReportDto.builder().build();
        when(enforcementReport.getAccountTransactionReports()).thenReturn(List.of(row1, row2));
        assertThat(operationByEnforcementDetailedReport.getNumberOfRecords()).isEqualTo(2);
    }

    @Test
    void getNumberOfRecords_noRecords_return0() {
        when(enforcementReport.getAccountTransactionReports()).thenReturn(null);
        assertThat(operationByEnforcementDetailedReport.getNumberOfRecords()).isEqualTo(0);
    }

    @Test
    void getReportMetaData_returnMetadata() {
        ReportMetaData actual = operationByEnforcementDetailedReport.getReportMetaData();
        assertThat(actual).isEqualTo(reportMetaData);
    }
}