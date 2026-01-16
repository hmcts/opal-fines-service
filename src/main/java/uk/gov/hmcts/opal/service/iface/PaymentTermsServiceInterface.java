package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

public interface PaymentTermsServiceInterface {

    public PaymentTermsEntity addPaymentTerm(PaymentTermsEntity paymentTermsEntity);

    /**
     * Deactivate any existing PaymentTerms entities for this defendant account
     * that are currently active, except the newly-created one (skipPaymentTermsId).
     */
    public void deactivateExistingActivePaymentTerms(Long defendantAccountId);
}
