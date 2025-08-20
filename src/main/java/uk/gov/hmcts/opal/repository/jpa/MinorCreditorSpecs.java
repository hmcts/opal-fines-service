package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MinorCreditorSpecs extends EntitySpecs<MinorCreditorEntity> {

    private static final String[] STRIP_CHARS = {
        " ", "-", "'", ".", ",", "/", "\\", "(", ")", "[", "]", "{", "}",
        ":", ";", "!", "?", "\"", "@", "#", "$", "%", "&", "*", "+"
    };

    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch criteria) {
        if (criteria == null) {
            return Specification.allOf(); // empty AND spec
        }

        Specification<MinorCreditorEntity> spec = Specification.allOf();

        // 1) businessUnitIds -> IN
        if (criteria.getBusinessUnitIds() != null && !criteria.getBusinessUnitIds().isEmpty()) {
            List<Short> ids = criteria.getBusinessUnitIds().stream()
                .filter(Objects::nonNull)
                .map(i -> (short) i.intValue())
                .collect(Collectors.toList());

            spec = spec.and((root, query, cb) -> root.get("businessUnitId").in(ids));
        }

        // 2) accountNumber
        spec = andIfPresent(spec, "accountNumber", criteria.getAccountNumber());

        // 3) creditor sub-object
        Creditor cred = criteria.getCreditor();
        if (cred != null) {
            if (cred.getOrganisation() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("organisation"), cred.getOrganisation()));
            }
            spec = andIfPresent(spec, "defendantOrganisationName", cred.getOrganisationName());
            spec = andIfPresent(spec, "forenames", cred.getForenames());
            spec = andIfPresent(spec, "surname", cred.getSurname());
            spec = andIfPresent(spec, "addressLine1", cred.getAddressLine1());
            spec = andIfPresent(spec, "postCode", cred.getPostcode());
        }

        return spec;
    }

    private Specification<MinorCreditorEntity> andIfPresent(
        Specification<MinorCreditorEntity> base,
        String attribute,
        String value
    ) {
        if (value == null || value.isBlank()) return base;
        return base.and((root, query, cb) -> likeStartsWithNormalized(root, cb, attribute, value));
    }

    /** AC3b: starts-with, AC3c: case-insensitive, AC3d: ignore special chars */
    private Predicate likeStartsWithNormalized(Root<MinorCreditorEntity> root,
                                               CriteriaBuilder cb,
                                               String attribute,
                                               String raw) {
        Expression<String> normField = normalizeExpr(cb, root.get(attribute));
        String norm = normalize(raw) + "%"; // starts-with
        return cb.like(normField, escapeForLike(norm), '\\');
    }

    private String normalize(String s) {
        String out = s.toLowerCase(Locale.ROOT);
        for (String ch : STRIP_CHARS) out = out.replace(ch, "");
        return out;
    }

    private Expression<String> normalizeExpr(CriteriaBuilder cb, Expression<String> expr) {
        Expression<String> e = cb.lower(expr);
        for (String ch : STRIP_CHARS) {
            e = cb.function("REPLACE", String.class, e, cb.literal(ch), cb.literal(""));
        }
        return e;
    }

    private String escapeForLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
