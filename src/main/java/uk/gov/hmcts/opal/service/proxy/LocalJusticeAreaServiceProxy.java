package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.service.LocalJusticeAreaServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyLocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("localJusticeAreaServiceProxy")
public class LocalJusticeAreaServiceProxy implements LocalJusticeAreaServiceInterface, ProxyInterface {

    private final LocalJusticeAreaService opalLocalJusticeAreaService;
    private final LegacyLocalJusticeAreaService legacyLocalJusticeAreaService;
    private final DynamicConfigService dynamicConfigService;

    private LocalJusticeAreaServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyLocalJusticeAreaService : opalLocalJusticeAreaService;
    }

    @Override
    public LocalJusticeAreaEntity getLocalJusticeArea(long localJusticeAreaId) {
        return getCurrentModeService().getLocalJusticeArea(localJusticeAreaId);
    }

    @Override
    public List<LocalJusticeAreaEntity> searchLocalJusticeAreas(LocalJusticeAreaSearchDto criteria) {
        return getCurrentModeService().searchLocalJusticeAreas(criteria);
    }
}
