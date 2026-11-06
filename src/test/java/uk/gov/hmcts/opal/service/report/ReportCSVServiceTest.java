package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;

@ExtendWith(MockitoExtension.class)
class ReportCSVServiceTest {

    @Mock
    ReportDataInterface reportDataInterface;

    @Mock
    ReportCSVMapper<ReportDataInterface> reportCSVMapper;

    @Mock
    ReportCSVMapperRegistry reportCSVMapperRegistry;

    @Mock
    ReportInstanceEntity reportInstanceEntity;

    @InjectMocks
    private ReportCSVService reportCSVService;

    @Test
    void convertReportDtoToCSV_returnsCsvBytesWhenMapperExists() {
        String csv = "HEADER1,HEADER2\n";
        doReturn(reportCSVMapper).when(reportCSVMapperRegistry).get(reportDataInterface.getClass());
        when(reportCSVMapper.reportToCSVString(reportInstanceEntity, reportDataInterface)).thenReturn(csv);

        byte[] result = reportCSVService.convertReportDtoToCSV(reportInstanceEntity, reportDataInterface);

        assertArrayEquals(csv.getBytes(StandardCharsets.UTF_8), result);
        verify(reportCSVMapper).reportToCSVString(reportInstanceEntity, reportDataInterface);
    }

    @Test
    void convertReportDtoToCSV_throwsWhenMapperMissing() {
        UnprocessableException exception = assertThrows(UnprocessableException.class,
            () -> reportCSVService.convertReportDtoToCSV(reportInstanceEntity, reportDataInterface));

        assertEquals("Report cannot be converted to CSV format.", exception.getDetailedReason());
        verifyNoInteractions(reportCSVMapper);
    }
}
