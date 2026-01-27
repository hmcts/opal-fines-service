package uk.gov.hmcts.opal.service.opal;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.service.iface.PaymentTermsServiceInterface;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.PaymentTermsService")
@Qualifier("paymentTermsService")
public class PaymentTermsService implements PaymentTermsServiceInterface {

    @Autowired
    private final DefendantAccountPaymentTermsRepository paymentTermsRepository;

    @Override
    public PaymentTermsEntity addPaymentTerm(PaymentTermsEntity paymentTermsEntity) {
        // Set new one to active
        paymentTermsEntity.setActive(Boolean.TRUE);

        // Set posted_details metadata
        if (paymentTermsEntity.getPostedDate() == null) {
            paymentTermsEntity.setPostedDate(LocalDateTime.now());
        }
        paymentTermsEntity.setPostedBy(paymentTermsEntity.getPostedBy());
        paymentTermsEntity.setPostedByUsername(paymentTermsEntity.getPostedByUsername());

        return paymentTermsRepository.save(paymentTermsEntity);
    }

    public void deactivateExistingActivePaymentTerms(Long defendantAccountId) {
        paymentTermsRepository.deactivateActiveByDefendantAccountId(defendantAccountId);
    }
}
