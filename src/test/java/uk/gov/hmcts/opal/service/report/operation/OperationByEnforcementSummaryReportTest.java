package uk.gov.hmcts.opal.service.report.operation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryReportDto;
import uk.gov.hmcts.opal.service.report.ReportMetaData;

@ExtendWith(MockitoExtension.class)
class OperationByEnforcementSummaryReportTest {

    @Mock
    ReportMetaData reportMetaData;

    @Mock
    SummaryReportDto enforcementReport;

    @InjectMocks
    OperationSummaryReport operationByEnforcementSummaryReport;

    @Test
    void getNumberOfRecords_2records_return2() {
        SummaryOperationReportRowDto row1 = SummaryOperationReportRowDto.builder().build();
        SummaryOperationReportRowDto row2 = SummaryOperationReportRowDto.builder().build();
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
