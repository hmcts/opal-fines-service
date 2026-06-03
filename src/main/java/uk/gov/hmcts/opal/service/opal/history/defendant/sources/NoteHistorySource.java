package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.mapper.history.NoteEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;

@Service
@RequiredArgsConstructor
public class NoteHistorySource implements AccountHistorySource {

    private final NoteRepository noteRepository;
    private final NoteEntityHistoryMapper noteEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public HistoryItemType getItemType() {
        return HistoryItemType.NOTE;
    }

    @Override
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        return noteRepository.findDefendantAccountHistoryNotes(
                context.getAccountId().toString(),
                filter.getDateFrom(),
                filter.getDateTo()
            ).stream()
            .map(noteEntityHistoryMapper::toHistoryItem)
            .map(DefendantAccountHistoryModelAdapter::toCoreItem)
            .toList();
    }
}
