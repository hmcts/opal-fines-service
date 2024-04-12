package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ApplicationFunctionSearchDto;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity;
import uk.gov.hmcts.opal.entity.ApplicationFunctionEntity_;

public class ApplicationFunctionSpecs extends EntitySpecs<ApplicationFunctionEntity> {

    public Specification<ApplicationFunctionEntity> findBySearchCriteria(ApplicationFunctionSearchDto criteria) {
        return Specification.allOf(specificationList(
            numeric(criteria.getApplicationFunctionId()).map(ApplicationFunctionSpecs::equalsApplicationFunctionId),
            notBlank(criteria.getFunctionName()).map(ApplicationFunctionSpecs::likeFunctionName)
        ));
    }

    public static Specification<ApplicationFunctionEntity> equalsApplicationFunctionId(String applicationFunctionId) {
        return (root, query, builder) -> builder.equal(root.get(ApplicationFunctionEntity_.applicationFunctionId),
                                                       applicationFunctionId);
    }

    public static Specification<ApplicationFunctionEntity> likeFunctionName(String functionName) {
        return (root, query, builder) -> likeFunctionNamePredicate(root, builder, functionName);
    }

    public static Predicate likeFunctionNamePredicate(From<?, ApplicationFunctionEntity> from, CriteriaBuilder builder,
                                                      String functionName) {
        return likeWildcardPredicate(from.get(ApplicationFunctionEntity_.functionName), builder, functionName);
    }

}
