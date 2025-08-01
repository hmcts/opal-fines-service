package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.CreditorTransactionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyCreditorTransactionService;
import uk.gov.hmcts.opal.disco.opal.CreditorTransactionService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("creditorTransactionServiceProxy")
public class CreditorTransactionServiceProxy implements CreditorTransactionServiceInterface, ProxyInterface {

    private final CreditorTransactionService opalCreditorTransactionService;
    private final LegacyCreditorTransactionService legacyCreditorTransactionService;
    private final DynamicConfigService dynamicConfigService;

    private CreditorTransactionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyCreditorTransactionService : opalCreditorTransactionService;
    }

    @Override
    public CreditorTransactionEntity getCreditorTransaction(long creditorTransactionId) {
        return getCurrentModeService().getCreditorTransaction(creditorTransactionId);
    }

    @Override
    public List<CreditorTransactionEntity> searchCreditorTransactions(CreditorTransactionSearchDto criteria) {
        return getCurrentModeService().searchCreditorTransactions(criteria);
    }
}
