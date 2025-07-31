package uk.gov.hmcts.opal.disco.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PaymentTermsSearchDto;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.PaymentTermsSpecs;
import uk.gov.hmcts.opal.disco.PaymentTermsServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("paymentTermsService")
public class PaymentTermsService implements PaymentTermsServiceInterface {

    private final PaymentTermsRepository paymentTermsRepository;

    private final PaymentTermsSpecs specs = new PaymentTermsSpecs();

    @Override
    public PaymentTermsEntity getPaymentTerms(long paymentTermsId) {
        return paymentTermsRepository.getReferenceById(paymentTermsId);
    }

    @Override
    public List<PaymentTermsEntity> searchPaymentTerms(PaymentTermsSearchDto criteria) {
        Page<PaymentTermsEntity> page = paymentTermsRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
