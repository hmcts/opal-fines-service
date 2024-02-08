package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.NoteDto;
import uk.gov.hmcts.opal.dto.search.NoteSearchDto;

import java.util.List;

public interface NoteServiceInterface {
    NoteDto saveNote(NoteDto noteDto);

    List<NoteDto> searchNotes(NoteSearchDto searchCriteria);
}
