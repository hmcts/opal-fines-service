package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum ResultId {
    ABDC("ABDC"),
    AEO("AEO"),
    AEOC("AEOC"),
    BWTU("BWTU"),
    CLAMPO("CLAMPO"),
    COLLO("COLLO"),
    CONF("CONF"),
    CW("CW"),
    DW("DW"),
    FSN("FSN"),
    HTT("HTT"),
    INTL("INTL"),
    MPSO("MPSO"),
    NAP("NAP"),
    NBWT("NBWT"),
    NOENF("NOENF"),
    PRIS("PRIS"),
    REGF("REGF"),
    REM("REM"),
    S136("S136"),
    SC("SC"),
    UPWO("UPWO"),
    WDN("WDN");

    private final String value;

    ResultId(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static ResultId fromValue(String value) {
        for (ResultId id : ResultId.values()) {
            if (id.value.equals(value)) {
                return id;
            }
        }
        throw new IllegalArgumentException("Unknown ResultId value: " + value);
    }
}
