package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.ImpositionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyImpositionService;
import uk.gov.hmcts.opal.service.opal.ImpositionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("impositionServiceProxy")
public class ImpositionServiceProxy implements ImpositionServiceInterface, ProxyInterface {

    private final ImpositionService opalImpositionService;
    private final LegacyImpositionService legacyImpositionService;
    private final DynamicConfigService dynamicConfigService;

    private ImpositionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyImpositionService : opalImpositionService;
    }

    @Override
    public ImpositionEntity getImposition(long impositionId) {
        return getCurrentModeService().getImposition(impositionId);
    }

    @Override
    public List<ImpositionEntity> searchImpositions(ImpositionSearchDto criteria) {
        return getCurrentModeService().searchImpositions(criteria);
    }
}
