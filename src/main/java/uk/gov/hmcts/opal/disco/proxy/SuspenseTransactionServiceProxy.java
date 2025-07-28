package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.SuspenseTransactionServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacySuspenseTransactionService;
import uk.gov.hmcts.opal.disco.opal.SuspenseTransactionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("suspenseTransactionServiceProxy")
public class SuspenseTransactionServiceProxy implements SuspenseTransactionServiceInterface, ProxyInterface {

    private final SuspenseTransactionService opalSuspenseTransactionService;
    private final LegacySuspenseTransactionService legacySuspenseTransactionService;
    private final DynamicConfigService dynamicConfigService;

    private SuspenseTransactionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacySuspenseTransactionService : opalSuspenseTransactionService;
    }

    @Override
    public SuspenseTransactionEntity getSuspenseTransaction(long suspenseTransactionId) {
        return getCurrentModeService().getSuspenseTransaction(suspenseTransactionId);
    }

    @Override
    public List<SuspenseTransactionEntity> searchSuspenseTransactions(SuspenseTransactionSearchDto criteria) {
        return getCurrentModeService().searchSuspenseTransactions(criteria);
    }
}
