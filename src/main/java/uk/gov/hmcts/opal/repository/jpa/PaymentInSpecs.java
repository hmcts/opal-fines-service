package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity_;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillEntity_;

public class PaymentInSpecs extends EntitySpecs<PaymentInEntity> {

    public static Specification<PaymentInEntity> equalsTillId(Long tillId) {
        return (root, query, builder) -> equalsTillIdPredicate(root, builder, tillId);
    }

    public static Predicate equalsTillIdPredicate(From<?, PaymentInEntity> from, CriteriaBuilder builder, Long tillId) {
        return builder.equal(joinTill(from).get(TillEntity_.tillId), tillId);
    }

    public static From<PaymentInEntity, TillEntity> joinTill(From<?, PaymentInEntity> from) {
        return from.join(PaymentInEntity_.tillEntity);
    }
}
