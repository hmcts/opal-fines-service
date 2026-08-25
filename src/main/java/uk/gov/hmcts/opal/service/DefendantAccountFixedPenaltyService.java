package uk.gov.hmcts.opal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountFixedPenaltyServiceProxy;

@Service
@Slf4j(topic = "opal.DefendantAccountService")
@RequiredArgsConstructor
public class DefendantAccountFixedPenaltyService {

    private final DefendantAccountFixedPenaltyServiceProxy defendantAccountFixedPenaltyServiceProxy;

    private final UserStateService userStateService;

    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(
        Long defendantAccountId) {

        UserStateV2 userState = userStateService.getUserStateFromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return defendantAccountFixedPenaltyServiceProxy.getDefendantAccountFixedPenalty(defendantAccountId);
    }
}
