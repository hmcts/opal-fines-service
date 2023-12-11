package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;

@Service
@RequiredArgsConstructor
@Qualifier("noteServiceProxy")
public class NoteServiceProxy implements NoteServiceInterface, LegacyProxy {

    private final NoteService opalNoteService;
    private final LegacyNoteService legacyNoteService;
    private final DynamicConfigService dynamicConfigService;

    private NoteServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyNoteService : opalNoteService;
    }

    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        return getCurrentModeService().saveNote(noteDto);
    }
}
