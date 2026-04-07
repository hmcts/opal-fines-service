package uk.gov.hmcts.opal.util;

import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;

public final class DefendantAccountStatusDisplay {

    private DefendantAccountStatusDisplay() {
    }

    public static String toDisplayName(DefendantAccountStatus status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case L -> "Live";
            case TO -> "TFO to be acknowledged";
            case TS -> "TFO to NI/Scotland to be acknowledged";
            case TA -> "TFO acknowledged";
            case CS -> "Account consolidated";
            case WO -> "Account written off";
        };
    }
}
