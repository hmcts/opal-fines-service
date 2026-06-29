package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

public enum TransactionType {
    DISHONOURED_CHEQUE("DISHCQ"),
    TRANSFER_FROM_SUSPENSE("FR_SUS"),
    MONIES_MANUALLY_ADJUSTED_TO_SUSPENSE("MADJ"),
    PAYMENT("PAYMNT"),
    REVERSED_PAYMENT("REVPAY"),
    REVERSED_WRITE_OFF("RVWOFF"),
    TRANSFERRED_OUT("TFO"),
    TRANSFERRED_IN("TFO_IN"),
    WRITE_OFF("WRTOFF");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static TransactionType getByLabel(String label) {
        return Stream.of(TransactionType.values())
            .filter(v -> v.getLabel().equals(label))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown TransactionType: " + label));
    }
}