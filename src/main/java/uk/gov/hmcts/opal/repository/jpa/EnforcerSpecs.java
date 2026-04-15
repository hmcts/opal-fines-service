package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity_;

import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class EnforcerSpecs extends AddressCySpecs<EnforcerEntity> {

    public Specification<EnforcerEntity> findBySearchCriteria(EnforcerSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCyCriteria(criteria),
            notBlank(criteria.getEnforcerId()).map(EnforcerSpecs::equalsEnforcerId),
            notBlank(criteria.getEnforcerCode()).map(EnforcerSpecs::equalsEnforcerCode),
            notBlank(criteria.getWarrantReferenceSequence()).map(EnforcerSpecs::equalsWarrantReferenceSequence),
            notBlank(criteria.getWarrantRegisterSequence()).map(EnforcerSpecs::equalsWarrantRegisterSequence)
        ));
    }

    public Specification<EnforcerEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyEnforcer)
        ));
    }

    public static Specification<EnforcerEntity> equalsEnforcerId(String enforcerId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.enforcerId), enforcerId);
    }

    public static Specification<EnforcerEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<EnforcerEntity> equalsEnforcerCode(String enforcerCode) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.enforcerCode), enforcerCode);
    }

    public static Specification<EnforcerEntity> equalsWarrantReferenceSequence(String warrantReferenceSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.warrantReferenceSequence),
                                                       warrantReferenceSequence);
    }

    public static Specification<EnforcerEntity> equalsWarrantRegisterSequence(String warrantRegisterSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.warrantRegisterSequence),
                                                       warrantRegisterSequence);
    }

    public Specification<EnforcerEntity> likeAnyEnforcer(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter)
        );
    }

    public static Join<EnforcerEntity, BusinessUnitEntity> joinBusinessUnit(From<?, EnforcerEntity> from) {
        return from.join(EnforcerEntity_.businessUnit);
    }
}
