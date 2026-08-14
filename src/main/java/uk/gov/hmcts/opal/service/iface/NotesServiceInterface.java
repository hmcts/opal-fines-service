package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;

public interface NotesServiceInterface {

    String addNote(AddNoteRequest request, String ifMatch, UserStateV2 user, DefendantAccountEntity account);

}
