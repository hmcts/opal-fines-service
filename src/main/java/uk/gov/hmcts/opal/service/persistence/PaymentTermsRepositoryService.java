package uk.gov.hmcts.opal.service.persistence;

import jakarta.persistence.EntityNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
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

    public String getPaymentTermsAsFormattedString(Long defendantAccountId) {
        Optional<PaymentTermsEntity> optionalAccount = paymentTermsRepository
                .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
                    defendantAccountId
                );
        if (optionalAccount.isEmpty()) {
            return null;
        }
        PaymentTermsEntity account = optionalAccount.get();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return switch (account.getTermsTypeCode()) {
            case PAID -> null;
            case BY_DATE -> account.getEffectiveDate().format(formatter);
            case INSTALMENTS -> formatInstalments(account, formatter);
        };
    }

    private String formatInstalments(PaymentTermsEntity account, DateTimeFormatter formatter) {
        StringBuilder sb = new StringBuilder(account.getInstalmentAmount().toPlainString())
            .append(" per ")
            .append(account.getInstalmentPeriod().name().toLowerCase())
            .append(" from ")
            .append(account.getEffectiveDate().format(formatter));

        if (account.getInstalmentLumpSum() != null) {
            sb.append(" following a lump sum of ")
                .append(account.getInstalmentLumpSum().toPlainString());
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public List<PaymentTermsEntity> findAll(Specification<PaymentTermsEntity> specification) {
        return paymentTermsRepository.findAll(specification);
    }
}
