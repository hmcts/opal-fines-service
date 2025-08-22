package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity_;

import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinorCreditorSpecs {

    private static final String[] STRIP_CHARS = {
        " ", "-", "'", ".", ",", "/", "\\", "(", ")", "[", "]", "{", "}",
        ":", ";", "!", "?", "\"", "@", "#", "$", "%", "&", "*", "+"
    };

    /** Build a Specification for MinorCreditorEntity from the search DTO (functional style). */
    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch criteria) {
        if (criteria == null) {
            return Specification.allOf();
        }

        Optional<Specification<MinorCreditorEntity>> businessUnitSpec =
            Optional.ofNullable(criteria.getBusinessUnitIds())
                .map(list -> list.stream()
                    .filter(Objects::nonNull)
                    .map(i -> (short) i.intValue())
                    .collect(Collectors.toList()))
                .filter(ids -> !ids.isEmpty())
                .map(ids -> (Specification<MinorCreditorEntity>) (root, q, cb) -> root.get("businessUnitId").in(ids));

        Optional<Specification<MinorCreditorEntity>> accountNumberSpec =
            hasText(criteria.getAccountNumber())
                ? Optional.of(likeStartsWithAttr("accountNumber", criteria.getAccountNumber()))
                : Optional.empty();

        Stream<Specification<MinorCreditorEntity>> creditorSpecs =
            Optional.ofNullable(criteria.getCreditor())
                .map(cred -> {
                    // boolean organisation equality
                    Specification<MinorCreditorEntity> organisationEq =
                        (root, q, cb) -> cb.equal(root.get("organisation"), cred.getOrganisation());

                    // string-like fields (normalized starts-with)
                    Stream<Specification<MinorCreditorEntity>> textSpecs = Stream
                        .<Map.Entry<String, String>>of(
                            entry("defendantOrganisationName", cred.getOrganisationName()),
                            entry("forenames",                  cred.getForenames()),
                            entry("surname",                    cred.getSurname()),
                            entry("addressLine1",               cred.getAddressLine1()),
                            entry("postCode",                   cred.getPostcode())
                        )
                        .filter(e -> hasText(e.getValue()))
                        .map(e -> likeStartsWithAttr(e.getKey(), e.getValue()));

                    return Stream.concat(Stream.of(organisationEq), textSpecs);
                })
                .orElseGet(Stream::empty);

        List<Specification<MinorCreditorEntity>> parts = Stream
            .concat(
                Stream.of(optStream(businessUnitSpec), optStream(accountNumberSpec)).flatMap(s -> s),
                creditorSpecs
            )
            .toList();

        return parts.stream().reduce(Specification.allOf(), Specification::and);
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

    private static <T> Predicate likeStartsWithNormalized(Root<T> root,
                                                          CriteriaBuilder cb,
                                                          String attribute,
                                                          String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String norm = normalize(raw) + "%"; // starts-with
        return cb.like(normField, escapeForLike(norm), '\\');
    }

    private static Predicate likeStartsWithNormalized(CriteriaBuilder cb,
                                                      Expression<String> expr,
                                                      String raw) {
        Expression<String> normField = normalizeExpr(cb, expr);
        String norm = normalize(raw) + "%";
        return cb.like(normField, escapeForLike(norm), '\\');
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
