package uk.gov.hmcts.opal.service.report;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

@Getter
public enum ReportId {

    FP_REGISTER("fp_register"),
    OP_ENFORCEMENT("operational_report_enforcement");

    public final String reportId;

    ReportId(String reportId) {
        this.reportId = reportId;
    }

    private static final Map<String, ReportId> BY_REPORT_ID =
        Stream.of(values())
            .collect(Collectors.toMap(ReportId::getReportId, reportType -> reportType));

    public static ReportId fromReportId(String reportId) {
        ReportId type = BY_REPORT_ID.get(reportId);
        if (type == null) {
            throw new ReportNotFoundException("Report id is not a valid report type: " + reportId);
        }
        return type;
    }
}
