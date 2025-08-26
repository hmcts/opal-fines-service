package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.HmrcRequestSearchDto;
import uk.gov.hmcts.opal.entity.HmrcRequestEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.HmrcRequestServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyHmrcRequestService;
import uk.gov.hmcts.opal.disco.opal.HmrcRequestService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("hmrcRequestServiceProxy")
public class HmrcRequestServiceProxy implements HmrcRequestServiceInterface, ProxyInterface {

    private final HmrcRequestService opalHmrcRequestService;
    private final LegacyHmrcRequestService legacyHmrcRequestService;
    private final DynamicConfigService dynamicConfigService;

    private HmrcRequestServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyHmrcRequestService : opalHmrcRequestService;
    }

    @Override
    public HmrcRequestEntity getHmrcRequest(long hmrcRequestId) {
        return getCurrentModeService().getHmrcRequest(hmrcRequestId);
    }

    @Override
    public List<HmrcRequestEntity> searchHmrcRequests(HmrcRequestSearchDto criteria) {
        return getCurrentModeService().searchHmrcRequests(criteria);
    }
}
