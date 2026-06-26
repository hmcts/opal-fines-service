package uk.gov.hmcts.opal.entity.report;

import lombok.Getter;

@Getter
public enum ReportInstanceGenerationStatus {

    REQUESTED("Requested"),
    IN_PROGRESS("In Progress"),
    READY("Ready"),
    ERROR("Error");

    private final String displayName;

    ReportInstanceGenerationStatus(String displayName) {
        this.displayName = displayName;
    }

}
