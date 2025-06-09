package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ReportInstanceSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity;
import uk.gov.hmcts.opal.entity.ReportInstanceEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class ReportInstanceSpecs extends EntitySpecs<ReportInstanceEntity> {

    public Specification<ReportInstanceEntity> findBySearchCriteria(ReportInstanceSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getReportInstanceId()).map(ReportInstanceSpecs::equalsReportInstanceId),
            numericShort(criteria.getBusinessUnitId()).map(ReportInstanceSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<ReportInstanceEntity> equalsReportInstanceId(String reportInstanceId) {
        return (root, query, builder) -> builder.equal(root.get(ReportInstanceEntity_.reportInstanceId),
                                                       reportInstanceId);
    }

    public static Specification<ReportInstanceEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<ReportInstanceEntity, BusinessUnitEntity> joinBusinessUnit(From<?, ReportInstanceEntity> from) {
        return from.join(ReportInstanceEntity_.businessUnit);
    }
}
