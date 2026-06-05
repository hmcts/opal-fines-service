package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantTransactionWriteOffCode {
    JCAM_A("JCAM-A", "JCAM-A"),
    JCAM_B("JCAM-B", "JCAM-B"),
    JCAM_C("JCAM-C", "JCAM-C"),
    JCAM_D("JCAM-D", "JCAM-D"),
    JCAM_E("JCAM-E", "JCAM-E"),
    JCAM_F("JCAM-F", "JCAM-F"),
    JCAM_G("JCAM-G", "JCAM-G"),
    JCAM_H("JCAM-H", "JCAM-H"),
    JCAM_I("JCAM-I", "JCAM-I"),
    JCAM_K("JCAM-K", "JCAM-K"),
    REMITT("REMITT", "REMITT"),
    IMPRIS("IMPRIS", "IMPRIS"),
    APPEAL("APPEAL", "APPEAL"),
    CTPROC("CTPROC", "CTPROC"),
    FIXPEN("FIXPEN", "FIXPEN"),
    REVIEW("REVIEW", "REVIEW"),
    INPERR("INPERR", "INPERR"),
    OTHERS("OTHERS", "OTHERS"),
    AMTCON("AMTCON", "AMTCON"),
    TRNOUT("TRNOUT", "Write off");

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
