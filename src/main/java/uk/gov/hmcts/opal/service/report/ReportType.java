package uk.gov.hmcts.opal.service.report;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import uk.gov.hmcts.opal.exception.ReportNotFoundException;

@Getter
public enum ReportType {

    FP_REGISTER("fp_register");

    public final String reportId;

    ReportType(String reportId) {
        this.reportId = reportId;
    }

    private static final Map<String, ReportType> BY_REPORT_ID =
        Stream.of(values())
            .collect(Collectors.toMap(ReportType::getReportId, reportType -> reportType));

    public static ReportType fromReportId(String reportId) {
        ReportType type = BY_REPORT_ID.get(reportId);
        if (type == null) {
            throw new ReportNotFoundException("Report id is not a valid report type: " + reportId);
        }
        return type;
    }
}
