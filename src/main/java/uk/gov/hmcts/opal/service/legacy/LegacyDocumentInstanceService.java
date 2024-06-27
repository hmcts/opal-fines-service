package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.searchResults.LegacyDocumentInstanceSearchResults;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.service.DocumentInstanceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDocumentInstanceService")
public class LegacyDocumentInstanceService extends LegacyService implements DocumentInstanceServiceInterface {


    public LegacyDocumentInstanceService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DocumentInstanceEntity getDocumentInstance(long documentInstanceId) {
        log.info("getDocumentInstance for {} from {}", documentInstanceId, legacyGateway.getUrl());
        return postToGateway("getDocumentInstance", DocumentInstanceEntity.class, documentInstanceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria) {
        log.info(":searchDocumentInstances: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchDocumentInstances", LegacyDocumentInstanceSearchResults.class, criteria)
            .getDocumentInstanceEntities();
    }

}
