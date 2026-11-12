package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseNoteCommonStrict;
import uk.gov.hmcts.opal.generated.model.SuspenseTransactionCommonStrict;
import uk.gov.hmcts.opal.mapper.SuspenseItemHistoryMapper;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SuspenseItemRepository;
import uk.gov.hmcts.opal.repository.SuspenseTransactionRepository;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j(topic = "opal.OpalSuspenseAccountsHistoryService")
@RequiredArgsConstructor
public class OpalSuspenseAccountsHistoryService implements SuspenseAccountsHistoryServiceInterface {

    private final SuspenseItemRepository suspenseItemRepository;
    private final NoteRepository noteRepository;
    private final SuspenseTransactionRepository suspenseTransactionRepository;
    private final SuspenseItemHistoryMapper historyMapper;

    @Override
    @Transactional(readOnly = true)
    public SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemId) {
        log.info("Getting suspense item history for business unit id {} and suspense item number {}", businessUnitId,
            suspenseItemId);

        Long itemId = suspenseItemRepository
            .findItemIdForBusinessUnit(suspenseItemId, businessUnitId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Suspense item not found in the requested business unit"));

        List<SuspenseNoteCommonStrict> notes =
            noteRepository.findSuspenseItemNotes(businessUnitId, suspenseItemId)
                .stream()
                .map(historyMapper::toSuspenseNote)
                .toList();

        List<SuspenseTransactionCommonStrict> transactions =
            suspenseTransactionRepository.findHistory(itemId, businessUnitId)
                .stream()
                .map(historyMapper::toSuspenseTransaction)
                .toList();

        return new SuspenseItemHistoryCommonStrict()
            .businessUnitId(Math.toIntExact(businessUnitId))
            .suspenseItemId(Math.toIntExact(itemId))
            .suspenseNotes(notes)
            .suspenseTransactions(transactions);
    }

}
