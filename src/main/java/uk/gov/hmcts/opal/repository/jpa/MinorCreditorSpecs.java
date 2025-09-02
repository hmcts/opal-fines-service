package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity_;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.hasText;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;

@Component
public class MinorCreditorSpecs {

    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch criteria) {
        if (criteria == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search criteria must be provided");
        }

        List<Specification<MinorCreditorEntity>> parts = new ArrayList<>();

        byBusinessUnitIds(criteria).ifPresent(parts::add);
        byCreditorAccountNumber(criteria).ifPresent(parts::add);
        parts.addAll(byCreditorTextFields(criteria));

        return combineAnd(parts);
    }

    private Optional<Specification<MinorCreditorEntity>> byBusinessUnitIds(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getBusinessUnitIds())
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(i -> (short) i.intValue())
                .toList())
            .filter(ids -> !ids.isEmpty())
            .map(ids -> (root, q, cb) -> root.get(MinorCreditorEntity_.BUSINESS_UNIT_ID).in(ids));
    }

    private Optional<Specification<MinorCreditorEntity>> byCreditorAccountNumber(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getAccountNumber())
            .filter(SpecificationUtils::hasText)
            .map(SpecificationUtils::stripCheckLetter)
            .map(prefix -> (Specification<MinorCreditorEntity>)
                (root, q, cb) -> likeStartsWithNormalized(root, cb, MinorCreditorEntity_.ACCOUNT_NUMBER, prefix));
    }

    private List<Specification<MinorCreditorEntity>> byCreditorTextFields(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .map(cred -> {
                List<Specification<MinorCreditorEntity>> out = new ArrayList<>();

                // Organisation name
                addTextFilterIfPresent(
                    out,
                    MinorCreditorEntity_.ORGANISATION_NAME,
                    cred.getOrganisationName(),
                    Boolean.TRUE.equals(cred.getExactMatchOrganisationName())
                );

                // Forenames
                addTextFilterIfPresent(
                    out,
                    MinorCreditorEntity_.FORENAMES,
                    cred.getForenames(),
                    Boolean.TRUE.equals(cred.getExactMatchForenames())
                );

                // Surname
                addTextFilterIfPresent(
                    out,
                    MinorCreditorEntity_.SURNAME,
                    cred.getSurname(),
                    Boolean.TRUE.equals(cred.getExactMatchSurname())
                );

                // Address & Postcode (no exact flags; keep starts-with)
                addStartsWithIfPresent(out, MinorCreditorEntity_.ADDRESS_LINE1, cred.getAddressLine1());
                addStartsWithIfPresent(out, MinorCreditorEntity_.POST_CODE,     cred.getPostcode());

                return out;
            })
            .orElseGet(List::of);
    }

    private void addTextFilterIfPresent(List<Specification<MinorCreditorEntity>> acc,
                                        String attribute,
                                        String value,
                                        boolean exactMatch) {
        if (!hasText(value)) return;

        if (exactMatch) {
            acc.add((root, q, cb) -> equalNormalized(root, cb, attribute, value));
        } else {
            acc.add((root, q, cb) -> likeStartsWithNormalized(root, cb, attribute, value));
        }
    }

    private void addStartsWithIfPresent(List<Specification<MinorCreditorEntity>> acc,
                                        String attribute,
                                        String value) {
        if (hasText(value)) {
            acc.add((root, q, cb) -> likeStartsWithNormalized(root, cb, attribute, value));
        }
    }

    /** AND all parts; require at least one filter to avoid full scans. */
    private Specification<MinorCreditorEntity> combineAnd(List<Specification<MinorCreditorEntity>> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Search request must include at least one filter"
            );
        }
        return parts.stream().reduce(Specification.allOf(), Specification::and);
    }
}
