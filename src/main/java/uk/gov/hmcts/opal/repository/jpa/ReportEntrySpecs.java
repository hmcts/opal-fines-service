package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ReportEntrySearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.ReportEntryEntity;
import uk.gov.hmcts.opal.entity.ReportEntryEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class ReportEntrySpecs extends EntitySpecs<ReportEntryEntity> {

    public Specification<ReportEntryEntity> findBySearchCriteria(ReportEntrySearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getReportEntryId()).map(ReportEntrySpecs::equalsReportEntryId),
            numericShort(criteria.getBusinessUnitId()).map(ReportEntrySpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<ReportEntryEntity> equalsReportEntryId(String reportEntryId) {
        return (root, query, builder) -> builder.equal(root.get(ReportEntryEntity_.reportEntryId), reportEntryId);
    }

    public static Specification<ReportEntryEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<ReportEntryEntity, BusinessUnitEntity> joinBusinessUnit(From<?, ReportEntryEntity> from) {
        return from.join(ReportEntryEntity_.businessUnit);
    }
}
