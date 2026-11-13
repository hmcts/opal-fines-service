package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemHistoryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsHistoryServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseAccountsHistoryService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalSuspenseAccountsHistoryService;

@Service
@RequiredArgsConstructor
public class SuspenseAccountsHistoryServiceProxy implements SuspenseAccountsHistoryServiceInterface,
    ProxyInterface {

    private final OpalSuspenseAccountsHistoryService opalSuspenseAccountsHistoryService;
    private final LegacySuspenseAccountsHistoryService legacySuspenseAccountsHistoryService;

    private final DynamicConfigService dynamicConfigService;

    private SuspenseAccountsHistoryServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService)
            ? legacySuspenseAccountsHistoryService : opalSuspenseAccountsHistoryService;
    }

    @Override
    public SuspenseItemHistoryCommonStrict getSuspenseItemHistory(Long businessUnitId, Long suspenseItemNumber) {
        return getCurrentModeService().getSuspenseItemHistory(businessUnitId, suspenseItemNumber);
    }

}
