package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseAccountSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseAccountSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.disco.SuspenseAccountServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacySuspenseAccountService")
public class LegacySuspenseAccountService extends LegacyService implements SuspenseAccountServiceInterface {

    public LegacySuspenseAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public SuspenseAccountEntity getSuspenseAccount(long suspenseAccountId) {
        log.debug("getSuspenseAccount for {} from {}", suspenseAccountId, legacyGateway.getUrl());
        return postToGateway("getSuspenseAccount", SuspenseAccountEntity.class, suspenseAccountId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SuspenseAccountEntity> searchSuspenseAccounts(SuspenseAccountSearchDto criteria) {
        log.debug(":searchSuspenseAccounts: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchSuspenseAccounts", LegacySuspenseAccountSearchResults.class, criteria)
            .getSuspenseAccountEntities();
    }

}
