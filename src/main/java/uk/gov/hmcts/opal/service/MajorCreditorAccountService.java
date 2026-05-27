package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService;

@Service
@Slf4j(topic = "opal.MajorCreditorAccountService")
@RequiredArgsConstructor
public class MajorCreditorAccountService {

    private final UserStateService userStateService;
    private final LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;

    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        log.debug(":getHeaderSummary: id={}", majorCreditorAccountId);

        UserState userState = userStateService.checkForAuthorisedUser();

        if (userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            return legacyMajorCreditorAccountService.getHeaderSummary(majorCreditorAccountId);
        }

        throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
    }
}
