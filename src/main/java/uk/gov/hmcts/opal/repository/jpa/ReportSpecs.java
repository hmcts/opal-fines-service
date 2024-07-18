package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ReportSearchDto;
import uk.gov.hmcts.opal.entity.ReportEntity;
import uk.gov.hmcts.opal.entity.ReportEntity_;

public class ReportSpecs extends EntitySpecs<ReportEntity> {

    public Specification<ReportEntity> findBySearchCriteria(ReportSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getReportId()).map(ReportSpecs::equalsReportId)
        ));
    }

    public static Specification<ReportEntity> equalsReportId(String reportId) {
        return (root, query, builder) -> builder.equal(root.get(ReportEntity_.reportId), reportId);
    }
}
