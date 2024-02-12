package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.entity.PrisonEntity_;

public class PrisonSpecs extends AddressSpecs<PrisonEntity> {

    public Specification<PrisonEntity> findBySearchCriteria(PrisonSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getPrisonId()).map(PrisonSpecs::equalsPrisonId),
            notBlank(criteria.getPrisonCode()).map(PrisonSpecs::equalsPrisonCode)
        ));
    }

    public static Specification<PrisonEntity> equalsPrisonId(String prisonId) {
        return (root, query, builder) -> builder.equal(root.get(PrisonEntity_.prisonId), prisonId);
    }

    public static Specification<PrisonEntity> equalsPrisonCode(String prisonCode) {
        return (root, query, builder) -> builder.equal(root.get(PrisonEntity_.prisonCode), prisonCode);
    }

}
