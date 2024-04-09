package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitNamePredicate;

public class MajorCreditorSpecs extends AddressSpecs<MajorCreditorEntity> {

    public Specification<MajorCreditorEntity> findBySearchCriteria(MajorCreditorSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getMajorCreditorId()).map(MajorCreditorSpecs::equalsMajorCreditorId),
            numericShort(criteria.getBusinessUnitId()).map(MajorCreditorSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(MajorCreditorSpecs::likeBusinessUnitName),
            numericShort(criteria.getMajorCreditorCode()).map(MajorCreditorSpecs::equalsMajorCreditorCode)
        ));
    }

    public static Specification<MajorCreditorEntity> equalsMajorCreditorId(String majorCreditorId) {
        return (root, query, builder) -> builder.equal(root.get(MajorCreditorEntity_.majorCreditorId), majorCreditorId);
    }

    public static Specification<MajorCreditorEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            businessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<MajorCreditorEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            businessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<MajorCreditorEntity> equalsMajorCreditorCode(Short majorCreditorCode) {
        return (root, query, builder) -> builder.equal(root.get(MajorCreditorEntity_.majorCreditorCode),
                                                       majorCreditorCode);
    }

    public static Join<MajorCreditorEntity, BusinessUnitEntity> joinBusinessUnit(From<?, MajorCreditorEntity> from) {
        return from.join(MajorCreditorEntity_.businessUnit);
    }

}
