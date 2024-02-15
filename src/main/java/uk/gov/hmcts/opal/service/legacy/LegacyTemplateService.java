package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
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
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

    @Override
    public List<TemplateEntity> searchTemplates(TemplateSearchDto criteria) {
        throw new LegacyGatewayResponseException("Not Yet Implemented");
    }

}
