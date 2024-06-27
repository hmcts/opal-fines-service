package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDefendantTransactionSearchResults;
import uk.gov.hmcts.opal.dto.search.DefendantTransactionSearchDto;
import uk.gov.hmcts.opal.entity.DefendantTransactionEntity;
import uk.gov.hmcts.opal.service.DefendantTransactionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDefendantTransactionService")
public class LegacyDefendantTransactionService extends LegacyService implements DefendantTransactionServiceInterface {

    public LegacyDefendantTransactionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DefendantTransactionEntity getDefendantTransaction(long defendantTransactionId) {
        log.info("getDefendantTransaction for {} from {}", defendantTransactionId, legacyGateway.getUrl());
        return postToGateway("getDefendantTransaction", DefendantTransactionEntity.class, defendantTransactionId);
    }

    @Override
    public List<DefendantTransactionEntity> searchDefendantTransactions(DefendantTransactionSearchDto criteria) {
        log.info(":searchDefendantTransactions: criteria: {} via gateway {}", criteria.toJson(),
                 legacyGateway.getUrl());
        return postToGateway("searchDefendantTransactions", LegacyDefendantTransactionSearchResults.class,
                             criteria)
            .getDefendantTransactionEntities();
    }

}
