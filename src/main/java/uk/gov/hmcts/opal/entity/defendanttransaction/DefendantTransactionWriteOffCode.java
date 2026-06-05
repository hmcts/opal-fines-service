package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantTransactionWriteOffCode {
    JCAM_A("JCAM-A", "Unknown Whereabouts"),
    JCAM_B("JCAM-B", "Emigrated / Gone Abroad"),
    JCAM_C("JCAM-C", "Deceased"),
    JCAM_D("JCAM-D", "Sent to Mental Health Institution"),
    JCAM_E("JCAM-E", "Sum Less Than £10"),
    JCAM_F("JCAM-F", "Imprisonment 12 Months"),
    JCAM_G("JCAM-G", "Limited Company Wound Up"),
    JCAM_H("JCAM-H", "Serviceman – Military Correctional Training"),
    JCAM_I("JCAM-I", "Local Authority Moved to Scotland"),
    JCAM_K("JCAM-K", "Other"),
    REMITT("REMITT", "Remitted"),
    IMPRIS("IMPRIS", "Satisfied by Imprisonment"),
    APPEAL("APPEAL", "Appeals"),
    CTPROC("CTPROC", "Statutory Declarations (Court Proceedings)"),
    FIXPEN("FIXPEN", "Statutory Declarations (Fixed Penalty)"),
    REVIEW("REVIEW", "Compensation No Longer Payable (Criminal Courts Act)"),
    INPERR("INPERR", "Input Error"),
    OTHERS("OTHERS", "Others"),
    AMTCON("AMTCON", "Consolidated"),
    TRNOUT("TRNOUT", "Transferred Out");

    private final String label;
    private final String displayName;

    DefendantTransactionWriteOffCode(String label, String displayName) {
        this.label = label;
        this.displayName = displayName;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DefendantTransactionWriteOffCode getByLabel(String label) {
        return Stream.of(DefendantTransactionWriteOffCode.values())
            .filter(code -> code.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
