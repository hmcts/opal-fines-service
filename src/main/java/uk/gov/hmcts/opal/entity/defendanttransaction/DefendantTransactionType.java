package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantTransactionType {
    CANCHQ("CANCHQ", "CANCHQ", "Cancelled cheque"),
    CHEQUE("CHEQUE", "CHEQUE", "Cheque"),
    CONSOL("CONSOL", "CONSOL", "Consolidation (master)"),
    DISHCQ("DISHCQ", "DISHCQ", "Dishonoured Cheque"),
    FR_SUS("FR-SUS", "FR-SUS", "Transfer from suspense"),
    MADJ("MADJ", "MADJ", "Manual adjustment"),
    PAYMNT("PAYMNT", "PAYMNT", "Payments"),
    REPSUS("REPSUS", "REPSUS", "Repayment from suspense"),
    REVPAY("REVPAY", "REVPAY", "Reverse payment"),
    RICHEQ("RICHEQ", "RICHEQ", "Reissued cheque"),
    RVWOFF("RVWOFF", "RVWOFF", "Reverse write-off"),
    TFO("TFO", "TFO", "TFO Out (E&W only)"),
    TFO_IN("TFO IN", "TFOIN", "TFO In"),
    WRTOFF("WRTOFF", "WRTOFF", "Write-off/Consolidation (child)"),
    XFER("XFER", "XFER", "Transfer to suspense");

    private final String label;
    private final String apiCode;
    private final String displayName;

    @JsonValue
    public String getLabel() {
        return label;
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    DefendantTransactionType(String label, String apiCode, String displayName) {
        this.label = label;
        this.apiCode = apiCode;
        this.displayName = displayName;
    }

    public static DefendantTransactionType getByLabel(String label) {
        return Stream.of(DefendantTransactionType.values())
            .filter(type -> type.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
