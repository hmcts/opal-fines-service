package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LogActionSearchDto;
import uk.gov.hmcts.opal.entity.LogActionEntity;
import uk.gov.hmcts.opal.entity.LogActionEntity_;

public class LogActionSpecs extends EntitySpecs<LogActionEntity> {

    public Specification<LogActionEntity> findBySearchCriteria(LogActionSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericShort(criteria.getLogActionId()).map(LogActionSpecs::equalsLogActionId),
            notBlank(criteria.getLogActionName()).map(LogActionSpecs::equalsLogActionName)
        ));
    }

    public static Specification<LogActionEntity> equalsLogActionId(Short logActionId) {
        return (root, query, builder) -> builder.equal(root.get(LogActionEntity_.logActionId), logActionId);
    }

    public static Specification<LogActionEntity> equalsLogActionName(String logActionName) {
        return (root, query, builder) -> builder.equal(root.get(LogActionEntity_.logActionName), logActionName);
    }

}
