package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.experimental.UtilityClass;

import java.util.Locale;

@UtilityClass
public class SpecificationUtils {

    private static final String[] STRIP_CHARS = {
        " ", "-", "'", ".", ",", "/", "\\", "(", ")", "[", "]", "{", "}",
        ":", ";", "!", "?", "\"", "@", "#", "$", "%", "&", "*", "+"
    };

    public static String stripCheckLetter(String acc) {
        if (acc == null) {
            return null;
        }

        return (acc.length() == 9 && Character.isLetter(acc.charAt(8))) ? acc.substring(0, 8) : acc;
    }

    public static <T> Predicate likeStartsWithNormalized(Root<T> root,
                                                  CriteriaBuilder cb,
                                                  String attribute,
                                                  String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String pattern = escapeForLike(normalize(raw)) + "%";
        return cb.like(normField, pattern, '\\');
    }

    public static Predicate likeStartsWithNormalized(CriteriaBuilder cb,
                                              Expression<String> expr,
                                              String raw) {
        Expression<String> normField = normalizeExpr(cb, expr);
        String pattern = escapeForLike(normalize(raw)) + "%";
        return cb.like(normField, pattern, '\\');
    }

    public static String normalize(String s) {
        String out = s.toLowerCase(Locale.ROOT);
        for (String ch : STRIP_CHARS) {
            out = out.replace(ch, "");
        }
        return out;
    }

    public static Expression<String> normalizeExpr(CriteriaBuilder cb, Expression<String> expr) {
        Expression<String> e = cb.lower(expr);
        for (String ch : STRIP_CHARS) {
            e = cb.function("REPLACE", String.class, e, cb.literal(ch), cb.literal(""));
        }
        return e;
    }

    public static String escapeForLike(String s) {
        return s.replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_");
    }

    public static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static <T> Predicate equalNormalized(Root<T> root,
                                                CriteriaBuilder cb,
                                                String attribute,
                                                String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String normalized = normalize(raw);
        return cb.equal(normField, normalized);
    }

    public static Predicate equalNormalized(CriteriaBuilder cb,
                                            Expression<String> expr,
                                            String raw) {
        Expression<String> normField = normalizeExpr(cb, expr);
        String normalized = normalize(raw);
        return cb.equal(normField, normalized);
    }



}
