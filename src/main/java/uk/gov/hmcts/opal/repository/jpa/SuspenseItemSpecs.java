package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity_;

import static uk.gov.hmcts.opal.repository.jpa.SuspenseAccountSpecs.equalsSuspenseAccountIdPredicate;

public class SuspenseItemSpecs extends EntitySpecs<SuspenseItemEntity> {

    public Specification<SuspenseItemEntity> findBySearchCriteria(SuspenseItemSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getSuspenseItemId()).map(SuspenseItemSpecs::equalsSuspenseItemId),
            numericLong(criteria.getSuspenseAccountId()).map(SuspenseItemSpecs::equalsSuspenseAccountId),
            numericShort(criteria.getSuspenseItemNumber()).map(SuspenseItemSpecs::equalsSuspenseItemNumber),
            notBlank(criteria.getSuspenseItemType()).map(SuspenseItemSpecs::likeSuspenseItemType),
            notBlank(criteria.getPaymentMethod()).map(SuspenseItemSpecs::likePaymentMethod),
            numericLong(criteria.getCourtFeeId()).map(SuspenseItemSpecs::equalsCourtFeeId)
        ));
    }

    public static Specification<SuspenseItemEntity> equalsSuspenseItemId(Long suspenseItemId) {
        return (root, query, builder) -> equalsSuspenseItemIdPredicate(root, builder, suspenseItemId);
    }

    public static Predicate equalsSuspenseItemIdPredicate(From<?, SuspenseItemEntity> from, CriteriaBuilder builder,
                                                          Long suspenseItemId) {
        return builder.equal(from.get(SuspenseItemEntity_.suspenseItemId), suspenseItemId);
    }

    public static Specification<SuspenseItemEntity> equalsSuspenseAccountId(Long suspenseAccountId) {
        return (root, query, builder) -> equalsSuspenseAccountIdPredicate(joinSuspenseAccount(root), builder,
                                                                          suspenseAccountId);
    }

    public static Specification<SuspenseItemEntity> equalsSuspenseItemNumber(Short suspenseItemNumber) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseItemEntity_.suspenseItemNumber),
                                                       suspenseItemNumber);
    }

    public static Specification<SuspenseItemEntity> likeSuspenseItemType(String suspenseItemType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseItemEntity_.suspenseItemType), builder, suspenseItemType);
    }

    public static Specification<SuspenseItemEntity> likePaymentMethod(String paymentMethod) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(SuspenseItemEntity_.paymentMethod), builder, paymentMethod);
    }

    public static Specification<SuspenseItemEntity> equalsCourtFeeId(Long courtFeeId) {
        return (root, query, builder) -> builder.equal(root.get(SuspenseItemEntity_.courtFeeId), courtFeeId);
    }

    public static Join<SuspenseItemEntity, SuspenseAccountEntity> joinSuspenseAccount(Root<SuspenseItemEntity> root) {
        return root.join(SuspenseItemEntity_.suspenseAccount);
    }
}
