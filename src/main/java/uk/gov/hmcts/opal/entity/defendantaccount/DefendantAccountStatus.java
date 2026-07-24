package uk.gov.hmcts.opal.entity.defendantaccount;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import lombok.Getter;
import uk.gov.hmcts.opal.entity.MappingValue;

@Getter
public enum DefendantAccountStatus implements MappingValue {
    ACCOUNT_CONSOLIDATED("CS", "Account consolidated"),
    LIVE("L", "Live"),
    TRANSFER_OUT_ACKNOWLEDGED("TA", "TFO acknowledged"),
    TRANSFER_OUT_PENDING("TO", "TFO to be acknowledged"),
    TRANSFER_OUT_TO_NI_SCOTLAND("TS", "TFO to NI/Scotland to be acknowledged"),
    ACCOUNT_WRITTEN_OFF("WO", "Account written off");

    private final String code;
    private final String displayName;

    DefendantAccountStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @Override
    @JsonValue
    public String getCode() {
        return code;
    }

    public static DefendantAccountStatus getByCode(String code) {
        return Stream.of(values())
            .filter(status -> status.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown DefendantAccountStatus: " + code));
    }
}
