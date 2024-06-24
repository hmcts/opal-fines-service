package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.AccountTransferSearchDto;
import uk.gov.hmcts.opal.entity.AccountTransferEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.AccountTransferServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyAccountTransferService")
public class LegacyAccountTransferService extends LegacyService implements AccountTransferServiceInterface {


    public LegacyAccountTransferService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public AccountTransferEntity getAccountTransfer(long accountTransferId) {
        log.info("Get Account Transfer for {} from {}", accountTransferId, legacyGateway.getUrl());
        return postToGateway("getAccountTransfer", AccountTransferEntity.class, accountTransferId);
    }

    @Override
    public List<AccountTransferEntity> searchAccountTransfers(AccountTransferSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
