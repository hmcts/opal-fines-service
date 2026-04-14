package uk.gov.hmcts.opal.entity.report;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum ReportInstanceGenerationStatus {

    REQUESTED,
    IN_PROGRESS,
    READY,
    ERROR

}
