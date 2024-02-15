package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.entity.TemplateEntity_;

public class TemplateSpecs extends EntitySpecs<TemplateEntity> {

    public Specification<TemplateEntity> findBySearchCriteria(TemplateSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getTemplateId()).map(TemplateSpecs::equalsTemplateId)
        ));
    }

    public static Specification<TemplateEntity> equalsTemplateId(String templateId) {
        return (root, query, builder) -> builder.equal(root.get(TemplateEntity_.templateId), templateId);
    }

}
