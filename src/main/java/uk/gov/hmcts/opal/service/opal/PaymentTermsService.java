package uk.gov.hmcts.opal.service.opal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    public PaymentTermsEntity addPaymentTerm(PaymentTermsEntity paymentTermsEntity, String postedBy) {

        paymentTermsEntity.setActive(false);

        // Set posted metadata
        paymentTermsEntity.setPostedDate(LocalDate.from(LocalDateTime.now()));
        paymentTermsEntity.setPostedBy(postedBy);
        paymentTermsEntity.setPostedByUsername(postedBy);

        // Persist using repository
        paymentTermsRepository.save(paymentTermsEntity);
        return paymentTermsEntity;
    }

    public void deactivateExistingActivePaymentTerms(Long defendantAccountId, Long skipPaymentTermsId) {
        // Query for active payment terms for account
        List<PaymentTermsEntity> activeOnes = paymentTermsRepository
            .findByDefendantAccount_DefendantAccountIdAndActiveTrue(defendantAccountId);

        for (PaymentTermsEntity paymentTermsEntity : activeOnes) {
            if (paymentTermsEntity.getPaymentTermsId() != null
                && paymentTermsEntity.getPaymentTermsId().equals(skipPaymentTermsId)) {
                continue; // skip the one just created
            }
            paymentTermsEntity.setActive(Boolean.FALSE);
            paymentTermsRepository.save(paymentTermsEntity);

            log.debug(":deactivateExistingActivePaymentTerms: set inactive paymentTermsId={} for account={}",
                paymentTermsEntity.getPaymentTermsId(), defendantAccountId);
        }
    }
}
