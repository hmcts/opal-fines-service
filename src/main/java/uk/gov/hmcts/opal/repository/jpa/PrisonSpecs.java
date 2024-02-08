package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PrisonSearchDto;
import uk.gov.hmcts.opal.entity.PrisonEntity;
import uk.gov.hmcts.opal.entity.PrisonEntity_;

public class PrisonSpecs extends EntitySpecs<PrisonEntity> {

    public Specification<PrisonEntity> findBySearchCriteria(PrisonSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getPrisonId()).map(PrisonSpecs::equalsPrisonId)
        ));
    }

    public static Specification<PrisonEntity> equalsPrisonId(String prisonId) {
        return (root, query, builder) -> builder.equal(root.get(PrisonEntity_.prisonId), prisonId);
    }

}
