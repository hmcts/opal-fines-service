package uk.gov.hmcts.opal.dto;

public enum AccountStatusReportFilterType {

    LIVE("live"),
    CLOSED("closed"),
    ALL("all");

    private final String label;

    AccountStatusReportFilterType(String label) {
        this.label = label;
    }


    public static AccountStatusReportFilterType fromCode(String label) {
        for (AccountStatusReportFilterType value : values()) {
            if (value.label.equals(label)) {
                return value;
            }
        }
        return null;
    }
}
