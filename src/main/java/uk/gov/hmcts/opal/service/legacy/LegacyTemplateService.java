package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyTemplateSearchResults;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.service.TemplateServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyTemplateService")
public class LegacyTemplateService extends LegacyService implements TemplateServiceInterface {

    public LegacyTemplateService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public TemplateEntity getTemplate(long templateId) {
        log.info("getTemplate for {} from {}", templateId, legacyGateway.getUrl());
        return postToGateway("getTemplate", TemplateEntity.class, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TemplateEntity> searchTemplates(TemplateSearchDto criteria) {
        log.info(":searchTemplates: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchTemplates", LegacyTemplateSearchResults.class, criteria)
            .getTemplateEntities();
    }

}
