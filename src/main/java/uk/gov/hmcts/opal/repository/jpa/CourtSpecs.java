package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.CourtEntity_;

public class CourtSpecs extends EntitySpecs<CourtEntity> {

    public Specification<CourtEntity> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getCourtId()).map(CourtSpecs::equalsCourtId)
        ));
    }

    public static Specification<CourtEntity> equalsCourtId(String courtId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtId), courtId);
    }

}
