package uk.gov.hmcts.opal.entity.defendanttransaction;

public enum DefendantTransactionStatus {
    C("Cleared/presented"),
    D("Dishonoured"),
    P("Partially-reversed"),
    R("Reversed"),
    X("Cancelled");

    private final String displayName;

    DefendantTransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
