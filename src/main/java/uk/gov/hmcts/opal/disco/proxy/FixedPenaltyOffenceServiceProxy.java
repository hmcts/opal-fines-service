package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.FixedPenaltyOffenceServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyFixedPenaltyOffenceService;
import uk.gov.hmcts.opal.disco.opal.FixedPenaltyOffenceService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("fixedPenaltyOffenceServiceProxy")
public class FixedPenaltyOffenceServiceProxy implements FixedPenaltyOffenceServiceInterface, ProxyInterface {

    private final FixedPenaltyOffenceService opalFixedPenaltyOffenceService;
    private final LegacyFixedPenaltyOffenceService legacyFixedPenaltyOffenceService;
    private final DynamicConfigService dynamicConfigService;

    private FixedPenaltyOffenceServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyFixedPenaltyOffenceService : opalFixedPenaltyOffenceService;
    }

    @Override
    public FixedPenaltyOffenceEntity getFixedPenaltyOffence(long fixedPenaltyOffenceId) {
        return getCurrentModeService().getFixedPenaltyOffence(fixedPenaltyOffenceId);
    }

    @Override
    public List<FixedPenaltyOffenceEntity> searchFixedPenaltyOffences(FixedPenaltyOffenceSearchDto criteria) {
        return getCurrentModeService().searchFixedPenaltyOffences(criteria);
    }
}
