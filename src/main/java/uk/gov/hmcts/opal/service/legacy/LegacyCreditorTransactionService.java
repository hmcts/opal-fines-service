package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.CreditorTransactionSearchDto;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.service.CreditorTransactionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyCreditorTransactionService")
public class LegacyCreditorTransactionService extends LegacyService implements CreditorTransactionServiceInterface {

    public LegacyCreditorTransactionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CreditorTransactionEntity getCreditorTransaction(long creditorTransactionId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<CreditorTransactionEntity> searchCreditorTransactions(CreditorTransactionSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
