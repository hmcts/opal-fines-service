package uk.gov.hmcts.opal.service.opal.history.source;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.mapper.history.NoteEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.NoteRepository;

@Service
@RequiredArgsConstructor
public class NoteHistorySourceService {

    private final NoteRepository noteRepository;
    private final NoteEntityHistoryMapper noteEntityHistoryMapper;

    @Transactional(readOnly = true)
    public List<DefendantAccountHistoryItem> fetch(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.NOTE)) {
            return List.of();
        }

        return noteRepository.findDefendantAccountHistoryNotes(
                defendantAccountId.toString(),
                filter.getDateFrom(),
                filter.getDateTo()
            ).stream()
            .map(noteEntityHistoryMapper::toHistoryItem)
            .toList();
    }
}
