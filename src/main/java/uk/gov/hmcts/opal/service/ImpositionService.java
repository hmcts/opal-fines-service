package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.service.proxy.ImpositionServiceProxy;

@Service
@Slf4j(topic = "opal.ImpositionService")
@RequiredArgsConstructor
public class ImpositionService {
    private final ImpositionServiceProxy impositionServiceProxy;

    private final UserStateService userStateService;

    public GetDefendantAccountImpositionsResponse getImpositions(
        Long defendantAccountId,
        String authHeaderValue) {

        log.debug(":getImpositions:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return impositionServiceProxy.getImpositions(defendantAccountId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }
}
