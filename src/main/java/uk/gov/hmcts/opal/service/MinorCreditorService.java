package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.proxy.MinorCreditorSearchProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class MinorCreditorService {

    private final MinorCreditorSearchProxy minorCreditorSearchProxy;

    private final UserStateService userStateService;

    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch entity,
                                                                        String authHeaderValue) {
        log.debug(":searchMinorCreditor:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.searchMinorCreditors(entity);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetMinorCreditorAccountHeaderSummaryResponse getMinorCreditorAccountHeaderSummary(
        Long minorCreditorId,
        String authHeaderValue) {

        log.debug(":getMinorCreditorAccountHeaderSummary: id={}", minorCreditorId);

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return minorCreditorSearchProxy.getHeaderSummary(minorCreditorId);
        } else {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

}
