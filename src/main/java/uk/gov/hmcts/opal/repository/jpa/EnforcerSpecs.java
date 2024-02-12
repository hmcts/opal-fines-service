package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity_;

public class EnforcerSpecs extends BaseCourtSpecs<EnforcerEntity> {

    public Specification<EnforcerEntity> findBySearchCriteria(EnforcerSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByBaseCourtCriteria(criteria),
            notBlank(criteria.getEnforcerId()).map(EnforcerSpecs::equalsEnforcerId),
            notBlank(criteria.getEnforcerCode()).map(EnforcerSpecs::equalsEnforcerCode),
            notBlank(criteria.getWarrantReferenceSequence()).map(EnforcerSpecs::equalswarrantReferenceSequence),
            notBlank(criteria.getWarrantRegisterSequence()).map(EnforcerSpecs::equalswarrantRegisterSequence)
        ));
    }

    public static Specification<EnforcerEntity> equalsEnforcerId(String enforcerId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.enforcerId), enforcerId);
    }

    public static Specification<EnforcerEntity> equalsEnforcerCode(String enforcerCode) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.enforcerCode), enforcerCode);
    }

    public static Specification<EnforcerEntity> equalswarrantReferenceSequence(String warrantReferenceSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.warrantReferenceSequence),
                                                       warrantReferenceSequence);
    }

    public static Specification<EnforcerEntity> equalswarrantRegisterSequence(String warrantRegisterSequence) {
        return (root, query, builder) -> builder.equal(root.get(EnforcerEntity_.warrantRegisterSequence),
                                                       warrantRegisterSequence);
    }

}
