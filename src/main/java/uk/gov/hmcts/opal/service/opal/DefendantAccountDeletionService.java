package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
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
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.repository.jpa.AllocationSpecs;
import uk.gov.hmcts.opal.repository.jpa.BacsPaymentSpecs;
import uk.gov.hmcts.opal.repository.jpa.ChequeSpecs;
import uk.gov.hmcts.opal.service.opal.jpa.CreditorAccountTransactional;

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
    private final CreditorAccountTransactional creditorAccountTransactional;
    private final PaymentCardRequestRepository paymentCardRequestsRepository;
    private final NoteRepository noteRepository;

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
            throw new EntityNotFoundException("Defendant Account not found: " + defendantAccountId);
        }
    }

    private void deleteLevel3Data(long defendantAccountId) {
        log.debug("Deleting Level 3 dependencies for defendant account {}", defendantAccountId);

        long count = 0;
        // Delete allocataions  @ManyToOne relationships
        count += allocationsRepository.delete(AllocationSpecs.equalsDefendantTransactionAccountId(defendantAccountId));
        count += allocationsRepository.delete(AllocationSpecs.equalsImpositionDefendantAccountId(defendantAccountId));

        // Delete entities with many to one transaction relationship via defendantTransactionId field
        List<Long> defendantTransactionIds =
            defendantTransactionRepository.findDefendantAccountTransactionIdsByDefendantAccountId(defendantAccountId);

        count += chequeRepository.delete(ChequeSpecs.hasDefendantTransactionIdIn(defendantTransactionIds));
        count += bacsPaymentsRepository.delete(BacsPaymentSpecs.hasDefendantTransactionIdIn(defendantTransactionIds));

        log.debug("Completed Level 3 deletion of {} rows for defendant account {}", count, defendantAccountId);
    }

    private void deleteLevel2Data(long defendantAccountId) {
        log.debug("Deleting Level 2 dependencies for defendant account {}", defendantAccountId);

        // Entities with @ManyToOne defendantAccount relationships
        accountTransferRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        defendantAccountPartiesRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        enforcementRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        creditorAccountTransactional.deleteAllByDefendantAccountId(defendantAccountId, creditorAccountTransactional);
        paymentTermsRepository.deleteByDefendantAccount_DefendantAccountId(defendantAccountId);
        defendantTransactionRepository.deleteByDefendantAccountId(defendantAccountId);

        // Entities with simple Long defendantAccountId fields
        committalWarrantProgressRepository.deleteByDefendantAccountId(defendantAccountId);
        fixedPenaltyOffenceRepository.deleteByDefendantAccountId(defendantAccountId);
        paymentCardRequestsRepository.deleteByDefendantAccountId(defendantAccountId);
        noteRepository.deleteByAssociatedRecordId(String.valueOf(defendantAccountId));

        log.debug("Completed Level 2 deletion for defendant account {}", defendantAccountId);
    }

    private void deleteLevel1Data(long defendantAccountId) {
        log.debug("Deleting parent defendant account {}", defendantAccountId);
        defendantAccountRepository.deleteById(defendantAccountId);

    }
}
