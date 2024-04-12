package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TemplateSearchDto;
import uk.gov.hmcts.opal.entity.TemplateEntity;
import uk.gov.hmcts.opal.entity.TemplateEntity_;

public class TemplateSpecs extends EntitySpecs<TemplateEntity> {

    public Specification<TemplateEntity> findBySearchCriteria(TemplateSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getTemplateId()).map(TemplateSpecs::equalsTemplateId),
            notBlank(criteria.getTemplateName()).map(TemplateSpecs::likeTemplateName)
        ));
    }

    public static Specification<TemplateEntity> equalsTemplateId(Long templateId) {
        return (root, query, builder) -> equalsTemplateIdPredicate(root, builder, templateId);
    }

    public static Predicate equalsTemplateIdPredicate(From<?, TemplateEntity> from, CriteriaBuilder builder,
                                                      Long templateId) {
        return builder.equal(from.get(TemplateEntity_.templateId), templateId);
    }

    public static Specification<TemplateEntity> likeTemplateName(String templateName) {
        return (root, query, builder) -> likeTemplateNamePredicate(root, builder, templateName);
    }

    public static Predicate likeTemplateNamePredicate(From<?, TemplateEntity> from, CriteriaBuilder builder,
                                                      String templateName) {
        return likeWildcardPredicate(from.get(TemplateEntity_.templateName), builder, templateName);
    }
}
