package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

@Service
@Slf4j(topic = "opal.PaymentTermsRepositoryService")
@RequiredArgsConstructor
public class PaymentTermsRepositoryService {

    private final PaymentTermsRepository paymentTermsRepository;

    @Transactional(readOnly = true)
    public PaymentTermsEntity findLatestByDefendantAccountId(Long defendantAccountId) {
        log.debug("Finding latest PaymentTermsEntity by defendantAccountId: {}", defendantAccountId);

        return paymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Payment Terms not found for Defendant Account Id: "
                                                               + defendantAccountId));
    }
}
