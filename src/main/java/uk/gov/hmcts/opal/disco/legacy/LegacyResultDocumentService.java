package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.ResultDocumentSearchDto;
import uk.gov.hmcts.opal.entity.ResultDocumentEntity;
import uk.gov.hmcts.opal.disco.ResultDocumentServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyResultDocumentService")
public class LegacyResultDocumentService extends LegacyService implements ResultDocumentServiceInterface {

    public LegacyResultDocumentService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ResultDocumentEntity getResultDocument(long resultDocumentId) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<ResultDocumentEntity> searchResultDocuments(ResultDocumentSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
