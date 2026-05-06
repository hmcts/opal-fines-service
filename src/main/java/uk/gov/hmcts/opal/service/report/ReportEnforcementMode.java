package uk.gov.hmcts.opal.service.report;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ReportEnforcementMode {

    ALL,
    LAST_ACTION,
    REGF,
    NOT_UNDER_ENFORCEMENT;

    @JsonCreator
    public static ReportEnforcementMode from(String value) {
        if (value == null) {
            return ALL;
        }
        return ReportEnforcementMode.valueOf(value);
    }
}
