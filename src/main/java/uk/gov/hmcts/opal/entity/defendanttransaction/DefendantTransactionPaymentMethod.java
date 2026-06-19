package uk.gov.hmcts.opal.entity.defendanttransaction;

public enum DefendantTransactionPaymentMethod {
    NC("Notes & Coins"),
    CQ("Cheque"),
    CT("Credit Transfer"),
    PO("Postal Order");

    private final String displayName;

    DefendantTransactionPaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
