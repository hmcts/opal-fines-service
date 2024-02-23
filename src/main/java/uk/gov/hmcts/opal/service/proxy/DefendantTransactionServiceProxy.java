package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.DefendantTransactionServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantTransactionService;
import uk.gov.hmcts.opal.service.opal.DefendantTransactionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("defendantTransactionServiceProxy")
public class DefendantTransactionServiceProxy implements DefendantTransactionServiceInterface, ProxyInterface {

    private final DefendantTransactionService opalDefendantTransactionService;
    private final LegacyDefendantTransactionService legacyDefendantTransactionService;
    private final DynamicConfigService dynamicConfigService;

    private DefendantTransactionServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDefendantTransactionService : opalDefendantTransactionService;
    }

    @Override
    public DefendantTransactionEntity getDefendantTransaction(long defendantTransactionId) {
        return getCurrentModeService().getDefendantTransaction(defendantTransactionId);
    }

    @Override
    public List<DefendantTransactionEntity> searchDefendantTransactions(DefendantTransactionSearchDto criteria) {
        return getCurrentModeService().searchDefendantTransactions(criteria);
    }
}
