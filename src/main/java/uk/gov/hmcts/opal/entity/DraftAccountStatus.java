package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DraftAccountStatus {

    SUBMITTED("Submitted"),
    REJECTED("Rejected"),
    DELETED("Deleted"),
    APPROVED("Approved"),
    RESUBMITTED("Resubmitted"),
    PENDING("Pending"),
    ERROR_IN_PUBLISHING("Error in publishing");

    private final String label;

    DraftAccountStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }
}
