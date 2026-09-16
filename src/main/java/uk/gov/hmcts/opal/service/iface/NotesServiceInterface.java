package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;

public interface NotesServiceInterface {

    String addNote(AddNoteRequest request, String ifMatch, UserState user, Short businessUnitId);

}
