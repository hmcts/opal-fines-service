package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.CourtSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.CourtEntity_;

public class CourtSpecs extends BaseCourtSpecs<CourtEntity> {

    public Specification<CourtEntity> findBySearchCriteria(CourtSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByBaseCourtCriteria(criteria),
            notBlank(criteria.getCourtId()).map(CourtSpecs::equalsCourtId),
            notBlank(criteria.getCourtCode()).map(CourtSpecs::equalsCourtCode),
            notBlank(criteria.getParentCourtId()).map(CourtSpecs::equalsParentCourtId),
            notBlank(criteria.getLocalJusticeAreaId()).map(CourtSpecs::equalsLocalJusticeAreaId),
            notBlank(criteria.getNationalCourtCode()).map(CourtSpecs::equalsNationalCourtCode)

        ));
    }

    public static Specification<CourtEntity> equalsCourtId(String courtId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtId), courtId);
    }

    public static Specification<CourtEntity> equalsCourtCode(String courtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.courtCode), courtCode);
    }

    public static Specification<CourtEntity> equalsParentCourtId(String parentCourtId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.parentCourtId), parentCourtId);
    }

    public static Specification<CourtEntity> equalsLocalJusticeAreaId(String localJusticeAreaId) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.localJusticeAreaId), localJusticeAreaId);
    }

    public static Specification<CourtEntity> equalsNationalCourtCode(String nationalCourtCode) {
        return (root, query, builder) -> builder.equal(root.get(CourtEntity_.nationalCourtCode), nationalCourtCode);
    }


}
