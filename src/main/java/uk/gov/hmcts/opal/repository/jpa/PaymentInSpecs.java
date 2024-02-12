package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity_;

public class PaymentInSpecs extends EntitySpecs<PaymentInEntity> {

    public Specification<PaymentInEntity> findBySearchCriteria(PaymentInSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPaymentInId()).map(PaymentInSpecs::equalsPaymentInId),
            notBlank(criteria.getPaymentMethod()).map(PaymentInSpecs::equalsPaymentMethod),
            notBlank(criteria.getDestinationType()).map(PaymentInSpecs::equalsDestinationType),
            notBlank(criteria.getAllocationType()).map(PaymentInSpecs::equalsAllocationType),
            notBlank(criteria.getAssociatedRecordType()).map(PaymentInSpecs::equalsAssociatedRecordType),
            notBlank(criteria.getThirdPartyPayerName()).map(PaymentInSpecs::equalsThirdPartyPayerName),
            notBlank(criteria.getAdditionalInformation()).map(PaymentInSpecs::equalsAdditionalInformation)
        ));
    }

    public static Specification<PaymentInEntity> equalsPaymentInId(String paymentInId) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.paymentInId), paymentInId);
    }

    public static Specification<PaymentInEntity> equalsPaymentMethod(String paymentMethod) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.paymentMethod), paymentMethod);
    }

    public static Specification<PaymentInEntity> equalsDestinationType(String destinationType) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.destinationType), destinationType);
    }

    public static Specification<PaymentInEntity> equalsAllocationType(String allocationType) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.allocationType), allocationType);
    }

    public static Specification<PaymentInEntity> equalsAssociatedRecordType(String associatedRecordType) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.associatedRecordType),
                                                       associatedRecordType);
    }

    public static Specification<PaymentInEntity> equalsThirdPartyPayerName(String thirdPartyPayerName) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.thirdPartyPayerName),
                                                       thirdPartyPayerName);
    }

    public static Specification<PaymentInEntity> equalsAdditionalInformation(String additionalInformation) {
        return (root, query, builder) -> builder.equal(root.get(PaymentInEntity_.additionalInformation),
                                                       additionalInformation);
    }

}
