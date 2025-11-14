package uk.gov.hmcts.opal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for parsing ETag/If-Match version headers like:
 *   "3", W/"7",  W/"001",  " 12 ", etc.
 * Returns a positive int version, defaulting to 1 when absent/invalid/out-of-range.
 */
public class IfMatchVersionUtil {

    // private constructor to prevent instantiation
    private IfMatchVersionUtil() {
    }

    // first run of digits in the header (e.g., matches 7 in W/"7")
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");

    /**
     * Parse If-Match header into a version number.
     * Examples:
     *  null/blank/garbage -> 1
     *  "\"3\""           -> 3
     *  "W/\"7\""         -> 7
     *  "W/\"001\""       -> 1
     *  "\"-1\""          -> 1   (negatives not allowed)
     *  beyond Integer.MAX_VALUE -> 1
     */
    public static int parseIfMatchVersion(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) return 1;

        Matcher m = DIGITS.matcher(ifMatch);
        if (!m.find()) return 1;

        String number = m.group(1); // digits only (no quotes/prefix)
        try {
            long parsed = Long.parseLong(number); // safe for large digits
            if (parsed <= 0 || parsed > Integer.MAX_VALUE) return 1;
            return (int) parsed;
        } catch (NumberFormatException e) {
            // TODO throw exception?
            return 1;
        }
    }
}
