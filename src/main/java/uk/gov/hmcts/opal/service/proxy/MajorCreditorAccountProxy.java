package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.service.iface.MajorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMajorCreditorAccountService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalMajorCreditorAccountService;

@Service
@Slf4j(topic = "opal.MajorCreditorAccountProxy")
@RequiredArgsConstructor
public class MajorCreditorAccountProxy implements MajorCreditorAccountServiceInterface, ProxyInterface {

    private final OpalMajorCreditorAccountService opalMajorCreditorAccountService;
    private final LegacyMajorCreditorAccountService legacyMajorCreditorAccountService;
    private final DynamicConfigService dynamicConfigService;

    private MajorCreditorAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMajorCreditorAccountService : opalMajorCreditorAccountService;
    }

    @Override
    public GetMajorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long majorCreditorAccountId) {
        log.debug(":getHeaderSummary: majorCreditorAccountId={}", majorCreditorAccountId);
        return getCurrentModeService().getHeaderSummary(majorCreditorAccountId);
    }
}
