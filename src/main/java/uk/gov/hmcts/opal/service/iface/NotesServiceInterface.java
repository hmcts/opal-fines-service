package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddNoteRequest;

public interface NotesServiceInterface {

    String addNote(AddNoteRequest request, Long version);

}
