package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity_;

public class PaymentTermsSpecs extends EntitySpecs<PaymentTermsEntity> {

    public Specification<PaymentTermsEntity> findBySearchCriteria(PaymentTermsSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPaymentTermsId()).map(PaymentTermsSpecs::equalsPaymentTermsId)
        ));
    }

    public static Specification<PaymentTermsEntity> equalsPaymentTermsId(String paymentTermsId) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.paymentTermsId), paymentTermsId);
    }

}
