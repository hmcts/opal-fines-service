package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.TillServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyTillService;
import uk.gov.hmcts.opal.disco.opal.TillService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("tillServiceProxy")
public class TillServiceProxy implements TillServiceInterface, ProxyInterface {

    private final TillService opalTillService;
    private final LegacyTillService legacyTillService;
    private final DynamicConfigService dynamicConfigService;

    private TillServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyTillService : opalTillService;
    }

    @Override
    public TillEntity getTill(long tillId) {
        return getCurrentModeService().getTill(tillId);
    }

    @Override
    public List<TillEntity> searchTills(TillSearchDto criteria) {
        return getCurrentModeService().searchTills(criteria);
    }
}
