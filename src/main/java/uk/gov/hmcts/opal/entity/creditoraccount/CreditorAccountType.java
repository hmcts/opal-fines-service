package uk.gov.hmcts.opal.entity.creditoraccount;

import lombok.Getter;

@Getter
public enum CreditorAccountType {
    CF("Central Fund"),
    MJ("Major Creditor"),
    MN("Minor Creditor");

    private final String label;

    CreditorAccountType(final String label) {
        this.label = label;
    }

    public boolean isMinorCreditor() {
        return this.equals(MN);
    }

    public boolean isMajorCreditor() {
        return this.equals(MJ);
    }

    public boolean isCentralFund() {
        return this.equals(CF);
    }

    public static String getDisplayName(String code) {
        if (code == null) {
            return null;
        }
        try {
            return CreditorAccountType.valueOf(code).getLabel();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
