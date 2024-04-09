package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.SuspenseTransactionSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseTransactionEntity;
import uk.gov.hmcts.opal.service.SuspenseTransactionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacySuspenseTransactionService")
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
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<SuspenseTransactionEntity> searchSuspenseTransactions(SuspenseTransactionSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
