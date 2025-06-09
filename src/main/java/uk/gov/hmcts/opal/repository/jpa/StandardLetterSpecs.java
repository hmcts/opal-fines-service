package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.StandardLetterSearchDto;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.StandardLetterEntity;
import uk.gov.hmcts.opal.entity.StandardLetterEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class StandardLetterSpecs extends EntitySpecs<StandardLetterEntity> {

    public Specification<StandardLetterEntity> findBySearchCriteria(StandardLetterSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getStandardLetterId()).map(StandardLetterSpecs::equalsStandardLetterId),
            numericShort(criteria.getBusinessUnitId()).map(StandardLetterSpecs::equalsBusinessUnitId)
        ));
    }

    public static Specification<StandardLetterEntity> equalsStandardLetterId(String standardLetterId) {
        return (root, query, builder) -> builder.equal(root.get(StandardLetterEntity_.standardLetterId),
                                                       standardLetterId);
    }

    public static Specification<StandardLetterEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Join<StandardLetterEntity, BusinessUnitEntity> joinBusinessUnit(From<?, StandardLetterEntity> from) {
        return from.join(StandardLetterEntity_.businessUnit);
    }
}
