package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity_;

public class PaymentTermsSpecs extends EntitySpecs<PaymentTermsEntity> {

    public Specification<PaymentTermsEntity> findBySearchCriteria(PaymentTermsSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPaymentTermsId()).map(PaymentTermsSpecs::equalsPaymentTermsId),
            notBlank(criteria.getTermsTypeCode()).map(PaymentTermsSpecs::equalsTermsTypeCode),
            notBlank(criteria.getInstalmentPeriod()).map(PaymentTermsSpecs::equalsInstalmentPeriod),
            notBlank(criteria.getJailDays()).map(PaymentTermsSpecs::equalsJailDays),
            notBlank(criteria.getAccountBalance()).map(PaymentTermsSpecs::equalsAccountBalance)
        ));
    }

    public static Specification<PaymentTermsEntity> equalsPaymentTermsId(String paymentTermsId) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.paymentTermsId), paymentTermsId);
    }

    public static Specification<PaymentTermsEntity> equalsTermsTypeCode(String termsTypeCode) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.termsTypeCode), termsTypeCode);
    }

    public static Specification<PaymentTermsEntity> equalsInstalmentPeriod(String instalmentPeriod) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.instalmentPeriod),
                                                       instalmentPeriod);
    }

    public static Specification<PaymentTermsEntity> equalsJailDays(String jailDays) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.jailDays), jailDays);
    }

    public static Specification<PaymentTermsEntity> equalsAccountBalance(String accountBalance) {
        return (root, query, builder) -> builder.equal(root.get(PaymentTermsEntity_.accountBalance), accountBalance);
    }

}
