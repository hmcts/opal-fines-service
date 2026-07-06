package uk.gov.hmcts.opal.service.report.operation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operation.DetailedAccountReportDto;
import uk.gov.hmcts.opal.dto.report.operation.DetailedReportDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@ExtendWith(MockitoExtension.class)
class OperationDetailedReportTest {

    @Mock
    ReportMetaData reportMetaData;

    @Mock
    DetailedReportDto enforcementReport;

    @InjectMocks
    OperationDetailedReport operationByEnforcementDetailedReport;

    @Test
    void getNumberOfRecords_2records_return2() {
        DetailedAccountReportDto row1 =
            DetailedAccountReportDto.builder().build();
        DetailedAccountReportDto row2 =
            DetailedAccountReportDto.builder().build();
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