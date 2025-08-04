package uk.gov.hmcts.opal.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@Service
@Slf4j(topic = "opal.DiscoDefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountServiceProxy defendantAccountServiceProxy;

    private final UserStateService userStateService;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId, String authHeaderValue) {

        log.debug(":getHeaderSummary:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)) {
            return defendantAccountServiceProxy.getHeaderSummary(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

}
