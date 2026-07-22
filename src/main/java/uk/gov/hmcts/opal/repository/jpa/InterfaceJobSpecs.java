package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobEntity_;
import uk.gov.hmcts.opal.entity.InterfaceJobStatus;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity_;
import uk.gov.hmcts.opal.service.opal.InterfaceJobService.InterfaceJobSearchCriteria;

public class InterfaceJobSpecs extends EntitySpecs<InterfaceJobEntity> {

    public Specification<InterfaceJobEntity> findBySearchCriteria(InterfaceJobSearchCriteria searchCriteria) {

        return Specification.allOf(specificationList(List.of(
            notNullOrEmpty(searchCriteria.getInterfaceJobStatuses()).map(InterfaceJobSpecs::hasStatusIn),
            notNullObject(searchCriteria.getCompletedDateFrom()).map(InterfaceJobSpecs::completedDateFrom),
            notNullObject(searchCriteria.getCompletedDateTo()).map(InterfaceJobSpecs::completedDateTo),
            notBlank(searchCriteria.getInterfaceName()).map(InterfaceJobSpecs::hasInterfaceName)),
            hasBusinessUnitIn(searchCriteria.getPermittedBusinessUnitIds())));
    }

    public static Specification<InterfaceJobEntity> hasBusinessUnitIn(Collection<Short> businessUnitIds) {
        return (root, query, builder) ->
            joinBusinessUnit(root).get(BusinessUnitEntity_.businessUnitId).in(businessUnitIds);
    }

    public static Specification<InterfaceJobEntity> hasStatusIn(Collection<InterfaceJobStatus> statuses) {
        return (root, query, builder) -> root.get(InterfaceJobEntity_.status).in(statuses);
    }

    public static Specification<InterfaceJobEntity> hasInterfaceName(String interfaceName) {
        return (root, query, builder) ->
            equalsLowerCaseBothPredicate(root.get(InterfaceJobEntity_.interfaceName), builder, interfaceName);
    }

    public static Specification<InterfaceJobEntity> completedDateFrom(LocalDateTime completedDateFrom) {
        return (root, query, builder) ->
            builder.greaterThanOrEqualTo(root.get(InterfaceJobEntity_.completedDateTime), completedDateFrom);
    }

    public static Specification<InterfaceJobEntity> completedDateTo(LocalDateTime completedDateTo) {
        return (root, query, builder) ->
            builder.lessThanOrEqualTo(root.get(InterfaceJobEntity_.completedDateTime), completedDateTo);
    }

    public static Join<InterfaceJobEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, InterfaceJobEntity> from) {

        return from.join(InterfaceJobEntity_.businessUnit);
    }
}
