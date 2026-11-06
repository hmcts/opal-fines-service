package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorLegacyEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorLegacyEntity_;

import java.util.Optional;

public class MajorCreditorSpecs extends AddressSpecs<MajorCreditorLegacyEntity> {

    public Specification<MajorCreditorLegacyEntity> findBySearchCriteria(MajorCreditorSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            numericLong(criteria.getMajorCreditorId()).map(MajorCreditorSpecs::equalsMajorCreditorId),
            numericShort(criteria.getBusinessUnitId()).map(MajorCreditorSpecs::equalsBusinessUnitId),
            notBlank(criteria.getMajorCreditorCode()).map(MajorCreditorSpecs::likeMajorCreditorCode)
        ));
    }

    public Specification<MajorCreditorLegacyEntity> referenceDataFilter(Optional<String> filter,
                                                                  Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyMajorCreditor),
            businessUnitId.map(MajorCreditorSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<MajorCreditorLegacyEntity> equalsMajorCreditorId(Long majorCreditorId) {
        return (root, query, builder) -> equalsMajorCreditorIdPredicate(root, builder, majorCreditorId);
    }

    public static Predicate equalsMajorCreditorIdPredicate(
            From<?, MajorCreditorLegacyEntity> from, CriteriaBuilder builder, Long majorCreditorId) {
        return builder.equal(from.get(MajorCreditorLegacyEntity_.majorCreditorId), majorCreditorId);
    }


    public static Specification<MajorCreditorLegacyEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            builder.equal(root.get(MajorCreditorLegacyEntity_.businessUnitId), businessUnitId);
    }



    public static Specification<MajorCreditorLegacyEntity> likeMajorCreditorCode(String majorCreditorCode) {
        return (root, query, builder) -> likeMajorCreditorCodePredicate(root, builder, majorCreditorCode);
    }

    public static Predicate likeMajorCreditorCodePredicate(
            From<?, MajorCreditorLegacyEntity> from, CriteriaBuilder builder, String majorCreditorCode) {
        return likeWildcardPredicate(
            from.get(MajorCreditorLegacyEntity_.majorCreditorCode), builder, majorCreditorCode);
    }

    public Specification<MajorCreditorLegacyEntity> likeAnyMajorCreditor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeMajorCreditorCode(filter)
        );
    }


}
