package uk.gov.hmcts.opal.service;

import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.MANAGE_SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.authorisation.model.FinesPermission.VIEW_SUSPENSE_ITEMS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;
import uk.gov.hmcts.opal.service.proxy.SuspenseAccountSummaryServiceProxy;

@Service
@Slf4j(topic = "opal.SuspenseAccountsSummaryService")
@RequiredArgsConstructor
public class SuspenseAccountsSummaryService implements SuspenseAccountsSummaryServiceInterface {

    private final SuspenseAccountSummaryServiceProxy suspenseAccountSummaryServiceProxy;

    private final UserStateService userStateService;

    @Override
    public SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Long businessUnitId, Long suspenseItemNumber) {

        log.debug("getSuspenseItemSummary: businessUnitId: {}, suspenseItemNumber: {}",
            businessUnitId, suspenseItemNumber);

        UserState userState = userStateService.getUserStateV1FromSecurityContext();

        if (userState.anyBusinessUnitUserHasAnyPermission(VIEW_SUSPENSE_ITEMS, MANAGE_SUSPENSE_ITEMS)) {
            return suspenseAccountSummaryServiceProxy.getSuspenseAccountsSummary(businessUnitId, suspenseItemNumber);
        } else {
            throw new PermissionNotAllowedException(VIEW_SUSPENSE_ITEMS, MANAGE_SUSPENSE_ITEMS);
        }

    }

}
