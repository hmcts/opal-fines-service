package uk.gov.hmcts.opal.service.report;

import java.util.Arrays;

public enum ReportType {
    DETAILED("Detailed"),
    SUMMARY("Summary");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }

    public static ReportType fromLabel(String label) {
        return Arrays.stream(values())
            .filter(t -> t.label.equalsIgnoreCase(label))
            .findFirst()
            .orElse(DETAILED);
    }
}