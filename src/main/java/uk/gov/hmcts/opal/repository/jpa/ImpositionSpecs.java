package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.ImpositionSearchDto;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.ImpositionEntity;
import uk.gov.hmcts.opal.entity.ImpositionEntity_;
import uk.gov.hmcts.opal.entity.UserEntity;

import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.equalsCourtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.CreditorAccountSpecs.equalsCreditorAccountIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs.equalsDefendantAccountIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.equalsUserIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.UserSpecs.likeUserDescriptionPredicate;

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
        return (root, query, builder) -> equalsDefendantAccountIdPredicate(joinDefendantAccount(root), builder,
                                                                           defendantAccountId);
    }

    public static Specification<ImpositionEntity> likePostedBy(String postedBy) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ImpositionEntity_.postedBy), builder, postedBy);
    }

    public static Specification<ImpositionEntity> equalsPostedByUserId(Long postedByUserId) {
        return (root, query, builder) -> equalsUserIdPredicate(joinPostedByUser(root), builder, postedByUserId);
    }

    public static Specification<ImpositionEntity> likePostedByUserDescription(String description) {
        return (root, query, builder) -> likeUserDescriptionPredicate(joinPostedByUser(root), builder, description);
    }

    public static Specification<ImpositionEntity> likeResultId(String resultId) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(ImpositionEntity_.resultId), builder, resultId);
    }

    public static Specification<ImpositionEntity> equalsImposingCourtId(Long imposingCourtId) {
        return (root, query, builder) -> equalsCourtIdPredicate(joinImposingCourt(root), builder, imposingCourtId);
    }

    public static Specification<ImpositionEntity> equalsOffenceId(Short offenceId) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.offenceId), offenceId);
    }

    public static Specification<ImpositionEntity> equalsCreditorAccountId(Long creditorAccountId) {
        return (root, query, builder) -> equalsCreditorAccountIdPredicate(joinCreditorAccount(root), builder,
                                                                          creditorAccountId);
    }

    public static Specification<ImpositionEntity> equalsUnitFineUnits(Short unitFineUnits) {
        return (root, query, builder) -> builder.equal(root.get(ImpositionEntity_.unitFineUnits), unitFineUnits);
    }

    public static Join<ImpositionEntity, DefendantAccountEntity> joinDefendantAccount(Root<ImpositionEntity> root) {
        return root.join(ImpositionEntity_.defendantAccount);
    }

    public static Join<ImpositionEntity, CourtEntity> joinImposingCourt(Root<ImpositionEntity> root) {
        return root.join(ImpositionEntity_.imposingCourt);
    }

    public static Join<ImpositionEntity, UserEntity> joinPostedByUser(Root<ImpositionEntity> root) {
        return root.join(ImpositionEntity_.postedByUser);
    }

    public static Join<ImpositionEntity, CreditorAccountEntity> joinCreditorAccount(Root<ImpositionEntity> root) {
        return root.join(ImpositionEntity_.creditorAccount);
    }
}
