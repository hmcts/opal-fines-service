package uk.gov.hmcts.opal.service.report;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public enum ReportInstanceGenerationStatus {
    REQUESTED,
    IN_PROGRESS,
    READY,
    ERROR
}
