package uk.gov.hmcts.opal.entity.enforcement;

import lombok.Getter;

import java.util.stream.Stream;

public enum AccountType {
    COL("COL"),
    A("A"),
    CO("CO"),
    Y("Y"),
    CFP("CFP");

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
