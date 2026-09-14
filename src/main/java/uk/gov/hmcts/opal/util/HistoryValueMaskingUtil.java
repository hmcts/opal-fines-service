package uk.gov.hmcts.opal.util;

import java.util.Locale;
import java.util.Set;

public final class HistoryValueMaskingUtil {

    private static final Set<String> BACS_AMENDMENT_ATTRIBUTES = Set.of(
        "bacs sort code",
        "bacs account type",
        "bacs account number",
        "bacs account name",
        "bacs account reference",
        "bank sort code",
        "bank account type",
        "bank account number",
        "bank account name",
        "bank account holder name",
        "bank account reference"
    );

    private HistoryValueMaskingUtil() {
    }

    public static String maskIfBacsAmendment(String attributeName, String value) {
        return isBacsAmendmentAttribute(attributeName) ? maskExceptLastTwo(value) : value;
    }

    public static boolean isBacsAmendmentAttribute(String attributeName) {
        return BACS_AMENDMENT_ATTRIBUTES.contains(normaliseAttributeName(attributeName));
    }

    public static String maskExceptLastTwo(String value) {
        if (value == null || value.length() <= 2) {
            return value;
        }

        return "*".repeat(value.length() - 2) + value.substring(value.length() - 2);
    }

    private static String normaliseAttributeName(String attributeName) {
        if (attributeName == null) {
            return "";
        }

        return attributeName.toLowerCase(Locale.UK)
            .replaceAll("[^a-z0-9]+", " ")
            .trim()
            .replaceAll("\\s+", " ");
    }
}
