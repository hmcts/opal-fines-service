package uk.gov.hmcts.opal.service.report;

public enum ReportType {
    DETAILED("Detailed"),
    SUMMARY("Summary");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }
}