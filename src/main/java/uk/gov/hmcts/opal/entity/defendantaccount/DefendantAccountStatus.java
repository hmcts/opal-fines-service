package uk.gov.hmcts.opal.entity.defendantaccount;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum DefendantAccountStatus {
    ACCOUNT_CONSOLIDATED("CS", "Account consolidated"),
    LIVE("L", "Live"),
    TRANSFER_OUT_ACKNOWLEDGED("TA", "TFO acknowledged"),
    TRANSFER_OUT_PENDING("TO", "TFO to be acknowledged"),
    TRANSFER_OUT_TO_NI_SCOTLAND("TS", "TFO to NI/Scotland to be acknowledged"),
    ACCOUNT_WRITTEN_OFF("WO", "Account written off");

    private final String label;
    private final String displayName;

    DefendantAccountStatus(String label, String displayName) {
        this.label = label;
        this.displayName = displayName;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DefendantAccountStatus getByLabel(String label) {
        return Stream.of(values())
            .filter(status -> status.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown DefendantAccountStatus: " + label));
    }
}
