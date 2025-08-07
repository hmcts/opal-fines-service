package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.TemplateMappingServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyTemplateMappingService;
import uk.gov.hmcts.opal.disco.opal.TemplateMappingService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

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
