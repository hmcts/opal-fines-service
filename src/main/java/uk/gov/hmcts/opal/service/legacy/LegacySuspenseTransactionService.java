package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseTransactionSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacySuspenseTransactionService")
public class LegacySuspenseTransactionService extends LegacyService implements SuspenseTransactionServiceInterface {

    public LegacySuspenseTransactionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public SuspenseTransactionEntity getSuspenseTransaction(long suspenseTransactionId) {
        log.debug("getSuspenseTransaction for {} from {}", suspenseTransactionId, legacyGateway.getUrl());
        return postToGateway("getSuspenseTransaction", SuspenseTransactionEntity.class, suspenseTransactionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SuspenseTransactionEntity> searchSuspenseTransactions(SuspenseTransactionSearchDto criteria) {
        log.debug(":searchSuspenseTransactions: criteria: {} via gateway {}",
                  criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchSuspenseTransactions", LegacySuspenseTransactionSearchResults.class, criteria)
            .getSuspenseTransactionEntities();
    }

}
