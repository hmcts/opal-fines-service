package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.TemplateServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyTemplateService;
import uk.gov.hmcts.opal.service.opal.TemplateService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("templateServiceProxy")
public class TemplateServiceProxy implements TemplateServiceInterface, ProxyInterface {

    private final TemplateService opalTemplateService;
    private final LegacyTemplateService legacyTemplateService;
    private final DynamicConfigService dynamicConfigService;

    private TemplateServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyTemplateService : opalTemplateService;
    }

    @Override
    public TemplateEntity getTemplate(long templateId) {
        return getCurrentModeService().getTemplate(templateId);
    }

    @Override
    public List<TemplateEntity> searchTemplates(TemplateSearchDto criteria) {
        return getCurrentModeService().searchTemplates(criteria);
    }
}
