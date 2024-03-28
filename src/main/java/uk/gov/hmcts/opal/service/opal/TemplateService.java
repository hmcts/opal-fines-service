package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.repository.TemplateRepository;
import uk.gov.hmcts.opal.repository.jpa.TemplateSpecs;
import uk.gov.hmcts.opal.service.TemplateServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier("templateService")
public class TemplateService implements TemplateServiceInterface {

    private final TemplateRepository templateRepository;

    private final TemplateSpecs specs = new TemplateSpecs();

    @Override
    public TemplateEntity getTemplate(long templateId) {
        return templateRepository.getReferenceById(templateId);
    }

    @Override
    public List<TemplateEntity> searchTemplates(TemplateSearchDto criteria) {
        Page<TemplateEntity> page = templateRepository
            .findBy(specs.findBySearchCriteria(criteria),
                    ffq -> ffq.page(Pageable.unpaged()));

        return page.getContent();
    }

}
