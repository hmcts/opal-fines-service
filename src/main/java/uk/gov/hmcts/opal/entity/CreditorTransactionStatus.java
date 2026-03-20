package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditorTransactionStatus {
    C,
    D,
    P,
    R,
    X;

    @JsonValue
    public String getValue() {
        return name();
    }
}
