package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorSearch;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalMinorCreditorService;

@Service
@Slf4j(topic = "opal.MinorCreditorSearchProxy")
@RequiredArgsConstructor
public class MinorCreditorSearchProxy implements MinorCreditorServiceInterface, ProxyInterface{

    private final OpalMinorCreditorService opalMinorCreditorService;
    private final LegacyMinorCreditorService legacyMinorCreditorService;
    private final DynamicConfigService dynamicConfigService;

    private MinorCreditorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMinorCreditorService : opalMinorCreditorService;
    }

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch criteria) {
        return getCurrentModeService().searchMinorCreditors(criteria);
    }
}
