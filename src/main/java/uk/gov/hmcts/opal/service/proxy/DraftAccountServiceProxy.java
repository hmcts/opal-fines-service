package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.DraftAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountService;
import uk.gov.hmcts.opal.service.opal.DraftAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("draftAccountServiceProxy")
public class DraftAccountServiceProxy implements DraftAccountServiceInterface, ProxyInterface {

    private final DraftAccountService opalDraftAccountService;
    private final LegacyDraftAccountService legacyDraftAccountService;
    private final DynamicConfigService dynamicConfigService;

    private DraftAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountService : opalDraftAccountService;
    }

    @Override
    public DraftAccountEntity getDraftAccount(long draftAccountId) {
        return getCurrentModeService().getDraftAccount(draftAccountId);
    }

    @Override
    public List<DraftAccountEntity> searchDraftAccounts(DraftAccountSearchDto criteria) {
        return getCurrentModeService().searchDraftAccounts(criteria);
    }
}
