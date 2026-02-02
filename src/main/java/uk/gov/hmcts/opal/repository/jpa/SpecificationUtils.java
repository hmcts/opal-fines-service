package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class SpecificationUtils {

    private static final char[] STRIP_CHARS = {
        ' ', '-', '\'', '.', ',', '/', '\\', '(', ')', '[', ']', '{', '}',
        ':', ';', '!', '?', '"', '@', '#', '$', '%', '&', '*', '+'
    };

    private static final boolean[] DROP = new boolean[256];

    static {
        for (int i = 0; i < STRIP_CHARS.length; i++) {
            char c = STRIP_CHARS[i];
            if (c < 256) {
                DROP[c] = true;
            } else {
                throw new RuntimeException("Character out of range in STRIP_CHARS: " + c);
            }
        }
    }

    public static String stripCheckLetter(String acc) {
        if (acc == null || acc.isBlank()) {
            return null;
        }

        return (acc.length() == 9 && Character.isLetter(acc.charAt(8))) ? acc.substring(0, 8) : acc;
    }

    public static <T> Predicate likeStartsWithNormalized(
        Root<T> root, CriteriaBuilder cb, String attribute, String raw) {

        return cb.like(normalizeExpr(cb, root.get(attribute)), escapeForLike(normalize(raw)) + "%", '\\');
    }

    public static Predicate likeStartsWithNormalized(CriteriaBuilder cb, Expression<String> expr, String raw) {
        return cb.like(normalizeExpr(cb, expr), escapeForLike(normalize(raw)) + "%", '\\');
    }

    public static String normalize(String s) {
        return stripChars(s).toLowerCase(Locale.ROOT);
    }

    public static String stripCharsOrNull(String s) {
        if (s == null) {
            return null;
        }
        return nullifyBlank(stripChars(s));
    }

    public static String stripCharsAndLowerOrNull(String s) {
        if (s == null) {
            return null;
        }
        return nullifyBlank(stripChars(s).toLowerCase());
    }

    // Strips characters defined in STRIP_CHARS from a string, typically input by the user
    public static String stripChars(String s) {
        StringBuilder out = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 256 || !DROP[c]) {
                out.append(c);
            }
        }

        return out.toString();
    }

    // Strips a reduced set of characters from a DB expression, which typically would be a DB column
    public static Expression<String> stripChars(CriteriaBuilder cb, Expression<String> expr) {
        return cb.function("translate", String.class, expr, cb.literal(" -'’"), cb.literal(""));
    }

    public static Expression<String> normalizeExpr(CriteriaBuilder cb, Expression<String> expr) {
        return cb.lower(stripChars(cb, expr));
    }

    public static String escapeForLike(String s) {
        return s.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    public static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isNullOrBlank(String candidate) {
        return Objects.isNull(candidate) || candidate.isBlank();
    }

    public static Boolean nullifyFalse(Boolean candidate) {
        return candidate == null ? null : candidate ? true : null;
    }

    public static String nullifyBlank(String candidate) {
        return candidate.isBlank() ? null : candidate;
    }

    public static <T> List<T> removeNullItems(List<T> candidate) {
        return nullifyEmptyList(candidate.stream().filter(Objects::nonNull).toList());
    }

    public static <T> List<T> nullifyEmptyList(List<T> candidate) {
        return candidate.isEmpty() ? null : candidate;
    }

    public static <T> Predicate equalNormalized(Root<T> root, CriteriaBuilder cb, String attribute, String raw) {
        return cb.equal(normalizeExpr(cb, root.get(attribute)), normalize(raw));
    }

    public static Predicate equalNormalized(CriteriaBuilder cb, Expression<String> expr, String raw) {
        return cb.equal(normalizeExpr(cb, expr), normalize(raw));
    }

    // TODO - this is very application data specific - move to appropriate place (maybe an enum?)
    public static String mapAccountStatusDisplayName(String statusCode) {
        if (statusCode == null) {
            return null;
        }

        return switch (statusCode.trim()) {
            case "L"  -> "Live";
            case "C"  -> "Completed";
            case "TO" -> "TFO to be acknowledged";
            case "TS" -> "TFO to NI/Scotland to be acknowledged";
            case "TA" -> "TFO acknowledged";
            case "CS" -> "Account consolidated";
            case "WO" -> "Account written off";
            default   -> statusCode;
        };
    }
}
