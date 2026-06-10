package uk.gov.hmcts.opal.service.report.operationbyenforcement;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportDto;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@ExtendWith(MockitoExtension.class)
class OperationByEnforcementSummaryReportTest {

    @Mock
    ReportMetaData reportMetaData;

    @Mock
    OperationByEnforcementSummaryReportDto enforcementReport;

    @InjectMocks
    OperationByEnforcementSummaryReport operationByEnforcementSummaryReport;

    @Test
    void getNumberOfRecords_2records_return2() {
        OperationByEnforcementSummaryReportRowDto row1 = OperationByEnforcementSummaryReportRowDto.builder().build();
        OperationByEnforcementSummaryReportRowDto row2 = OperationByEnforcementSummaryReportRowDto.builder().build();
        when(enforcementReport.getReportSummaryRows()).thenReturn(List.of(row1, row2));
        assertThat(operationByEnforcementSummaryReport.getNumberOfRecords()).isEqualTo(2);
    }

    @Test
    void getNumberOfRecords_noRecords_return0() {
        when(enforcementReport.getReportSummaryRows()).thenReturn(null);
        assertThat(operationByEnforcementSummaryReport.getNumberOfRecords()).isEqualTo(0);
    }

    @Test
    void getReportMetaData_returnMetadata() {
        ReportMetaData actual = operationByEnforcementSummaryReport.getReportMetaData();
        assertThat(actual).isEqualTo(reportMetaData);
    }
}