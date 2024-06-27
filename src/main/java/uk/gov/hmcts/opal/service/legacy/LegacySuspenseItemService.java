package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacySuspenseItemSearchResults;
import uk.gov.hmcts.opal.dto.search.SuspenseItemSearchDto;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.service.SuspenseItemServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacySuspenseItemService")
public class LegacySuspenseItemService extends LegacyService implements SuspenseItemServiceInterface {

    public LegacySuspenseItemService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public SuspenseItemEntity getSuspenseItem(long suspenseItemId) {
        log.info("getSuspenseItem for {} from {}", suspenseItemId, legacyGateway.getUrl());
        return postToGateway("getSuspenseItem", SuspenseItemEntity.class, suspenseItemId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SuspenseItemEntity> searchSuspenseItems(SuspenseItemSearchDto criteria) {
        log.info(":searchSuspenseItems: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchSuspenseItems", LegacySuspenseItemSearchResults.class, criteria)
            .getSuspenseItemEntities();
    }

}
