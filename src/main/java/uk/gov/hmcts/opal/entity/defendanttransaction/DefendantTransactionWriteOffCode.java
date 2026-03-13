package uk.gov.hmcts.opal.entity.defendanttransaction;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum DefendantTransactionWriteOffCode {
    JCAM_A("JCAM-A"),
    JCAM_B("JCAM-B"),
    JCAM_C("JCAM-C"),
    JCAM_D("JCAM-D"),
    JCAM_E("JCAM-E"),
    JCAM_F("JCAM-F"),
    JCAM_G("JCAM-G"),
    JCAM_H("JCAM-H"),
    JCAM_I("JCAM-I"),
    JCAM_K("JCAM-K"),
    REMITT("REMITT"),
    IMPRIS("IMPRIS"),
    APPEAL("APPEAL"),
    CTPROC("CTPROC"),
    FIXPEN("FIXPEN"),
    REVIEW("REVIEW"),
    INPERR("INPERR"),
    OTHERS("OTHERS"),
    AMTCON("AMTCON"),
    TRNOUT("TRNOUT");

    private final String label;

    DefendantTransactionWriteOffCode(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public static DefendantTransactionWriteOffCode getByLabel(String label) {
        return Stream.of(DefendantTransactionWriteOffCode.values())
            .filter(code -> code.getLabel().equals(label))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
