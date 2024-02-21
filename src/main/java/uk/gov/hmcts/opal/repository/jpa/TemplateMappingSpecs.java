package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity_;

public class TemplateMappingSpecs extends EntitySpecs<TemplateMappingEntity> {

    public Specification<TemplateMappingEntity> findBySearchCriteria(TemplateMappingSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getTemplateId()).map(TemplateMappingSpecs::equalsTemplateId),
            notBlank(criteria.getApplicationFunctionId()).map(TemplateMappingSpecs::equalsApplicationFunctionId)
        ));
    }

    public static Specification<TemplateMappingEntity> equalsTemplateId(String templateId) {
        return (root, query, builder) -> builder.equal(root.get(TemplateMappingEntity_.templateId), templateId);
    }

    public static Specification<TemplateMappingEntity> equalsApplicationFunctionId(String applicationFunctionId) {
        return (root, query, builder) -> builder.equal(root.get(TemplateMappingEntity_.applicationFunctionId),
                                                       applicationFunctionId
        );
    }
}
