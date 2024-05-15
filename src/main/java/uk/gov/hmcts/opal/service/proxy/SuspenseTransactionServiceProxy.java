package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacySuspenseTransactionService;
import uk.gov.hmcts.opal.service.opal.SuspenseTransactionService;

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
