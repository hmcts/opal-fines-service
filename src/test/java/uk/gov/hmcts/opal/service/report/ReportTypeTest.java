package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.opal.service.report.ReportType.FP_REGISTER;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

class ReportTypeTest {

    @Test
    void getReportId_reportTypeExists_returnReportType() {
        ReportType type = ReportType.fromReportId("fp_register");
        assertEquals(FP_REGISTER, type);
    }

    @Test
    void getReportId_reportTypeDoesNotExist_throwException() {
        assertThrows(
            ReportNotFoundException.class,
            () -> ReportType.fromReportId("fake_report_type")
        );
    }
}