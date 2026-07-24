package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    NC("Notes & Coins"),
    CQ("Cheque"),
    CT("Credit Transfer"),
    PO("Postal Order");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
