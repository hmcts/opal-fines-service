package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyDocumentSearchResults;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyDocumentService")
public class LegacyDocumentService extends LegacyService implements DocumentServiceInterface {

    public LegacyDocumentService(LegacyGatewayProperties legacyGateway, RestClient restClient) {
        super(legacyGateway, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DocumentEntity getDocument(String documentId) {
        log.debug("getDocument for {} from {}", documentId, legacyGateway.getUrl());
        return postToGateway("getDocument", DocumentEntity.class, documentId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        log.debug(":searchDocuments: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchDocuments", LegacyDocumentSearchResults.class, criteria)
            .getDocumentEntities();
    }

}
