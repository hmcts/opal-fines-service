package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity_;

import java.time.LocalDateTime;
import java.util.Optional;

public class LocalJusticeAreaSpecs extends AddressSpecs<LocalJusticeAreaEntity> {

    public Specification<LocalJusticeAreaEntity> findBySearchCriteria(LocalJusticeAreaSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getLjaCode()).map(LocalJusticeAreaSpecs::likeLjaCode),
            notBlank(criteria.getLocalJusticeAreaId()).map(LocalJusticeAreaSpecs::equalsLocalJusticeAreaId)
        ));
    }

    public Specification<LocalJusticeAreaEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            Optional.of(LocalDateTime.now()).map(LocalJusticeAreaSpecs::endDateGreaterThenEqualToDate),
            filter.filter(s -> !s.isBlank()).map(this::likeAnyLocalJusticeArea)
        ));
    }

    public static Specification<LocalJusticeAreaEntity> equalsLocalJusticeAreaId(String localJusticeAreaId) {
        return (root, query, builder) -> builder.equal(root.get(LocalJusticeAreaEntity_.localJusticeAreaId),
                                                       localJusticeAreaId);
    }

    public static Specification<LocalJusticeAreaEntity> likeLjaCode(String ljaCode) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(LocalJusticeAreaEntity_.ljaCode), builder, ljaCode);
    }

    public Specification<LocalJusticeAreaEntity> likeAnyLocalJusticeArea(String filter) {
        return Specification.anyOf(
            likeLjaCode(filter),
            likeName(filter),
            likePostcode(filter)
        );
    }

    public static Specification<LocalJusticeAreaEntity> endDateGreaterThenEqualToDate(LocalDateTime expiryDate) {
        return (root, query, builder) -> builder.or(
            builder.isNull(root.get(LocalJusticeAreaEntity_.endDate)),
            builder.greaterThanOrEqualTo(root.get(LocalJusticeAreaEntity_.endDate), expiryDate)
        );
    }
}
