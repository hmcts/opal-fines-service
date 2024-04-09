package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.entity.ImpositionEntity_;

public class ImpositionSpecs extends EntitySpecs<ImpositionEntity> {

    public Specification<ImpositionEntity> findBySearchCriteria(ImpositionSearchDto criteria) {
        return Specification.allOf(specificationList(
            notBlank(criteria.getImpositionId()).map(ImpositionSpecs::equalsImpositionId),
            numericLong(criteria.getDefendantAccountId()).map(ImpositionSpecs::equalsDefendantAccountId),
            notBlank(criteria.getPostedBy()).map(ImpositionSpecs::likePostedBy),
            numericLong(criteria.getPostedByUserId()).map(ImpositionSpecs::equalsPostedByUserId),
            notBlank(criteria.getResultId()).map(ImpositionSpecs::likeResultId),
            numericLong(criteria.getImposingCourtId()).map(ImpositionSpecs::equalsImposingCourtId),
            numericShort(criteria.getOffenceId()).map(ImpositionSpecs::equalsOffenceId),
            numericLong(criteria.getCreditorAccountId()).map(ImpositionSpecs::equalsCreditorAccountId),
            numericShort(criteria.getUnitFineUnits()).map(ImpositionSpecs::equalsUnitFineUnits)
        ));
    }

    public static Specification<ImpositionEntity> equalsImpositionId(String impositionId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.impositionId), impositionId);
    }

    public static Specification<ImpositionEntity> equalsDefendantAccountId(Long defendantAccountId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.defendantAccountId),
                                                       defendantAccountId);
    }

    public static Specification<ImpositionEntity> likePostedBy(String postedBy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ImpositionEntity_.postedBy), builder, postedBy);
    }

    public static Specification<ImpositionEntity> equalsPostedByUserId(Long postedByUserId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.postedByUserId), postedByUserId);
    }

    public static Specification<ImpositionEntity> likeResultId(String resultId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ImpositionEntity_.resultId), builder, resultId);
    }

    public static Specification<ImpositionEntity> equalsImposingCourtId(Long imposingCourtId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.imposingCourtId), imposingCourtId);
    }

    public static Specification<ImpositionEntity> equalsOffenceId(Short offenceId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.offenceId), offenceId);
    }

    public static Specification<ImpositionEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.creditorAccountId),
                                                       creditorAccountId);
    }

    public static Specification<ImpositionEntity> equalsUnitFineUnits(Short unitFineUnits) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.unitFineUnits), unitFineUnits);
    }
}
