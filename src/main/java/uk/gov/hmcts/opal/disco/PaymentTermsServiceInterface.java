package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;

import java.util.List;

public interface PaymentTermsServiceInterface {

    PaymentTermsEntity getPaymentTerms(long paymentTermsId);

    List<PaymentTermsEntity> searchPaymentTerms(PaymentTermsSearchDto criteria);
}
