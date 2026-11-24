package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.AddNoteRequestNotes;
import uk.gov.hmcts.opal.generated.model.NoteCommon;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@Service
@Slf4j(topic = "opal.NotesService")
@RequiredArgsConstructor
public class NotesService {

    private final NotesProxy notesProxy;
    private final UserStateService userStateService;

    public String addNote(AddNoteRequestNotes request, String ifMatch, Short businessUnitId) {
        log.debug(":addNote:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        FinesPermission permission =
            request.getActivityNote().getRecordType() == NoteCommon.RecordTypeEnum.SUSPENSE_ITEMS
            ? FinesPermission.MANAGE_SUSPENSE_ITEMS : FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES;
        if (!userState.hasBusinessUnitUserWithPermission(businessUnitId, permission)) {
            throw new PermissionNotAllowedException(businessUnitId, permission);
        }

        return notesProxy.addNote(request, ifMatch, userState, businessUnitId);
    }
}
