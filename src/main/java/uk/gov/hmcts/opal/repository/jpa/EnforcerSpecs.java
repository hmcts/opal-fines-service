package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerLegacyEntity;
import uk.gov.hmcts.opal.entity.EnforcerLegacyEntity_;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

import java.util.Optional;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class EnforcerSpecs extends AddressCySpecs<EnforcerLegacyEntity> {

    public Specification<EnforcerLegacyEntity> findBySearchCriteria(EnforcerSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCyCriteria(criteria),
            notBlank(criteria.getEnforcerId()).map(EnforcerSpecs::equalsEnforcerId),
            notBlank(criteria.getEnforcerCode()).map(EnforcerSpecs::equalsEnforcerCode),
            notBlank(criteria.getWarrantReferenceSequence()).map(EnforcerSpecs::equalsWarrantReferenceSequence),
            notBlank(criteria.getWarrantRegisterSequence()).map(EnforcerSpecs::equalsWarrantRegisterSequence)
        ));
    }

    public Specification<EnforcerLegacyEntity> referenceDataFilter(Optional<String> filter) {
        return Specification.allOf(specificationList(
            filter.filter(s -> !s.isBlank()).map(this::likeAnyEnforcer)
        ));
    }

    public static Specification<EnforcerLegacyEntity> equalsEnforcerId(String enforcerId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerLegacyEntity_.enforcerId), enforcerId);
    }

    public static Specification<EnforcerLegacyEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<EnforcerLegacyEntity> equalsEnforcerCode(String enforcerCode) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerLegacyEntity_.enforcerCode), enforcerCode);
    }

    public static Specification<EnforcerLegacyEntity> equalsWarrantReferenceSequence(String warrantReferenceSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerLegacyEntity_.warrantReferenceSequence),
                                                       warrantReferenceSequence);
    }

    public static Specification<EnforcerLegacyEntity> equalsWarrantRegisterSequence(String warrantRegisterSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerLegacyEntity_.warrantRegisterSequence),
                                                       warrantRegisterSequence);
    }

    public Specification<EnforcerLegacyEntity> likeAnyEnforcer(String filter) {
        return Specification.anyOf(
            likeName(filter),
            likeNameCy(filter)
        );
    }

    public static Join<EnforcerLegacyEntity, BusinessUnitEntity> joinBusinessUnit(From<?, EnforcerLegacyEntity> from) {
        return from.join(EnforcerLegacyEntity_.businessUnit);
    }
}
