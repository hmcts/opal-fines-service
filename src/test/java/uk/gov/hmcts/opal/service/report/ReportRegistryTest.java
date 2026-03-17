package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportRegistryTest {

    @SuppressWarnings("rawtypes")
    @Mock
    ReportInterface report1;

    @SuppressWarnings("rawtypes")
    @Mock
    ReportInterface report2;


    @Test
    void constructor_buildsMapAccessibleWithReportId() {
        when(report1.getType()).thenReturn(ReportType.FP_REGISTER);

        ReportRegistry registry =
            new ReportRegistry(List.of(report1));

        assertSame(report1, registry.get(ReportType.FP_REGISTER.reportId));
    }

    @Test
    void constructor_throwsOnDuplicateTypes() {
        when(report1.getType()).thenReturn(ReportType.FP_REGISTER);
        when(report2.getType()).thenReturn(ReportType.FP_REGISTER);

        assertThrows(IllegalStateException.class, () -> new ReportRegistry(List.of(report1, report2)));
    }
}