package uk.gov.hmcts.opal.repository.jpa;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.FixedPenaltyOffenceSearchDto;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity_;

public class FixedPenaltyOffenceSpecs extends EntitySpecs<FixedPenaltyOffenceEntity> {

    public Specification<FixedPenaltyOffenceEntity> findBySearchCriteria(FixedPenaltyOffenceSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getDefendantAccountId()).map(FixedPenaltyOffenceSpecs::equalsDefendantAccountId),
            notBlank(criteria.getTicketNumber()).map(FixedPenaltyOffenceSpecs::likeTicketNumber),
            notBlank(criteria.getVehicleRegistration()).map(FixedPenaltyOffenceSpecs::likeVehicleRegistration),
            notBlank(criteria.getOffenceLocation()).map(FixedPenaltyOffenceSpecs::likeOffenceLocation),
            notBlank(criteria.getNoticeNumber()).map(FixedPenaltyOffenceSpecs::likeNoticeNumber),
            notBlank(criteria.getLicenceNumber()).map(FixedPenaltyOffenceSpecs::likeLicenceNumber)
        ));
    }

    public static Specification<FixedPenaltyOffenceEntity> equalsDefendantAccountId(Long defendantAccountId) {
        return (root, query, builder) -> builder.equal(root.get(FixedPenaltyOffenceEntity_.defendantAccountId),
                                                       defendantAccountId);
    }

    public static Specification<FixedPenaltyOffenceEntity> likeTicketNumber(String ticketNumber) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(FixedPenaltyOffenceEntity_.ticketNumber),
                                                               builder, ticketNumber);
    }

    public static Specification<FixedPenaltyOffenceEntity> likeVehicleRegistration(String vehicleRegistration) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(FixedPenaltyOffenceEntity_.vehicleRegistration),
                                                               builder, vehicleRegistration);
    }

    public static Specification<FixedPenaltyOffenceEntity> likeOffenceLocation(String offenceLocation) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(FixedPenaltyOffenceEntity_.offenceLocation),
                                                               builder, offenceLocation);
    }

    public static Specification<FixedPenaltyOffenceEntity> likeNoticeNumber(String noticeNumber) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(FixedPenaltyOffenceEntity_.noticeNumber),
                                                               builder, noticeNumber);
    }

    public static Specification<FixedPenaltyOffenceEntity> likeLicenceNumber(String licenceNumber) {
        return (root, query, builder) -> likeWildcardPredicate(root.get(FixedPenaltyOffenceEntity_.licenceNumber),
                                                               builder, licenceNumber);
    }


}
