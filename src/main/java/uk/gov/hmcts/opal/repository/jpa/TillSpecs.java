package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.TillSearchDto;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillEntity_;

public class TillSpecs extends EntitySpecs<TillEntity> {

    public Specification<TillEntity> findBySearchCriteria(TillSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getTillId()).map(TillSpecs::equalsTillId)
        ));
    }

    public static Specification<TillEntity> equalsTillId(String tillId) {
        return (root, query, builder) -> builder.equal(root.get(TillEntity_.tillId), tillId);
    }

}
