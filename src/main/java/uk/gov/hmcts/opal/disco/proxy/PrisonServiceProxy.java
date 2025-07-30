package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.PrisonServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyPrisonService;
import uk.gov.hmcts.opal.disco.opal.PrisonService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("prisonServiceProxy")
public class PrisonServiceProxy implements PrisonServiceInterface, ProxyInterface {

    private final PrisonService opalPrisonService;
    private final LegacyPrisonService legacyPrisonService;
    private final DynamicConfigService dynamicConfigService;

    private PrisonServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyPrisonService : opalPrisonService;
    }

    @Override
    public PrisonEntity getPrison(long prisonId) {
        return getCurrentModeService().getPrison(prisonId);
    }

    @Override
    public List<PrisonEntity> searchPrisons(PrisonSearchDto criteria) {
        return getCurrentModeService().searchPrisons(criteria);
    }
}
