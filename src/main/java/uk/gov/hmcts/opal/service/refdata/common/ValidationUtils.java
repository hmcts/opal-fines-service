package uk.gov.hmcts.opal.service.refdata.common;

import java.util.function.Function;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void validateRequired(Object value, String field) {
        if (value == null) {
            throw invalid(field, "is required");
        }
    }

    public static void validateLength(String value, String field, int max) {
        if (value != null && value.length() > max) {
            throw invalid(field, "must not exceed " + max + " characters");
        }
    }

    public static void validateValue(String value, String field, Function<String, ?> convert) {
        if (value != null) {
            try {
                convert.apply(value);
            } catch (IllegalArgumentException e) {
                throw invalid(field, "has an invalid value: " + value);
            }
        }
    }

    public static IllegalArgumentException invalid(String field, String message) {
        return new IllegalArgumentException(field + " " + message);
    }
}
