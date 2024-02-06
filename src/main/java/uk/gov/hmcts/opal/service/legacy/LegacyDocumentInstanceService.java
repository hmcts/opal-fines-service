package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.service.DocumentInstanceServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyDocumentInstanceService")
public class LegacyDocumentInstanceService extends LegacyService implements DocumentInstanceServiceInterface {

    @Autowired
    protected LegacyDocumentInstanceService(@Value("${legacy-gateway-url}") String gatewayUrl, RestClient restClient) {
        super(gatewayUrl, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DocumentInstanceEntity getDocumentInstance(long documentInstanceId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
