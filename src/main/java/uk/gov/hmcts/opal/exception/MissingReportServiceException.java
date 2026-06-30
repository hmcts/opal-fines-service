package uk.gov.hmcts.opal.exception;

public class MissingReportServiceException extends ReportNotFoundException {

    public MissingReportServiceException(String reportId) {
        super("No report service implementation found for reportId: " + reportId);
    }

    public MissingReportServiceException(String reportId, Throwable cause) {
        super("No report service implementation found for reportId: " + reportId);
        initCause(cause);
    }
}
