package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_TRANSACTIONS;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.ControlTotalRepository;
import uk.gov.hmcts.opal.repository.CourtFeesReceivedRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;

@Service
@Slf4j(topic = "opal.SuspenseItemDeletionService")
@RequiredArgsConstructor
public class SuspenseItemDeletionService {
    private final SuspenseItemRepository suspenseItemRepository;
    private final SuspenseTransactionRepository suspenseTransactionRepository;
    private final CourtFeesReceivedRepository courtFeesReceivedRepository;
    private final ControlTotalRepository controlTotalRepository;
    private final NoteRepository noteRepository;
    private final EntityManager em;

    @Transactional
    public void deleteSuspenseItem(Long suspenseItemId) {
        if  (suspenseItemId == null || suspenseItemId <= 0L) {
            throw new InvalidReferenceValidationException(
                "Invalid Suspense Item ID, it must be a positive integer: "  + suspenseItemId);
        }
        Optional<SuspenseItemEntity> suspenseItemEntityOptional = suspenseItemRepository.findById(suspenseItemId);
        if (suspenseItemEntityOptional.isEmpty()) {
            log.warn("Suspense Item with id {} not found", suspenseItemId);
            return;
        }

        SuspenseItemEntity suspenseItem =  suspenseItemEntityOptional.get();
        List<SuspenseTransactionEntity> suspenseTransactions = suspenseTransactionRepository
            .findAllBySuspenseItem(suspenseItem);

        final long courtFeeReceivedCount = courtFeesReceivedRepository.deleteAllBySuspenseTransactionIn(
            suspenseTransactions);
        final long controlTotalCount = controlTotalRepository.deleteAllByAssociatedRecordTypeAndAssociatedRecordIdIn(
            SUSPENSE_TRANSACTIONS.getLabel(), suspenseTransactions.stream()
                .map(transaction -> transaction.getSuspenseTransactionId().toString())
                .collect(Collectors.toSet()));
        final long noteCount = noteRepository.deleteAllByAssociatedRecordTypeAndAssociatedRecordId(
            SUSPENSE_ITEMS.getLabel(), suspenseItemId.toString());
        suspenseTransactionRepository.deleteAll(suspenseTransactions);
        suspenseItemRepository.delete(suspenseItem);

        em.flush();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Suspense Item with id {} deleted", suspenseItemId);
                log.info("{} Suspense Transactions deleted", suspenseTransactions.size());
                log.info("{} Court Fees Received deleted", courtFeeReceivedCount);
                log.info("{} Control Totals deleted", controlTotalCount);
                log.info("{} Notes deleted", noteCount);
            }
        });
    }
}
