package uk.gov.hmcts.opal.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.FinesPermission;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.response.GetMajorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.service.proxy.MajorCreditorAccountProxy;

@Service
@Slf4j(topic = "opal.MajorCreditorAccountService")
@RequiredArgsConstructor
public class MajorCreditorAccountService {

    private final UserStateService userStateService;
    private final MajorCreditorAccountProxy majorCreditorAccountProxy;

    public GetMajorCreditorAccountAtAGlanceResponse getAtAGlance(Long majorCreditorAccountId) {
        log.debug(":getAtAGlance: id={}", majorCreditorAccountId);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();
        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return majorCreditorAccountProxy.getAtAGlance(majorCreditorAccountId);
    }

    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        log.debug(":getHeaderSummary: id={}", majorCreditorAccountId);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        GetMajorCreditorAccountHeaderSummaryResponse response =
            majorCreditorAccountProxy.getHeaderSummary(majorCreditorAccountId);
        Short businessUnitId = getBusinessUnitId(response.getBusinessUnitDetails());

        if (!userState.hasBusinessUnitUserWithPermission(businessUnitId, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(businessUnitId, FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return response;
    }

    public GetMajorCreditorHistoryResponse getHistory(
        Long majorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes
    ) {
        log.debug(":getHistory: id={}", majorCreditorAccountId);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (!userState.anyBusinessUnitUserHasPermission(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS)) {
            throw new PermissionNotAllowedException(FinesPermission.SEARCH_AND_VIEW_ACCOUNTS);
        }

        return majorCreditorAccountProxy.getHistory(majorCreditorAccountId, dateFrom, dateTo, itemTypes);
    }

    private static Short getBusinessUnitId(BusinessUnitSummaryCommon businessUnitDetails) {
        if (businessUnitDetails == null || businessUnitDetails.getBusinessUnitId() == null) {
            throw new IllegalStateException("Business unit details were not returned for the major creditor account");
        }
        return businessUnitDetails.getBusinessUnitId();
    }
}
