package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.OffenceServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyOffenceService;
import uk.gov.hmcts.opal.service.opal.OffenceService;


@Service
@RequiredArgsConstructor
@Qualifier("offenceServiceProxy")
public class OffenceServiceProxy implements OffenceServiceInterface, ProxyInterface {

    private final OffenceService opalOffenceService;
    private final LegacyOffenceService legacyOffenceService;
    private final DynamicConfigService dynamicConfigService;

    private OffenceServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyOffenceService : opalOffenceService;
    }
}
