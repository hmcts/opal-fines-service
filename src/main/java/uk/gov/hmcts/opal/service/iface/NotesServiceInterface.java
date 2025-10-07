package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

public interface NotesServiceInterface {

    String addNote(AddNoteRequest request, String ifMatch, UserState user, DefendantAccountEntity account);

}
