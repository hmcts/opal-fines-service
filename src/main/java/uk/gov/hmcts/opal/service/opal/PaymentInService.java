package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentInSearchDto;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.repository.PaymentInRepository;
import uk.gov.hmcts.opal.repository.jpa.PaymentInSpecs;
import uk.gov.hmcts.opal.service.PaymentInServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentInService implements PaymentInServiceInterface {

    private final PaymentInRepository paymentInRepository;

    private final PaymentInSpecs specs = new PaymentInSpecs();

    @Override
    public PaymentInEntity getPaymentIn(long paymentInId) {
        return paymentInRepository.getReferenceById(paymentInId);
    }

    @Override
    public List<PaymentInEntity> searchPaymentIns(PaymentInSearchDto criteria) {
        Page<PaymentInEntity> page = paymentInRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
