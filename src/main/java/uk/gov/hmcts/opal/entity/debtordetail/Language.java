package uk.gov.hmcts.opal.entity.debtordetail;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum Language {
    ENGLISH("EN"),
    WELSH_AND_ENGLISH("CY");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static Language fromCode(String code) {
        return Stream.of(Language.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown Language code: " + code));
    }
}
