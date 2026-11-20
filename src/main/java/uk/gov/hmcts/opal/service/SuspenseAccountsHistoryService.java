package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;
import uk.gov.hmcts.opal.service.proxy.SuspenseAccountsHistoryServiceProxy;

@Service
@Slf4j(topic = "opal.SuspenseAccountsSummaryService")
@RequiredArgsConstructor
public class SuspenseAccountsHistoryService implements SuspenseAccountsHistoryServiceInterface {

    private final SuspenseAccountsHistoryServiceProxy suspenseAccountHistoryServiceProxy;

    private final UserStateService userStateService;

    @Override
    public SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemNumber) {

        log.debug("getSuspenseItemHistory businessUnitId: {}, suspenseItemNumber: {}",
            businessUnitId, suspenseItemNumber);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (userState.anyBusinessUnitUserHasAnyPermission(VIEW_SUSPENSE_ITEMS, MANAGE_SUSPENSE_ITEMS)) {
            return suspenseAccountHistoryServiceProxy.getSuspenseItemHistory(businessUnitId, suspenseItemNumber);
        } else {
            throw new PermissionNotAllowedException(VIEW_SUSPENSE_ITEMS, MANAGE_SUSPENSE_ITEMS);
        }
    }
}
