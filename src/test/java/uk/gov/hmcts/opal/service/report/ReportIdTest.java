package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.opal.service.report.ReportId.CASH_LIST;
import static uk.gov.hmcts.opal.service.report.ReportId.FP_REGISTER;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

class ReportIdTest {

    @Test
    void getReportId_reportTypeExists_returnReportType() {
        ReportId type = ReportId.fromReportId("fp_register");
        assertEquals(FP_REGISTER, type);
    }

    @Test
    void getReportId_cashListReportTypeExists_returnReportType() {
        ReportId type = ReportId.fromReportId("cash_list");
        assertEquals(CASH_LIST, type);
    }

    @Test
    void getReportId_reportTypeDoesNotExist_throwException() {
        assertThrows(
            ReportNotFoundException.class,
            () -> ReportId.fromReportId("fake_report_type")
        );
    }
}
