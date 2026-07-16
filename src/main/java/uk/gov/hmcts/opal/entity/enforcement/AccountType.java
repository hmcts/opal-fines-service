package uk.gov.hmcts.opal.entity.enforcement;

import java.util.stream.Stream;
import lombok.Getter;

public enum AccountType {
    ADULT_COLLECTION_ORDER("COL"),
    ADULT_NO_COLLECTION_ORDER("A"),
    COMPANY("CO"),
    YOUTH("Y");

    @Getter
    private final String code;

    AccountType(String code) {
        this.code = code;
    }

    public static AccountType getByCode(String code) {
        return Stream.of(AccountType.values())
            .filter(at -> at.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown AccountType: " + code));
    }
}
