package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;

public class SuspenseAccountSpecs extends EntitySpecs<SuspenseAccountEntity> {

    public Specification<SuspenseAccountEntity> findBySearchCriteria(SuspenseAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getSuspenseAccountId()).map(SuspenseAccountSpecs::equalsSuspenseAccountId),
            numericShort(criteria.getBusinessUnitId()).map(SuspenseAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(SuspenseAccountSpecs::likeBusinessUnitName),
            notBlank(criteria.getAccountNumber()).map(SuspenseAccountSpecs::likeAccountNumber)
        ));
    }

    public static Specification<SuspenseAccountEntity> equalsSuspenseAccountId(Long suspenseAccountId)  {
        return (root, query, builder) -> equalsSuspenseAccountIdPredicate(root, builder, suspenseAccountId);
    }

    public static Predicate equalsSuspenseAccountIdPredicate(From<?, SuspenseAccountEntity> from,
                                                             CriteriaBuilder builder, Long suspenseAccountId) {
        return builder.equal(from.get(SuspenseAccountEntity_.suspenseAccountId), suspenseAccountId);
    }

    public static Specification<SuspenseAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<SuspenseAccountEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
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
