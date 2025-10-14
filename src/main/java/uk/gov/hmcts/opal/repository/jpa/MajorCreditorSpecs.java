package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorFullEntity;
import uk.gov.hmcts.opal.entity.majorcreditor.MajorCreditorEntity_;

import java.util.Optional;

public class MajorCreditorSpecs extends AddressSpecs<MajorCreditorFullEntity> {

    public Specification<MajorCreditorFullEntity> findBySearchCriteria(MajorCreditorSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            numericLong(criteria.getMajorCreditorId()).map(MajorCreditorSpecs::equalsMajorCreditorId),
            numericShort(criteria.getBusinessUnitId()).map(MajorCreditorSpecs::equalsBusinessUnitId),
            notBlank(criteria.getMajorCreditorCode()).map(MajorCreditorSpecs::likeMajorCreditorCode)
        ));
    }

    public Specification<MajorCreditorFullEntity> referenceDataFilter(Optional<String> filter,
                                                                      Optional<Short> businessUnitId) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyMajorCreditor),
            businessUnitId.map(MajorCreditorSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<MajorCreditorFullEntity> equalsMajorCreditorId(Long majorCreditorId) {
        return (root, query, builder) -> equalsMajorCreditorIdPredicate(root, builder, majorCreditorId);
    }

    public static Predicate equalsMajorCreditorIdPredicate(
            From<?, MajorCreditorFullEntity> from, CriteriaBuilder builder, Long majorCreditorId) {
        return builder.equal(from.get(MajorCreditorEntity_.majorCreditorId), majorCreditorId);
    }


    public static Specification<MajorCreditorFullEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            builder.equal(root.get(MajorCreditorEntity_.businessUnitId), businessUnitId);
    }



    public static Specification<MajorCreditorFullEntity> likeMajorCreditorCode(String majorCreditorCode) {
        return (root, query, builder) -> likeMajorCreditorCodePredicate(root, builder, majorCreditorCode);
    }

    public static Predicate likeMajorCreditorCodePredicate(
            From<?, MajorCreditorFullEntity> from, CriteriaBuilder builder, String majorCreditorCode) {
        return likeWildcardPredicate(from.get(MajorCreditorEntity_.majorCreditorCode), builder, majorCreditorCode);
    }

    public Specification<MajorCreditorFullEntity> likeAnyMajorCreditor(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeMajorCreditorCode(filter)
        );
    }


}
