package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.repository.TemplateMappingRepository;
import uk.gov.hmcts.opal.repository.jpa.TemplateMappingSpecs;
import uk.gov.hmcts.opal.service.TemplateMappingServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("templateMappingService")
public class TemplateMappingService implements TemplateMappingServiceInterface {

    private final TemplateMappingRepository templateMappingRepository;

    private final TemplateMappingSpecs specs = new TemplateMappingSpecs();

    @Override
    public TemplateMappingEntity getTemplateMapping(Long templateId, Long applicationFunctionId) {
        return templateMappingRepository
            .findDistinctByTemplate_TemplateIdAndApplicationFunction_ApplicationFunctionId(
                templateId, applicationFunctionId);
    }

    @Override
    public List<TemplateMappingEntity> searchTemplateMappings(TemplateMappingSearchDto criteria) {
        Page<TemplateMappingEntity> page = templateMappingRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
