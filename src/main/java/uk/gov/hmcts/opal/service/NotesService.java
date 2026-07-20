package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@Service
@Slf4j(topic = "opal.NotesService")
@RequiredArgsConstructor
public class NotesService {

    private final NotesProxy notesProxy;
    private final UserStateService userStateService;
    private final AccountNoteContextFactory accountNoteContextFactory;

    public String addNote(AddNoteRequest request, String ifMatch, Short businessUnitId) {
        log.debug(":addNote:");

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        AccountNoteContext target = accountNoteContextFactory.from(request.getActivityNote());

        if (!userState.hasBusinessUnitUserWithPermission(
            businessUnitId, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.ADD_ACCOUNT_ACTIVITY_NOTES);
        }

        return notesProxy.addNote(request, ifMatch, userState, target);
    }
}
