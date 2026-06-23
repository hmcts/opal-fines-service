package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.opal.service.report.mapper.csv.ReportCSVMapper;

@ExtendWith(MockitoExtension.class)
class ReportCSVServiceTest {

    @Mock
    ReportDataInterface reportDataInterface;

    @Mock
    ReportCSVMapper<ReportDataInterface> reportCSVMapper;

    private Map<Class<? extends ReportDataInterface>, ReportCSVMapper<? extends ReportDataInterface>>
        reportToCSVStringMapperMap;

    private ReportCSVService reportCSVService;

    @BeforeEach
    void setUp() {
        reportToCSVStringMapperMap = new HashMap<>();
        reportCSVService = new ReportCSVService(reportToCSVStringMapperMap);
    }

    @Test
    void covertReportDtoToCSV_returnsCsvBytesWhenMapperExists() {
        String csv = "HEADER1,HEADER2\n";
        reportToCSVStringMapperMap.put(reportDataInterface.getClass(), reportCSVMapper);
        when(reportCSVMapper.reportToCSVString(reportDataInterface)).thenReturn(csv);

        byte[] result = reportCSVService.covertReportDtoToCSV(reportDataInterface);

        assertArrayEquals(csv.getBytes(StandardCharsets.UTF_8), result);
        verify(reportCSVMapper).reportToCSVString(reportDataInterface);
    }

    @Test
    void covertReportDtoToCSV_throwsWhenMapperMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> reportCSVService.covertReportDtoToCSV(reportDataInterface));

        assertEquals("Report cannot be converted to CSV format.", exception.getMessage());
        verifyNoInteractions(reportCSVMapper);
    }
}
