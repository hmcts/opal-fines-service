package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.PermissionNotAllowedException;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountService {

    private final DefendantAccountServiceProxy defendantAccountServiceProxy;

    private final UserStateService userStateService;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId, String authHeaderValue) {
        log.debug(":getHeaderSummary:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (!userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountServiceProxy.getHeaderSummary(defendantAccountId);
    }



    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto,
                                                                    String authHeaderValue) {
        log.debug(":searchDefendantAccounts:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.searchDefendantAccounts(accountSearchDto);
        } else {
            throw new PermissionNotAllowedException(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

    public GetDefendantAccountPartyResponse getDefendantAccountParty(
        Long defendantAccountId,
        Long defendantAccountPartyId,
        String authHeaderValue) {

        log.debug(":getDefendantAccountParty:");

        UserState userState = userStateService.checkForAuthorisedUser(authHeaderValue);

        if (userState.anyBusinessUnitUserHasPermission(Permissions.SEARCH_AND_VIEW_ACCOUNTS)) {

            return defendantAccountServiceProxy.getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
        } else {
            throw new PermissionNotAllowedException(Permissions.SEARCH_AND_VIEW_ACCOUNTS);
        }
    }

}
