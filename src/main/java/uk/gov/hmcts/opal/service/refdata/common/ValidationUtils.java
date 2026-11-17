package uk.gov.hmcts.opal.service.refdata.common;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validateLength(String value, String field, int max) {
        if (value != null && value.length() > max) {
            throw invalid(field, "must not exceed " + max + " characters");
        }
    }

    public static IllegalArgumentException invalid(String field, String message) {
        return new IllegalArgumentException(field + " " + message);
    }
}
