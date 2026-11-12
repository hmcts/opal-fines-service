package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;

public interface NotesServiceInterface {

    String addNote(AddNoteRequestNotes request, String ifMatch, UserStateV2 user, Short businessUnitId);

}
