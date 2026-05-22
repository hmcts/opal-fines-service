package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountImpositionsResponse;
import uk.gov.hmcts.opal.service.iface.ImpositionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalImpositionService;

@Service
@Slf4j(topic = "opal.ImpositionServiceProxy")
@RequiredArgsConstructor
public class ImpositionServiceProxy implements ImpositionServiceInterface, ProxyInterface {

    private final OpalImpositionService opalImpositionService;
    private final LegacyImpositionService legacyImpositionService;
    private final DynamicConfigService dynamicConfigService;

    private ImpositionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyImpositionService : opalImpositionService;
    }

    @Override
    public GetDefendantAccountImpositionsResponse getImpositions(Long defendantAccountId) {
        return getCurrentModeService().getImpositions(defendantAccountId);
    }
}
