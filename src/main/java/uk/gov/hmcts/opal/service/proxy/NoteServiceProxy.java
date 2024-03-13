package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;
import uk.gov.hmcts.opal.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.NoteServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyNoteService;
import uk.gov.hmcts.opal.service.opal.NoteService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("noteServiceProxy")
public class NoteServiceProxy implements NoteServiceInterface, ProxyInterface {

    private final NoteService opalNoteService;
    private final LegacyNoteService legacyNoteService;
    private final DynamicConfigService dynamicConfigService;

    private NoteServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyNoteService : opalNoteService;
    }

    @Override
    @FeatureToggle(feature = "add-notes", value = true)
    public NoteDto saveNote(NoteDto noteDto) {
        return getCurrentModeService().saveNote(noteDto);
    }

    @Override
    public List<NoteDto> searchNotes(NoteSearchDto searchCriteria) {
        return getCurrentModeService().searchNotes(searchCriteria);
    }
}
