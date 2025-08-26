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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class MinorCreditorSpecs {

    private static final String[] STRIP_CHARS = {
        " ", "-", "'", ".", ",", "/", "\\", "(", ")", "[", "]", "{", "}",
        ":", ";", "!", "?", "\"", "@", "#", "$", "%", "&", "*", "+"
    };
    // --- PUBLIC ENTRYPOINT ---
// public entry
    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch c) {
        if (c == null) return Specification.allOf();

        List<Specification<MinorCreditorEntity>> parts = new ArrayList<>();
        byBusinessUnitIds(c).ifPresent(parts::add);
        byCreditorAccountNumber(c).ifPresent(parts::add);               // <-- uses view's account_number
        parts.addAll(byCreditorTextFields(c));                          // organisationName etc.

        return combineAnd(parts);
    }

    // business_unit_id IN (...)
    private Optional<Specification<MinorCreditorEntity>> byBusinessUnitIds(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getBusinessUnitIds())
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(i -> (short) i.intValue())
                .toList())
            .filter(ids -> !ids.isEmpty())
            .map(ids -> (root, q, cb) -> root.get(MinorCreditorEntity_.businessUnitId).in(ids));
    }

    // account_number starts-with (creditor account; strip optional check letter)
    private Optional<Specification<MinorCreditorEntity>> byCreditorAccountNumber(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getAccountNumber())
            .filter(s -> !s.isBlank())
            .map(MinorCreditorSpecs::stripCheckLetter) // "12345678A" -> "12345678"
            .map(prefix -> (Specification<MinorCreditorEntity>)
                (root, q, cb) -> likeStartsWithNormalized(
                    cb, root.get(MinorCreditorEntity_.accountNumber), prefix));
    }

    // text fields on the creditor side (from your view columns)
    private List<Specification<MinorCreditorEntity>> byCreditorTextFields(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .map(cred -> {
                List<Specification<MinorCreditorEntity>> out = new ArrayList<>();
                addStartsWithIfPresent(out, "organisationName", cred.getOrganisationName()); // <-- view column
                addStartsWithIfPresent(out, "forenames",        cred.getForenames());
                addStartsWithIfPresent(out, "surname",          cred.getSurname());
                addStartsWithIfPresent(out, "addressLine1",     cred.getAddressLine1());
                addStartsWithIfPresent(out, "postCode",         cred.getPostcode());
                // Only add organisation == true/false if your search DTO uses Boolean and it's non-null:
                // if (cred.getOrganisation() != null) {
                //     out.add((root, q, cb) -> cb.equal(root.get(MinorCreditorEntity_.organisation), cred.getOrganisation()));
                // }
                return out;
            })
            .orElseGet(java.util.Collections::emptyList);
    }

// unchanged helpers you already have: stripCheckLetter, likeStartsWithNormalized(...), normalizeExpr, etc.

    /** Adds a normalized LIKE 'value%' spec for attribute if the value has text. */
    private void addStartsWithIfPresent(List<Specification<MinorCreditorEntity>> acc,
                                        String attribute,
                                        String value) {
        if (value != null && !value.isBlank()) {
            acc.add((root, q, cb) -> likeStartsWithNormalized(root, cb, attribute, value));
        }
    }

// --- COMBINER ---

    /** AND-combine all specs; neutral spec if empty. */
    private Specification<MinorCreditorEntity> combineAnd(List<Specification<MinorCreditorEntity>> parts) {
        if (parts == null || parts.isEmpty()) return Specification.allOf();
        @SuppressWarnings("unchecked")
        Specification<MinorCreditorEntity>[] arr = parts.toArray(Specification[]::new);
        return Specification.allOf(arr);
    }

    public Specification<MinorCreditorEntity> filterByAccountNumberStartsWithWithCheckLetter(MinorCreditorSearch dto) {
        return (root, query, cb) ->
            Optional.ofNullable(dto)
                .map(MinorCreditorSearch::getAccountNumber)
                .filter(MinorCreditorSpecs::hasText)
                .map(MinorCreditorSpecs::stripCheckLetter)
                .map(stripped -> likeStartsWithNormalized(cb, root.get(MinorCreditorEntity_.accountNumber), stripped))
                .orElseGet(cb::conjunction);
    }

    // -------------------------- functional helpers --------------------------

    private static <T> Specification<T> andAll(List<Specification<T>> specs) {
        if (specs == null || specs.isEmpty()) {
            return Specification.allOf();
        }
        return specs.stream().reduce(Specification.allOf(), Specification::and);
    }

    private static <T> Optional<Specification<T>> specIfHasText(String value,
                                                                Function<String, Specification<T>> builder) {
        return hasText(value) ? Optional.of(builder.apply(value)) : Optional.empty();
    }

    private static <K, V> AbstractMap.SimpleEntry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static <T> Stream<T> optStream(Optional<T> opt) {
        return opt.map(Stream::of).orElseGet(Stream::empty);
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

    /** Attribute-name based normalized starts-with LIKE. */
    private static <T> Predicate likeStartsWithNormalized(Root<T> root,
                                                          CriteriaBuilder cb,
                                                          String attribute,
                                                          String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String pattern = escapeForLike(normalize(raw)) + "%";   // escape first, then add %
        return cb.like(normField, pattern, '\\');
    }

    /** Expression-based normalized starts-with LIKE (for metamodel paths, joins, etc.). */
    private static Predicate likeStartsWithNormalized(CriteriaBuilder cb,
                                                      Expression<String> expr,
                                                      String raw) {
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
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
