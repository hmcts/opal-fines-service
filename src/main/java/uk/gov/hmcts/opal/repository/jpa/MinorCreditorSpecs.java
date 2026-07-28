package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.hasText;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.dto.Creditor;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity_;

@Component
public class MinorCreditorSpecs extends EntitySpecs<MinorCreditorEntity> {

    public Specification<MinorCreditorEntity> findBySearchCriteria(MinorCreditorSearch criteria) {
        if (criteria == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search criteria must be provided");
        }

        return combineAnd(specificationList(
            byBusinessUnitIds(criteria),
            byCreditorAccountNumber(criteria),
            byOrganisationName(criteria),
            byForenames(criteria),
            bySurname(criteria),
            byAddressLine1(criteria),
            byPostcode(criteria)
        ));
    }

    private Optional<Specification<MinorCreditorEntity>> byBusinessUnitIds(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getBusinessUnitIds())
            .map(list -> list.stream()
                .filter(Objects::nonNull)
                .map(i -> (short) i.intValue())
                .toList())
            .filter(ids -> !ids.isEmpty())
            .map(MinorCreditorSpecs::businessUnitIdsIn);
    }

    private Optional<Specification<MinorCreditorEntity>> byCreditorAccountNumber(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getAccountNumber())
            .filter(SpecificationUtils::hasText)
            .map(SpecificationUtils::stripCheckLetter)
            .map(MinorCreditorSpecs::accountNumberStartsWith);
    }

    private Optional<Specification<MinorCreditorEntity>> byOrganisationName(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .filter(creditor -> hasText(creditor.getOrganisationName()))
            .map(creditor -> textMatches(
                MinorCreditorEntity_.ORGANISATION_NAME,
                creditor.getOrganisationName(),
                creditor.getExactMatchOrganisationName()
            ));
    }

    private Optional<Specification<MinorCreditorEntity>> byForenames(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .filter(creditor -> hasText(creditor.getForenames()))
            .map(creditor -> textMatches(
                MinorCreditorEntity_.FORENAMES,
                creditor.getForenames(),
                creditor.getExactMatchForenames()
            ));
    }

    private Optional<Specification<MinorCreditorEntity>> bySurname(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .filter(creditor -> hasText(creditor.getSurname()))
            .map(creditor -> textMatches(
                MinorCreditorEntity_.SURNAME,
                creditor.getSurname(),
                creditor.getExactMatchSurname()
            ));
    }

    private Optional<Specification<MinorCreditorEntity>> byAddressLine1(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .map(Creditor::getAddressLine1)
            .filter(SpecificationUtils::hasText)
            .map(value -> startsWith(MinorCreditorEntity_.ADDRESS_LINE1, value));
    }

    private Optional<Specification<MinorCreditorEntity>> byPostcode(MinorCreditorSearch c) {
        return Optional.ofNullable(c.getCreditor())
            .map(Creditor::getPostcode)
            .filter(SpecificationUtils::hasText)
            .map(value -> startsWith(MinorCreditorEntity_.POST_CODE, value));
    }

    public static Specification<MinorCreditorEntity> businessUnitIdsIn(List<Short> businessUnitIds) {
        return (root, query, builder) -> root.get(MinorCreditorEntity_.BUSINESS_UNIT_ID).in(businessUnitIds);
    }

    public static Specification<MinorCreditorEntity> accountNumberStartsWith(String accountNumberPrefix) {
        return startsWith(MinorCreditorEntity_.ACCOUNT_NUMBER, accountNumberPrefix);
    }

    public static Specification<MinorCreditorEntity> textMatches(
        String attributeName, String value, Boolean exactMatch) {

        return Boolean.TRUE.equals(exactMatch)
            ? (root, query, builder) -> equalNormalized(root, builder, attributeName, value)
            : startsWith(attributeName, value);
    }

    public static Specification<MinorCreditorEntity> startsWith(String attributeName, String value) {

        return (root, query, builder) -> likeStartsWithNormalized(root, builder, attributeName, value);
    }

    /** AND all parts; require at least one filter to avoid full scans. */
    private Specification<MinorCreditorEntity> combineAnd(List<Specification<MinorCreditorEntity>> parts) {
        if (parts == null || parts.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Search request must include at least one filter");
        }
        return parts.stream().reduce(Specification.allOf(), Specification::and);
    }
}
