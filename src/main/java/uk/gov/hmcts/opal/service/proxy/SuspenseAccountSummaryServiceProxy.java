package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.SuspenseItemSummaryCommonStrict;
import uk.gov.hmcts.opal.service.iface.SuspenseAccountsSummaryServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseAccountSummaryService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalSuspenseAccountsSummaryService;

@Service
@RequiredArgsConstructor
public class SuspenseAccountSummaryServiceProxy implements SuspenseAccountsSummaryServiceInterface,
    ProxyInterface {

    private final OpalSuspenseAccountsSummaryService opalSuspenseAccountSummaryService;
    private final LegacySuspenseAccountSummaryService legacySuspenseAccountSummaryService;
    private final DynamicConfigService dynamicConfigService;

    private SuspenseAccountsSummaryServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService)
            ? legacySuspenseAccountSummaryService : opalSuspenseAccountSummaryService;
    }

    @Override
    public SuspenseItemSummaryCommonStrict getSuspenseAccountsSummary(Long businessUnitId, Long suspenseItemNumber) {
        return getCurrentModeService().getSuspenseAccountsSummary(businessUnitId, suspenseItemNumber);
    }

}
