package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;

@Service
@Slf4j(topic = "opal.NotesService")
@RequiredArgsConstructor
public class NotesService {

    private final NotesProxy notesProxy;
    private final UserStateService userStateService;
    private final DefendantAccountRepository defendantAccountRepository;


    public String addNote(AddNoteRequest request, String ifMatch, String authHeaderValue) {
        log.debug(":addNote:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        DefendantAccountEntity account =
            defendantAccountRepository
                .findById(Long.valueOf(request.getActivityNote().getRecordId()))
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Account %s not found".formatted(request.getActivityNote().getRecordId())
                ));

        if (!userState.hasBusinessUnitUserWithPermission(
            account.getBusinessUnit().getBusinessUnitId(), Permissions.ACCOUNT_MAINTENANCE)) {
            throw new PermissionNotAllowedException(Permissions.ACCOUNT_MAINTENANCE);
        }

        return notesProxy.addNote(request, ifMatch, userState, account);
    }
}
