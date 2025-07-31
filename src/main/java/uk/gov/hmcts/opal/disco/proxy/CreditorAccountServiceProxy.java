package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.CreditorAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyCreditorAccountService;
import uk.gov.hmcts.opal.disco.opal.CreditorAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("creditorAccountServiceProxy")
public class CreditorAccountServiceProxy implements CreditorAccountServiceInterface, ProxyInterface {

    private final CreditorAccountService opalCreditorAccountService;
    private final LegacyCreditorAccountService legacyCreditorAccountService;
    private final DynamicConfigService dynamicConfigService;

    private CreditorAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyCreditorAccountService : opalCreditorAccountService;
    }

    @Override
    public CreditorAccountEntity getCreditorAccount(long creditorAccountId) {
        return getCurrentModeService().getCreditorAccount(creditorAccountId);
    }

    @Override
    public List<CreditorAccountEntity> searchCreditorAccounts(CreditorAccountSearchDto criteria) {
        return getCurrentModeService().searchCreditorAccounts(criteria);
    }
}
