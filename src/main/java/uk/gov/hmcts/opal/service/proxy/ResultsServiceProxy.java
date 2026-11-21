package uk.gov.hmcts.opal.service.proxy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.generated.model.GetResultByIdResponseResults;
import uk.gov.hmcts.opal.generated.model.ResultsRefDataResponse;
import uk.gov.hmcts.opal.service.iface.ResultsServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalResultService;
import uk.gov.hmcts.opal.service.legacy.LegacyResultService;

@Service
@RequiredArgsConstructor
public class ResultsServiceProxy implements ResultsServiceInterface, ProxyInterface {
    private final LegacyResultService legacyResultService;
    private final OpalResultService opalResultService;
    private final DynamicConfigService dynamicConfigService;

    private ResultsServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyResultService : opalResultService;
    }

    @Override
    public GetResultByIdResponseResults getResult(String resultId, boolean includeWelsh) {
        return getCurrentModeService().getResult(resultId, includeWelsh);
    }

    @Override
    public ResultsRefDataResponse getResultsByIds(List<String> resultIds, Boolean active, Boolean manualEnforcement,
                                                  Boolean generatesHearing, Boolean enforcement,
                                                  Boolean enforcementOverride) {
        return getCurrentModeService().getResultsByIds(resultIds, active, manualEnforcement, generatesHearing,
            enforcement, enforcementOverride);
    }
}
