package uk.gov.hmcts.opal.service.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;

@Service
@Slf4j(topic = "opal.PaymentCardRequestRepositoryService")
@RequiredArgsConstructor
public class PaymentCardRequestRepositoryService {

    final PaymentCardRequestRepository paymentCardRequestRepository;

    @Transactional(readOnly = true)
    public boolean existsByDefendantAccountId(Long defendantAccountId) {
        log.debug("Checking existence of PaymentCardRequestEntity by defendantAccountId: {}", defendantAccountId);
        return paymentCardRequestRepository.existsByDefendantAccountId(defendantAccountId);
    }

    @Transactional
    public PaymentCardRequestEntity save(PaymentCardRequestEntity paymentCardRequestEntity) {
        log.debug("Saving PaymentCardRequestEntity for Defendant Account ID: {}",
                  paymentCardRequestEntity.getDefendantAccountId());
        return paymentCardRequestRepository.save(paymentCardRequestEntity);
    }
}
