package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.CourtEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

import java.time.LocalDate;

public class DefendantAccountSpecs extends EntitySpecs<DefendantAccountEntity> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

    public Specification<DefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notBlank(accountSearchDto.getSurname()).map(DefendantAccountSpecs::likeSurname),
            notBlank(accountSearchDto.getForename()).map(DefendantAccountSpecs::likeForename),
            notBlank(accountSearchDto.getInitials()).map(DefendantAccountSpecs::likeInitials),
            notBlank(accountSearchDto.getNiNumber()).map(DefendantAccountSpecs::likeNiNumber),
            notBlank(accountSearchDto.getAddressLineOne()).map(DefendantAccountSpecs::likeAddressLine1),
            notNullLocalDate(accountSearchDto.getDateOfBirth()).map(DefendantAccountSpecs::equalsDateOfBirth),
            accountSearchDto.getNumericCourt().map(DefendantAccountSpecs::equalsAnyCourtId)
        ));
    }

    public static Specification<DefendantAccountEntity> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DefendantAccountEntity_.accountNumber), accountNo);
        };
    }

    public static Specification<DefendantAccountEntity> equalsAnyCourtId(Long courtId) {
        return Specification.anyOf(
            equalsImposingCourtId(courtId),
            equalsEnforcingCourtId(courtId),
            equalsLastHearingCourtId(courtId));
    }

    public static Specification<DefendantAccountEntity> equalsImposingCourtId(Long courtId) {
        return (root, query, builder) -> {
            return builder.equal(root.get(DefendantAccountEntity_.imposingCourtId), courtId);
        };
    }

    public static Specification<DefendantAccountEntity> equalsEnforcingCourtId(Long courtId) {
        return (root, query, builder) -> {
            return builder.equal(joinEnforcingCourt(root).get(CourtEntity_.courtId), courtId);
        };
    }

    public static Specification<DefendantAccountEntity> equalsLastHearingCourtId(Long courtId) {
        return (root, query, builder) -> {
            return builder.equal(joinLastHearingCourt(root).get(CourtEntity_.courtId), courtId);
        };
    }

    public static Specification<DefendantAccountEntity> likeSurname(String surname) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.surname)), "%" + surname.toLowerCase() + "%");
        };
    }

    public static Specification<DefendantAccountEntity> likeForename(String forename) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.forenames)), "%" + forename.toLowerCase() + "%");
        };
    }

    public static Specification<DefendantAccountEntity> likeOrganisationName(String organisation) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.organisationName)), "%" + organisation.toLowerCase() + "%");
        };
    }

    public static Specification<DefendantAccountEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) -> {
            return builder.equal(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.dateOfBirth), dob);
        };
    }

    public static Specification<DefendantAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.niNumber)), "%" + niNumber.toLowerCase() + "%");
        };
    }

    public static Specification<DefendantAccountEntity> likeAddressLine1(String addressLine) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.addressLine1)), "%" + addressLine.toLowerCase() + "%");
        };
    }

    public static Specification<DefendantAccountEntity> likeInitials(String initials) {
        return (root, query, builder) -> {
            return builder.like(builder.lower(joinPartyOnAssociationType(root, builder, DEFENDANT_ASSOC_TYPE)
                          .get(PartyEntity_.initials)), "%" + initials.toLowerCase() + "%");
        };
    }

    public static Join<DefendantAccountEntity, CourtEntity> joinEnforcingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.enforcingCourt);
    }

    public static Join<DefendantAccountEntity, CourtEntity> joinLastHearingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinPartyOnAssociationType(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder, String assocType) {
        return onAssociationType(builder, root.join(DefendantAccountEntity_.parties), assocType)
            .join(DefendantAccountPartiesEntity_.party);
    }


    public static ListJoin<DefendantAccountEntity, DefendantAccountPartiesEntity> onAssociationType(
        CriteriaBuilder builder,
        ListJoin<DefendantAccountEntity, DefendantAccountPartiesEntity> parties,
        String assocType) {
        return parties.on(builder.equal(parties.get(DefendantAccountPartiesEntity_.associationType), assocType));
    }
}
