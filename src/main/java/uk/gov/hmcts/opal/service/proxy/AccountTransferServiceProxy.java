package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.service.AccountTransferServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyAccountTransferService;
import uk.gov.hmcts.opal.service.opal.AccountTransferService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("accountTransferServiceProxy")
public class AccountTransferServiceProxy implements AccountTransferServiceInterface, ProxyInterface {

    private final AccountTransferService opalAccountTransferService;
    private final LegacyAccountTransferService legacyAccountTransferService;
    private final DynamicConfigService dynamicConfigService;

    private AccountTransferServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyAccountTransferService : opalAccountTransferService;
    }

    @Override
    public AccountTransferEntity getAccountTransfer(long accountTransferId) {
        return getCurrentModeService().getAccountTransfer(accountTransferId);
    }

    @Override
    public List<AccountTransferEntity> searchAccountTransfers(AccountTransferSearchDto criteria) {
        return getCurrentModeService().searchAccountTransfers(criteria);
    }
}
