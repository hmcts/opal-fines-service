package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;

import java.util.List;

public interface PaymentInServiceInterface {

    PaymentInEntity getPaymentIn(long paymentInId);

    List<PaymentInEntity> searchPaymentIns(PaymentInSearchDto criteria);
}
