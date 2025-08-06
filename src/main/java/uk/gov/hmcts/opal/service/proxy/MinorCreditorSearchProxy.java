package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorService;

import java.util.List;

@Service
@Slf4j(topic = "opal.MinorCreditorSearchProxy")
@RequiredArgsConstructor
public class MinorCreditorSearchProxy implements MinorCreditorServiceInterface, ProxyInterface{

    private final LegacyMinorCreditorService legacyMinorCreditorService;
    private final DynamicConfigService dynamicConfigService;

    private MinorCreditorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMinorCreditorService : null;
    }

    @Override
    public MinorCreditorEntity searchMinorCreditors(MinorCreditorEntity criteria) {
        return getCurrentModeService().searchMinorCreditors(criteria);
    }
}
