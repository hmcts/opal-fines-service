package uk.gov.hmcts.opal.repository.jpa;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.TillSummaryEntity;
import uk.gov.hmcts.opal.entity.TillSummaryEntity_;
import uk.gov.hmcts.opal.service.opal.TillService.TillSearchCriteria;

public class TillSummarySpecs extends EntitySpecs<TillSummaryEntity> {

    public Specification<TillSummaryEntity> findBySearchCriteria(TillSearchCriteria searchCriteria) {
        return Specification.allOf(specificationList(List.of(
            notNullOrEmpty(searchCriteria.getTillStatuses()).map(TillSummarySpecs::hasStatusIn),
            notNullObject(searchCriteria.getAutoPayments()).map(TillSummarySpecs::hasAutoPayment)),
            hasBusinessUnitIn(searchCriteria.getPermittedBusinessUnitIds())));
    }

    public static Specification<TillSummaryEntity> hasBusinessUnitIn(Collection<Short> businessUnitIds) {
        return (root, query, builder) -> root.get(TillSummaryEntity_.businessUnitId).in(businessUnitIds);
    }

    public static Specification<TillSummaryEntity> hasStatusIn(Collection<TillStatusEnum> statuses) {
        return (root, query, builder) -> root.get(TillSummaryEntity_.status).in(statuses);
    }

    public static Specification<TillSummaryEntity> hasAutoPayment(Boolean autoPayment) {
        return (root, query, builder) -> builder.equal(root.get(TillSummaryEntity_.autoPayment), autoPayment);
    }
}
