package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.NoteDto;

@Service
@RequiredArgsConstructor
@Qualifier("noteServiceProxy")
public class NoteServiceProxy implements NoteServiceInterface {

    private final NoteService opalNoteService;
    private final LegacyNoteService legacyNoteService;
    private final DynamicConfigService dynamicConfigService;


    @Override
    public NoteDto saveNote(NoteDto noteDto) {
        if ("legacy".equals(dynamicConfigService.getAppMode().getMode())) {
            return legacyNoteService.saveNote(noteDto);
        } else {
            return opalNoteService.saveNote(noteDto);
        }
    }
}
