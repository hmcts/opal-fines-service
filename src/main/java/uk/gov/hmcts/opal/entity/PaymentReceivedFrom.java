package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum PaymentReceivedFrom {
    DEFENDANT("D"),
    APPLICANT("A"),
    THIRD_PARTY("T");

    private final String code;

    PaymentReceivedFrom(String code) {
        this.code = code;
    }
}
