package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

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
        when(report1.getReportId()).thenReturn(ReportId.FP_REGISTER);

        ReportRegistry registry =
            new ReportRegistry(List.of(report1));

        assertSame(report1, registry.get(ReportId.FP_REGISTER.reportId));
    }

    @Test
    void constructor_throwsOnDuplicateTypes() {
        when(report1.getReportId()).thenReturn(ReportId.FP_REGISTER);
        when(report2.getReportId()).thenReturn(ReportId.FP_REGISTER);

        assertThrows(IllegalStateException.class, () -> new ReportRegistry(List.of(report1, report2)));
    }

    @Test
    void constructor_implementationNotFound_throwsReportNotFoundException() {
        ReportRegistry registry = new ReportRegistry(List.of(report1));
        assertThrows(ReportNotFoundException.class, () -> registry.get(ReportId.FP_REGISTER.reportId));
    }
}