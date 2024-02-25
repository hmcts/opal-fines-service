package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;

import java.util.List;

public interface TemplateMappingServiceInterface {

    TemplateMappingEntity getTemplateMapping(Long templateId, Long applicationFunctionId);

    List<TemplateMappingEntity> searchTemplateMappings(TemplateMappingSearchDto criteria);
}
