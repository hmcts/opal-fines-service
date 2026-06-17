package uk.gov.hmcts.opal.dto;

public enum CollectionOrderReportFilterType {

    WITH("with"),
    WITHOUT("without"),
    ALL("all");

    private final String label;

    CollectionOrderReportFilterType(String label) {
        this.label = label;
    }


    public static CollectionOrderReportFilterType fromCode(String label) {
        for (CollectionOrderReportFilterType value : values()) {
            if (value.label.equals(label)) {
                return value;
            }
        }
        return null;
    }
}
