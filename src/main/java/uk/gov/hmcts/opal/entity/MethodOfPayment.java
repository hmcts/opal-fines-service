package uk.gov.hmcts.opal.entity;

import lombok.Getter;

@Getter
public enum MethodOfPayment implements MappingValue {
    NOTES_AND_COINS("NC", "Notes and coins"),
    POSTAL_ORDER("PO", "Postal order"),
    CHEQUE("CQ", "Cheque"),
    CREDIT_TRANSFER("CT", "Credit transfer");

    private final String code;
    private final String displayName;

    MethodOfPayment(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
}
