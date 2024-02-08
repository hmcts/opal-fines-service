package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity_;

public class PaymentInSpecs extends EntitySpecs<PaymentInEntity> {

    public Specification<PaymentInEntity> findBySearchCriteria(PaymentInSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPaymentInId()).map(PaymentInSpecs::equalsPaymentInId)
        ));
    }

    public static Specification<PaymentInEntity> equalsPaymentInId(String paymentInId) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.paymentInId), paymentInId);
    }

}
