package uk.gov.hmcts.opal.entity;

public enum DraftAccountStatus {
    SUBMITTED("Submitted");

    private final String label;

    DraftAccountStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
