package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.service.TemplateMappingServiceInterface;

import java.util.List;

@Service
@Slf4j(topic = "LegacyTemplateMappingService")
public class LegacyTemplateMappingService extends LegacyService implements TemplateMappingServiceInterface {

    public LegacyTemplateMappingService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public TemplateMappingEntity getTemplateMapping(Long templateId, Long applicationFunctionId) {
        log.info("getTemplateMapping for {} from {}", templateId, legacyGateway.getUrl());
        return postToGateway("getTemplateMapping", TemplateMappingEntity.class, templateId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TemplateMappingEntity> searchTemplateMappings(TemplateMappingSearchDto criteria) {
        log.info(":searchTemplateMappings: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway("searchTemplateMappings", List.class, criteria);
    }

}
