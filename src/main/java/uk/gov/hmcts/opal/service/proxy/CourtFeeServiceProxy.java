package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CourtFeeSearchDto;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.CourtFeeServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyCourtFeeService;
import uk.gov.hmcts.opal.service.opal.CourtFeeService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("courtFeeServiceProxy")
public class CourtFeeServiceProxy implements CourtFeeServiceInterface, ProxyInterface {

    private final CourtFeeService opalCourtFeeService;
    private final LegacyCourtFeeService legacyCourtFeeService;
    private final DynamicConfigService dynamicConfigService;

    private CourtFeeServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyCourtFeeService : opalCourtFeeService;
    }

    @Override
    public CourtFeeEntity getCourtFee(long courtFeeId) {
        return getCurrentModeService().getCourtFee(courtFeeId);
    }

    @Override
    public List<CourtFeeEntity> searchCourtFees(CourtFeeSearchDto criteria) {
        return getCurrentModeService().searchCourtFees(criteria);
    }
}
