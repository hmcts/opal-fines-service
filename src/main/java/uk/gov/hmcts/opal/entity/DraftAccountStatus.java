package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    @JsonCreator
    public static DraftAccountStatus fromLabel(String label) {
        return Arrays.stream(values()).filter(s -> s.getLabel().equals(label)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("'" + label + "' is not a valid Draft Account Status."));
    }
}
