package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.MiscellaneousAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMiscellaneousAccountService;
import uk.gov.hmcts.opal.service.opal.MiscellaneousAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("miscellaneousAccountServiceProxy")
public class MiscellaneousAccountServiceProxy implements MiscellaneousAccountServiceInterface, ProxyInterface {

    private final MiscellaneousAccountService opalMiscellaneousAccountService;
    private final LegacyMiscellaneousAccountService legacyMiscellaneousAccountService;
    private final DynamicConfigService dynamicConfigService;

    private MiscellaneousAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMiscellaneousAccountService : opalMiscellaneousAccountService;
    }

    @Override
    public MiscellaneousAccountEntity getMiscellaneousAccount(long miscellaneousAccountId) {
        return getCurrentModeService().getMiscellaneousAccount(miscellaneousAccountId);
    }

    @Override
    public List<MiscellaneousAccountEntity> searchMiscellaneousAccounts(MiscellaneousAccountSearchDto criteria) {
        return getCurrentModeService().searchMiscellaneousAccounts(criteria);
    }
}
