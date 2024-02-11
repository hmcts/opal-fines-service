package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.service.PaymentInServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentInService implements PaymentInServiceInterface {

    private final PaymentInRepository paymentInRepository;

    @Override
    public PaymentInEntity getPaymentIn(long paymentInId) {
        return paymentInRepository.getReferenceById(paymentInId);
    }

    @Override
    public List<PaymentInEntity> searchPaymentIns(PaymentInSearchDto criteria) {
        return null;
    }

}
