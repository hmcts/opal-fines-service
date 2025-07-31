package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDocumentInstanceSearchResults;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.disco.DocumentInstanceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyDocumentInstanceService")
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
        log.debug("getDocumentInstance for {} from {}", documentInstanceId, legacyGateway.getUrl());
        return postToGateway("getDocumentInstance", DocumentInstanceEntity.class, documentInstanceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria) {
        log.debug(":searchDocumentInstances: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchDocumentInstances", LegacyDocumentInstanceSearchResults.class, criteria)
            .getDocumentInstanceEntities();
    }

}
