package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyApplicationFunctionSearchResults;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.disco.ApplicationFunctionServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyApplicationFunctionService")
public class LegacyApplicationFunctionService extends LegacyService implements ApplicationFunctionServiceInterface {

    public LegacyApplicationFunctionService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public ApplicationFunctionEntity getApplicationFunction(long applicationFunctionId) {
        log.debug("getApplicationFunction for {} from {}", applicationFunctionId, legacyGateway.getUrl());
        return postToGateway("getApplicationFunction", ApplicationFunctionEntity.class, applicationFunctionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ApplicationFunctionEntity> searchApplicationFunctions(ApplicationFunctionSearchDto criteria) {
        log.debug(":searchApplicationFunctions: criteria: {} via gateway {}",
                  criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchApplicationFunctions", LegacyApplicationFunctionSearchResults.class, criteria)
            .getApplicationFunctionEntities();
    }

}
