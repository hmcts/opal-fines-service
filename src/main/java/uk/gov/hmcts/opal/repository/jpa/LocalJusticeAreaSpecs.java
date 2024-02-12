package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity_;

public class LocalJusticeAreaSpecs extends AddressSpecs<LocalJusticeAreaEntity> {

    public Specification<LocalJusticeAreaEntity> findBySearchCriteria(LocalJusticeAreaSearchDto criteria) {
        return Specification.allOf(specificationList(
            findByAddressCriteria(criteria),
            notBlank(criteria.getLocalJusticeAreaId()).map(LocalJusticeAreaSpecs::equalsLocalJusticeAreaId)
        ));
    }

    public static Specification<LocalJusticeAreaEntity> equalsLocalJusticeAreaId(String localJusticeAreaId) {
        return (root, query, builder) -> builder.equal(root.get(LocalJusticeAreaEntity_.localJusticeAreaId),
                                                       localJusticeAreaId);
    }

}
