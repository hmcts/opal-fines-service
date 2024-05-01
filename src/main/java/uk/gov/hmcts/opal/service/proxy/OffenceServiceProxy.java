package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyOffenceService;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.util.List;

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

    @Override
    public OffenceEntity getOffence(long offenceId) {
        return getCurrentModeService().getOffence(offenceId);
    }

    @Override
    public List<OffenceEntity> searchOffences(OffenceSearchDto criteria) {
        return getCurrentModeService().searchOffences(criteria);
    }
}
