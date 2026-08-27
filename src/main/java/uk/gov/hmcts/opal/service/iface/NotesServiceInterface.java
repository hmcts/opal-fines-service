package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.service.AccountNoteContext;

public interface NotesServiceInterface {

    String addNote(AddNoteRequestNotes request, String ifMatch, UserState user, AccountNoteContext target);

}
