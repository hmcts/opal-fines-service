package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.SuspenseAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacySuspenseAccountService;
import uk.gov.hmcts.opal.disco.opal.SuspenseAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseAccountServiceProxy")
public class SuspenseAccountServiceProxy implements SuspenseAccountServiceInterface, ProxyInterface {

    private final SuspenseAccountService opalSuspenseAccountService;
    private final LegacySuspenseAccountService legacySuspenseAccountService;
    private final DynamicConfigService dynamicConfigService;

    private SuspenseAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacySuspenseAccountService : opalSuspenseAccountService;
    }

    @Override
    public SuspenseAccountEntity getSuspenseAccount(long suspenseAccountId) {
        return getCurrentModeService().getSuspenseAccount(suspenseAccountId);
    }

    @Override
    public List<SuspenseAccountEntity> searchSuspenseAccounts(SuspenseAccountSearchDto criteria) {
        return getCurrentModeService().searchSuspenseAccounts(criteria);
    }
}
