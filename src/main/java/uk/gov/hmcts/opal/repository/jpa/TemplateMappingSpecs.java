package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TemplateMappingSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity_;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity;
import uk.gov.hmcts.opal.entity.TemplateMappingEntity_;

import static uk.gov.hmcts.opal.repository.jpa.TemplateSpecs.templateIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.TemplateSpecs.templateNamePredicate;

public class TemplateMappingSpecs extends EntitySpecs<TemplateMappingEntity> {

    public Specification<TemplateMappingEntity> findBySearchCriteria(TemplateMappingSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getTemplateId()).map(TemplateMappingSpecs::equalsTemplateId),
            notBlank(criteria.getTemplateName()).map(TemplateMappingSpecs::likeTemplateName),
            numericLong(criteria.getApplicationFunctionId()).map(TemplateMappingSpecs::equalsApplicationFunctionId)
        ));
    }

    public static Specification<TemplateMappingEntity> equalsTemplateId(Long templateId) {
        return (root, query, builder) -> templateIdPredicate(joinTemplate(root), builder, templateId);
    }

    public static Specification<TemplateMappingEntity> likeTemplateName(String templateName) {
        return (root, query, builder) -> templateNamePredicate(joinTemplate(root), builder, templateName);
    }

    public static Specification<TemplateMappingEntity> equalsApplicationFunctionId(Long applicationFunctionId) {
        return (root, query, builder) -> builder.equal(joinApplicationFunction(root).get(
            ApplicationFunctionEntity_.applicationFunctionId), applicationFunctionId
        );
    }

    public static Join<TemplateMappingEntity, TemplateEntity> joinTemplate(Root<TemplateMappingEntity> root) {
        return root.join(TemplateMappingEntity_.template);
    }

    public static Join<TemplateMappingEntity, ApplicationFunctionEntity> joinApplicationFunction(
        Root<TemplateMappingEntity> root) {
        return root.join(TemplateMappingEntity_.applicationFunction);
    }
}
