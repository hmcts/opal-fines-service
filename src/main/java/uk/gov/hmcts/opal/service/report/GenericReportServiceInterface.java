package uk.gov.hmcts.opal.service.report;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceRequestReports;
import uk.gov.hmcts.opal.generated.model.CreateReportInstanceResponseReports;
import uk.gov.hmcts.opal.generated.model.ReportInstanceListReportsInner;
import uk.gov.hmcts.opal.generated.model.ReportInstanceReports;

public interface GenericReportServiceInterface {

    void generateReportInstanceContent(Long id);

    CreateReportInstanceResponseReports addReportInstance(
        CreateReportInstanceRequestReports request,
        Long requestedBy,
        String requestedByName,
        boolean generateReportContentAsync
    );

    List<ReportInstanceListReportsInner> searchReportInstances(
        LocalDate fromDate,
        LocalDate toDate,
        List<Short> businessUnits,
        Long userId,
        String reportId
    );

    ReportInstanceReports getReportInstance(Long id);

    Object getReportInstanceContent(Long id, FileType fileType);
}
