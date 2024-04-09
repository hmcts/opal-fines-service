package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.businessUnitNamePredicate;

public class SuspenseAccountSpecs extends EntitySpecs<SuspenseAccountEntity> {

    public Specification<SuspenseAccountEntity> findBySearchCriteria(SuspenseAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getSuspenseAccountId()).map(SuspenseAccountSpecs::equalsSuspenseAccountId),
            numericShort(criteria.getBusinessUnitId()).map(SuspenseAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(SuspenseAccountSpecs::likeBusinessUnitName),
            notBlank(criteria.getAccountNumber()).map(SuspenseAccountSpecs::likeAccountNumber)
        ));
    }

    public static Specification<SuspenseAccountEntity> equalsSuspenseAccountId(String suspenseAccountId) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseAccountEntity_.suspenseAccountId),
                                                       suspenseAccountId);
    }

    public static Specification<SuspenseAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            businessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<SuspenseAccountEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            businessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<SuspenseAccountEntity> likeAccountNumber(String accountNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseAccountEntity_.accountNumber), builder, accountNumber);
    }

    public static Join<SuspenseAccountEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, SuspenseAccountEntity> from) {
        return from.join(SuspenseAccountEntity_.businessUnit);
    }

}
