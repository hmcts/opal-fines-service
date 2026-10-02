package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;

public interface NotesServiceInterface {

    String addNote(AddNoteRequestNotes request, String ifMatch, UserState user, Short businessUnitId);

}
