package uk.gov.hmcts.opal.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.opal.OpalMinorCreditorService;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class MinorCreditorService {

    public static final String MINOR_CREDITOR_DELETED_MESSAGE_FORMAT = """
        { "message": "Minor Creditor '%s' deleted"}""";

    private final MinorCreditorSearchProxy minorCreditorSearchProxy;

    // Only for deletions that are only used in testing
    private final OpalMinorCreditorService opalMinorCreditorService;

    private final UserStateService userStateService;

    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch entity,
                                                                        String authHeaderValue) {

        log.debug(":searchMinorCreditor:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.searchMinorCreditors(entity);
        } else {
            throw new PermissionNotAllowedException(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public String deleteMinorCreditor(long minorCreditorId, boolean checkExisted, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        try {
            boolean deleted =  opalMinorCreditorService.deleteMinorCreditor(minorCreditorId, opalMinorCreditorService);
            if (deleted) {
                log.debug(":deleteMinorCreditor: Deleted Draft Account: {}", minorCreditorId);
            }
        } catch (UnexpectedRollbackException | EntityNotFoundException ure) {
            if (checkExisted) {
                throw ure;
            }
        }
        return String.format(MINOR_CREDITOR_DELETED_MESSAGE_FORMAT, minorCreditorId);
    }

}
