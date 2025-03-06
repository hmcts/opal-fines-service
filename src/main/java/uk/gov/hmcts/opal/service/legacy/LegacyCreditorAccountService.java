package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyCreditorAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.CreditorAccountSearchDto;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.service.CreditorAccountServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyCreditorAccountService")
public class LegacyCreditorAccountService extends LegacyService implements CreditorAccountServiceInterface {

    public LegacyCreditorAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public CreditorAccountEntity getCreditorAccount(long creditorAccountId) {
        log.debug("getCreditorAccount for {} from {}", creditorAccountId, legacyGateway.getUrl());
        return postToGateway("getCreditorAccount", CreditorAccountEntity.class, creditorAccountId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CreditorAccountEntity> searchCreditorAccounts(CreditorAccountSearchDto criteria) {
        log.debug(":searchCreditorAccounts: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchCreditorAccounts", LegacyCreditorAccountSearchResults.class, criteria)
            .getCreditorAccountEntities();
    }

}
