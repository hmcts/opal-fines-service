package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.DraftAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity;
import uk.gov.hmcts.opal.entity.DraftAccountEntity_;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;

public class DraftAccountSpecs extends EntitySpecs<DraftAccountEntity> {

    public Specification<DraftAccountEntity> findBySearchCriteria(DraftAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getDraftAccountId()).map(DraftAccountSpecs::equalsDraftAccountId),
            numericShort(criteria.getBusinessUnitId()).map(DraftAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getAccountType()).map(DraftAccountSpecs::likeAccountType),
            notBlank(criteria.getAccountStatus()).map(DraftAccountSpecs::likeAccountStatus)
        ));
    }

    public static Specification<DraftAccountEntity> equalsDraftAccountId(Long draftAccountId) {
        return (root, query, builder) -> builder.equal(root.get(DraftAccountEntity_.draftAccountId), draftAccountId);
    }

    public static Specification<DraftAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<DraftAccountEntity> likeAccountType(String accountType) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DraftAccountEntity_.accountType), builder, accountType);
    }

    public static Specification<DraftAccountEntity> likeAccountStatus(String accountStatus) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(DraftAccountEntity_.accountStatus), builder, accountStatus);
    }

    public static Join<DraftAccountEntity, BusinessUnitEntity> joinBusinessUnit(From<?, DraftAccountEntity> from) {
        return from.join(DraftAccountEntity_.businessUnit);
    }
}
