package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@Service
@Slf4j(topic = "opal.NotesService")
@RequiredArgsConstructor
public class NotesService {

    private final NotesProxy notesProxy;
    private final UserStateService userStateService;

    public String addNote(AddNoteRequest request, Long version, String authHeaderValue) {
        log.debug(":addNote:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (!userState.anyBusinessUnitUserHasPermission(Permissions.ACCOUNT_MAINTENANCE)) {
            throw new PermissionNotAllowedException(Permissions.ACCOUNT_MAINTENANCE);
        }

        return notesProxy.addNote(request, version);
    }
}
