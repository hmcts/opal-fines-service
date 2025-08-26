package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity_;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
public class MinorCreditorSpecs {

    private static final String[] STRIP_CHARS =
        {" ", "-", "'", ".", ",", "/", "\\", "(", ")", "[", "]",
            "{", "}", ":", ";", "!", "?", "\"", "@", "#", "$", "%", "&", "*", "+"};

    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch c) {
        if (c == null) {
            return Specification.allOf();
        }

        List<Specification<MinorCreditorEntity>> parts = new ArrayList<>();
        byBusinessUnitIds(c).ifPresent(parts::add);
        byCreditorAccountNumber(c).ifPresent(parts::add);               // <-- uses view's account_number
        parts.addAll(byCreditorTextFields(c));                          // organisationName etc.

        return combineAnd(parts);
    }

    private Optional<Specification<MinorCreditorEntity>> byBusinessUnitIds(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getBusinessUnitIds())
            .map(list -> list.stream().filter(Objects::nonNull)
                .map(i -> (short) i.intValue()).toList()).filter(
                    ids -> !ids.isEmpty())
            .map(ids -> (root, q, cb)
                -> root.get(MinorCreditorEntity_.businessUnitId).in(ids));
    }

    private Optional<Specification<MinorCreditorEntity>> byCreditorAccountNumber(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getAccountNumber())
            .filter(s -> !s.isBlank()).map(MinorCreditorSpecs::stripCheckLetter)
            .map(prefix -> (Specification<MinorCreditorEntity>)
                (root, q, cb) -> likeStartsWithNormalized(
                cb,
                root.get(MinorCreditorEntity_.accountNumber),
                prefix
            ));
    }

    private List<Specification<MinorCreditorEntity>> byCreditorTextFields(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor()).map(cred -> {
            List<Specification<MinorCreditorEntity>> out = new ArrayList<>();
            addStartsWithIfPresent(out, "organisationName", cred.getOrganisationName());
            addStartsWithIfPresent(out, "forenames", cred.getForenames());
            addStartsWithIfPresent(out, "surname", cred.getSurname());
            addStartsWithIfPresent(out, "addressLine1", cred.getAddressLine1());
            addStartsWithIfPresent(out, "postCode", cred.getPostcode());

            return out;
        }).orElseGet(java.util.Collections::emptyList);
    }

    private void addStartsWithIfPresent(List<Specification<MinorCreditorEntity>> acc, String attribute, String value) {
        if (value != null && !value.isBlank()) {
            acc.add((root, q, cb) -> likeStartsWithNormalized(root, cb, attribute, value));
        }
    }

    private Specification<MinorCreditorEntity> combineAnd(List<Specification<MinorCreditorEntity>> parts) {
        if (parts == null || parts.isEmpty()) {
            return Specification.allOf();
        }
        @SuppressWarnings("unchecked") Specification<MinorCreditorEntity>[] arr = parts.toArray(Specification[]::new);
        return Specification.allOf(arr);
    }

    private static Specification<MinorCreditorEntity> likeStartsWithAttr(String attribute, String raw) {
        return (root, q, cb) -> likeStartsWithNormalized(root, cb, attribute, raw);
    }

    private static String stripCheckLetter(String acc) {
        if (acc == null) {
            return null;
        }
        return (acc.length() == 9 && Character.isLetter(acc.charAt(8))) ? acc.substring(0, 8) : acc;
    }

    private static <T> Predicate likeStartsWithNormalized(Root<T> root,
                                                          CriteriaBuilder cb, String attribute, String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String pattern = escapeForLike(normalize(raw)) + "%";   // escape first, then add %
        return cb.like(normField, pattern, '\\');
    }

    /**
     * Expression-based normalized starts-with LIKE (for metamodel paths, joins, etc.).
     */
    private static Predicate likeStartsWithNormalized(CriteriaBuilder cb, Expression<String> expr, String raw) {
        Expression<String> normField = normalizeExpr(cb, expr);
        String pattern = escapeForLike(normalize(raw)) + "%";   // escape first, then add %
        return cb.like(normField, pattern, '\\');
    }

    private static String normalize(String s) {
        String out = s.toLowerCase(Locale.ROOT);
        for (String ch : STRIP_CHARS) {
            out = out.replace(ch, "");
        }
        return out;
    }

    private static Expression<String> normalizeExpr(CriteriaBuilder cb, Expression<String> expr) {
        Expression<String> e = cb.lower(expr);
        for (String ch : STRIP_CHARS) {
            e = cb.function("REPLACE", String.class, e, cb.literal(ch), cb.literal(""));
        }
        return e;
    }

    private static String escapeForLike(String s) {
        return s.replace("\\", "\\\\")
            .replace("%", "\\%").replace("_", "\\_");
    }
}
