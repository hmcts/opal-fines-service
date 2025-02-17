package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.service.CourtServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyCourtService;
import uk.gov.hmcts.opal.service.opal.CourtService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("courtServiceProxy")
public class CourtServiceProxy implements CourtServiceInterface, ProxyInterface {

    private final CourtService opalCourtService;
    private final LegacyCourtService legacyCourtService;
    private final DynamicConfigService dynamicConfigService;

    private CourtServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyCourtService : opalCourtService;
    }

    @Override
    public CourtEntity.Lite getCourtLite(long courtId) {
        return getCurrentModeService().getCourtLite(courtId);
    }

    @Override
    public List<CourtEntity.Lite> searchCourts(CourtSearchDto criteria) {
        return getCurrentModeService().searchCourts(criteria);
    }
}
