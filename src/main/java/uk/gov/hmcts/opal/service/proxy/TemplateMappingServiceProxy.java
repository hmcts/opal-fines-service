package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.TemplateMappingServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyTemplateMappingService;
import uk.gov.hmcts.opal.service.opal.TemplateMappingService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("templateMappingServiceProxy")
public class TemplateMappingServiceProxy implements TemplateMappingServiceInterface, ProxyInterface {

    private final TemplateMappingService opalTemplateMappingService;
    private final LegacyTemplateMappingService legacyTemplateMappingService;
    private final DynamicConfigService dynamicConfigService;

    private TemplateMappingServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyTemplateMappingService : opalTemplateMappingService;
    }

    @Override
    public TemplateMappingEntity getTemplateMapping(Long templateId, Long applicationFunctionId) {
        return getCurrentModeService().getTemplateMapping(templateId, applicationFunctionId);
    }

    @Override
    public List<TemplateMappingEntity> searchTemplateMappings(TemplateMappingSearchDto criteria) {
        return getCurrentModeService().searchTemplateMappings(criteria);
    }
}
