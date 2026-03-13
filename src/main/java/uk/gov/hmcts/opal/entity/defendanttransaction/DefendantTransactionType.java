package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantTransactionType {
    CANCHQ("CANCHQ"),
    CHEQUE("CHEQUE"),
    CONSOL("CONSOL"),
    DISHCQ("DISHCQ"),
    FR_SUS("FR-SUS"),
    MADJ("MADJ"),
    PAYMNT("PAYMNT"),
    REPSUS("REPSUS"),
    REVPAY("REVPAY"),
    RICHEQ("RICHEQ"),
    RVWOFF("RVWOFF"),
    TFO("TFO"),
    TFO_IN("TFO IN"),
    WRTOFF("WRTOFF"),
    XFER("XFER");

    private final String label;

    @JsonValue
    public String getLabel() {
        return label;
    }

    DefendantTransactionType(String label) {
        this.label = label;
    }

    public static DefendantTransactionType getByLabel(String label) {
        return Stream.of(DefendantTransactionType.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
