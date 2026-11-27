package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.exception.InvalidReferenceValidationException;
import uk.gov.hmcts.opal.repository.ControlTotalRepository;
import uk.gov.hmcts.opal.repository.CourtFeesReceivedRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;

@ExtendWith(MockitoExtension.class)
class SuspenseItemDeletionServiceTest {

    @Mock
    private SuspenseItemRepository suspenseItemRepository;

    @Mock
    private SuspenseTransactionRepository suspenseTransactionRepository;

    @Mock
    private CourtFeesReceivedRepository courtFeesReceivedRepository;

    @Mock
    private ControlTotalRepository controlTotalRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SuspenseItemDeletionService suspenseItemDeletionService;

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deleteSuspenseItem_whenIdIsNull_throwsInvalidReferenceValidationException() {
        assertThrows(InvalidReferenceValidationException.class,
            () -> suspenseItemDeletionService.deleteSuspenseItem(null));

        verifyNoInteractions(suspenseItemRepository, suspenseTransactionRepository, courtFeesReceivedRepository,
            controlTotalRepository, noteRepository, entityManager);
    }

    @Test
    void deleteSuspenseItem_whenIdIsZero_throwsInvalidReferenceValidationException() {
        assertThrows(InvalidReferenceValidationException.class,
            () -> suspenseItemDeletionService.deleteSuspenseItem(0L));

        verifyNoInteractions(suspenseItemRepository, suspenseTransactionRepository, courtFeesReceivedRepository,
            controlTotalRepository, noteRepository, entityManager);
    }

    @Test
    void deleteSuspenseItem_deletesAllRelatedRecordsAndFlushes() {
        SuspenseItemEntity suspenseItem = SuspenseItemEntity.builder().suspenseItemId(1L).build();
        SuspenseTransactionEntity suspenseTransaction = SuspenseTransactionEntity.builder()
            .suspenseTransactionId(2L)
            .suspenseItem(suspenseItem)
            .build();
        List<SuspenseTransactionEntity> suspenseTransactions = List.of(suspenseTransaction);

        when(suspenseItemRepository.findById(1L)).thenReturn(Optional.of(suspenseItem));
        when(suspenseTransactionRepository.findAllBySuspenseItem(suspenseItem)).thenReturn(suspenseTransactions);
        when(courtFeesReceivedRepository.deleteAllBySuspenseTransactionIn(suspenseTransactions)).thenReturn(1L);
        when(controlTotalRepository.deleteAllByAssociatedRecordTypeAndAssociatedRecordIdIn(
            "suspense_transactions", Set.of("2"))).thenReturn(1L);
        when(noteRepository.deleteAllByAssociatedRecordTypeAndAssociatedRecordId(
            "suspense_items", "1")).thenReturn(1L);

        TransactionSynchronizationManager.initSynchronization();

        suspenseItemDeletionService.deleteSuspenseItem(1L);

        InOrder deletionOrder = inOrder(courtFeesReceivedRepository, controlTotalRepository, noteRepository,
            suspenseTransactionRepository, suspenseItemRepository, entityManager);
        deletionOrder.verify(courtFeesReceivedRepository).deleteAllBySuspenseTransactionIn(suspenseTransactions);
        deletionOrder.verify(controlTotalRepository).deleteAllByAssociatedRecordTypeAndAssociatedRecordIdIn(
            "suspense_transactions", Set.of("2"));
        deletionOrder.verify(noteRepository).deleteAllByAssociatedRecordTypeAndAssociatedRecordId(
            "suspense_items", "1");
        deletionOrder.verify(suspenseTransactionRepository).deleteAll(suspenseTransactions);
        deletionOrder.verify(suspenseItemRepository).delete(suspenseItem);
        deletionOrder.verify(entityManager).flush();
    }
}
