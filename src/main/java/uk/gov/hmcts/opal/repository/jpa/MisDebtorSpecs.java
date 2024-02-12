package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MisDebtorSearchDto;
import uk.gov.hmcts.opal.entity.MisDebtorEntity;
import uk.gov.hmcts.opal.entity.MisDebtorEntity_;

public class MisDebtorSpecs extends EntitySpecs<MisDebtorEntity> {

    public Specification<MisDebtorEntity> findBySearchCriteria(MisDebtorSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getMisDebtorId()).map(MisDebtorSpecs::equalsMisDebtorId),
            notBlank(criteria.getDebtorName()).map(MisDebtorSpecs::equalsDebtorName),
            notBlank(criteria.getDaysInJail()).map(MisDebtorSpecs::equalsDaysInJail)
        ));
    }

    public static Specification<MisDebtorEntity> equalsMisDebtorId(String misDebtorId) {
        return (root, query, builder) -> builder.equal(root.get(MisDebtorEntity_.misDebtorId), misDebtorId);
    }

    public static Specification<MisDebtorEntity> equalsDebtorName(String debtorName) {
        return (root, query, builder) -> builder.equal(root.get(MisDebtorEntity_.debtorName), debtorName);
    }

    public static Specification<MisDebtorEntity> equalsDaysInJail(String daysInJail) {
        return (root, query, builder) -> builder.equal(root.get(MisDebtorEntity_.daysInJail), daysInJail);
    }

}
