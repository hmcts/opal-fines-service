package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.PaymentTermsServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTermsService implements PaymentTermsServiceInterface {

    private final PaymentTermsRepository paymentTermsRepository;

    @Override
    public PaymentTermsEntity getPaymentTerms(long paymentTermsId) {
        return paymentTermsRepository.getReferenceById(paymentTermsId);
    }

    @Override
    public List<PaymentTermsEntity> searchPaymentTerms(PaymentTermsSearchDto criteria) {
        return null;
    }

}
