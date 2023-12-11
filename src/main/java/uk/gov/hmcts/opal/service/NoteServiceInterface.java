package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.NoteDto;

public interface NoteServiceInterface {
    NoteDto saveNote(NoteDto noteDto);
}
