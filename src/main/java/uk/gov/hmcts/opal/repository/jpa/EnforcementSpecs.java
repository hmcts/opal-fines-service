package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.EnforcementSearchDto;
import uk.gov.hmcts.opal.entity.EnforcementEntity;
import uk.gov.hmcts.opal.entity.EnforcementEntity_;

public class EnforcementSpecs extends EntitySpecs<EnforcementEntity> {

    public Specification<EnforcementEntity> findBySearchCriteria(EnforcementSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getEnforcementId()).map(EnforcementSpecs::equalsEnforcementId),
            notBlank(criteria.getPostedBy()).map(EnforcementSpecs::equalsPostedBy),
            notBlank(criteria.getReason()).map(EnforcementSpecs::equalsReason),
            notBlank(criteria.getWarrantReference()).map(EnforcementSpecs::equalsWarrantReference),
            notBlank(criteria.getCaseReference()).map(EnforcementSpecs::equalsCaseReference),
            notBlank(criteria.getAccountType()).map(EnforcementSpecs::equalsAccountType)
        ));
    }

    public static Specification<EnforcementEntity> equalsEnforcementId(String enforcementId) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.enforcementId), enforcementId);
    }

    public static Specification<EnforcementEntity> equalsPostedBy(String postedBy) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.postedBy), postedBy);
    }

    public static Specification<EnforcementEntity> equalsReason(String reason) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.reason), reason);
    }

    public static Specification<EnforcementEntity> equalsWarrantReference(String warrantReference) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.warrantReference), warrantReference);
    }

    public static Specification<EnforcementEntity> equalsCaseReference(String caseReference) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.caseReference), caseReference);
    }

    public static Specification<EnforcementEntity> equalsAccountType(String accountType) {
        return (root, query, builder) -> builder.equal(root.get(EnforcementEntity_.accountType), accountType);
    }

}
