package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.repository.AccountTransferRepository;
import uk.gov.hmcts.opal.repository.AllocationRepository;
import uk.gov.hmcts.opal.repository.BacsPaymentRepository;
import uk.gov.hmcts.opal.repository.ChequeRepository;
import uk.gov.hmcts.opal.repository.CommittalWarrantProgressRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPartiesRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

import java.util.List;

@Service
@Transactional
@Slf4j(topic = "opal.DefendantAccountDeletionService")
@RequiredArgsConstructor
public class DefendantAccountDeletionService {

    // Level 1 - Parent
    private final DefendantAccountRepository defendantAccountRepository;

    // Level 2 - Direct children
    private final DefendantAccountPartiesRepository defendantAccountPartiesRepository;
    private final DefendantTransactionRepository defendantTransactionRepository;
    private final PaymentTermsRepository paymentTermsRepository;
    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;
    private final AccountTransferRepository accountTransferRepository;
    private final EnforcementRepository enforcementRepository;
    private final CommittalWarrantProgressRepository committalWarrantProgressRepository;
    private final ImpositionRepository impositionRepository;
    private final PaymentCardRequestRepository paymentCardRequestsRepository;

    // Level 3 - Grandchildren
    private final AllocationRepository allocationsRepository;
    private final ChequeRepository chequeRepository;
    private final BacsPaymentRepository bacsPaymentsRepository;


    public void deleteDefendantAccountAndAssociatedData(long defendantAccountId) {
        log.warn("DESTRUCTIVE OPERATION: Deleting defendant account {} and ALL associated data", defendantAccountId);

        validateAccountExists(defendantAccountId);

        deleteLevel3Data(defendantAccountId);
        deleteLevel2Data(defendantAccountId);
        deleteLevel1Data(defendantAccountId);

        log.warn("COMPLETED: Deleted defendant account {} and all associated data", defendantAccountId);
    }

    private void validateAccountExists(long defendantAccountId) {
        if (!defendantAccountRepository.existsById(defendantAccountId)) {
            throw new EntityNotFoundException("DefendantAccount not found: " + defendantAccountId);
        }
    }

    private void deleteLevel3Data(long defendantAccountId) {
        log.debug("Deleting Level 3 dependencies for defendant account {}", defendantAccountId);

        // Delete allocataions  @ManyToOne relationships
        allocationsRepository.deleteByDefendantTransaction_DefendantAccount_DefendantAccountId(defendantAccountId);
        allocationsRepository.deleteByImposition_DefendantAccount_DefendantAccountId(defendantAccountId);

        // Delete entities with many to one transaction relationship via defendantTransactionId field
        List<Long> defendantTransactionIds =
            defendantTransactionRepository.findDefendantAccountTransactionIdsByDefendantAccountId(defendantAccountId);

        for (Long transactionId : defendantTransactionIds) {
            chequeRepository.deleteByDefendantTransactionId(transactionId);
            bacsPaymentsRepository.deleteByDefendantTransactionId(transactionId);
        }

        log.debug("Completed Level 3 deletion for defendant account {}", defendantAccountId);
    }

    private void deleteLevel2Data(long defendantAccountId) {
        log.debug("Deleting Level 2 dependencies for defendant account {}", defendantAccountId);

        // Entities with @ManyToOne defendantAccount relationships
        accountTransferRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        defendantAccountPartiesRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        enforcementRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        impositionRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        paymentTermsRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        defendantTransactionRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);

        // Entities with simple Long defendantAccountId fields
        committalWarrantProgressRepository.deleteByDefendantAccountId(defendantAccountId);
        fixedPenaltyOffenceRepository.deleteByDefendantAccountId(defendantAccountId);
        paymentCardRequestsRepository.deleteByDefendantAccountId(defendantAccountId);


        log.debug("Completed Level 2 deletion for defendant account {}", defendantAccountId);
    }

    private void deleteLevel1Data(long defendantAccountId) {
        log.debug("Deleting parent defendant account {}", defendantAccountId);
        defendantAccountRepository.deleteById(defendantAccountId);

    }
}
