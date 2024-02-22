package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity.MappingId;

import java.util.List;

public interface TemplateMappingServiceInterface {

    TemplateMappingEntity getTemplateMapping(MappingId templateMappingId);

    List<TemplateMappingEntity> searchTemplateMappings(TemplateMappingSearchDto criteria);
}
