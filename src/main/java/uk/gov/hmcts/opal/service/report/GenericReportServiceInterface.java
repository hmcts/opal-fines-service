package uk.gov.hmcts.opal.service.report;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;

public interface GenericReportServiceInterface {

    void generateReportInstanceContent(Long id);

    List<ReportInstanceListReportsInner> searchReportInstances(LocalDate fromDate, LocalDate toDate,
        List<Integer> businessUnits, Integer userId, String reportId);

}
