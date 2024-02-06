package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDocumentService")
public class LegacyDocumentService extends LegacyService implements DocumentServiceInterface {

    @Autowired
    protected LegacyDocumentService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DocumentEntity getDocument(long documentId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<DocumentEntity> searchDocuments(DocumentSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
