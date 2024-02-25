package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.CourtEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;

import java.time.LocalDate;

import static uk.gov.hmcts.opal.repository.jpa.CourtSpecs.courtIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartySpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.addressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.dateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.forenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.initialsPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.niNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.organisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.postcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.surnamePredicate;

public class DefendantAccountSpecs extends EntitySpecs<DefendantAccountEntity> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

    public Specification<DefendantAccountEntity> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notBlank(accountSearchDto.getSurname()).map(DefendantAccountSpecs::likeSurname),
            notBlank(accountSearchDto.getForename()).map(DefendantAccountSpecs::likeForename),
            notBlank(accountSearchDto.getInitials()).map(DefendantAccountSpecs::likeInitials),
            notNullLocalDate(accountSearchDto.getDateOfBirth()).map(DefendantAccountSpecs::equalsDateOfBirth),
            notBlank(accountSearchDto.getNiNumber()).map(DefendantAccountSpecs::likeNiNumber),
            notBlank(accountSearchDto.getAddressLine()).map(DefendantAccountSpecs::likeAnyAddressLine),
            notBlank(accountSearchDto.getPostcode()).map(DefendantAccountSpecs::likePostcode),
            accountSearchDto.getNumericCourt().map(DefendantAccountSpecs::equalsAnyCourtId)
        ));
    }

    public static Specification<DefendantAccountEntity> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccountEntity_.accountNumber), accountNo);
    }

    public static Specification<DefendantAccountEntity> equalsAnyCourtId(Long courtId) {
        return Specification.anyOf(
            equalsImposingCourtId(courtId),
            equalsEnforcingCourtId(courtId),
            equalsLastHearingCourtId(courtId));
    }

    public static Specification<DefendantAccountEntity> equalsImposingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccountEntity_.imposingCourtId), courtId);
    }

    public static Specification<DefendantAccountEntity> equalsEnforcingCourtId(Long courtId) {
        return (root, query, builder) -> courtIdPredicate(joinEnforcingCourt(root), builder, courtId);
    }

    public static Specification<DefendantAccountEntity> equalsLastHearingCourtId(Long courtId) {
        return (root, query, builder) -> courtIdPredicate(joinLastHearingCourt(root), builder, courtId);
    }

    public static Specification<DefendantAccountEntity> likeSurname(String surname) {
        return (root, query, builder) ->
            surnamePredicate(joinDefendantParty(root, builder), builder, surname);
    }

    public static Specification<DefendantAccountEntity> likeForename(String forename) {
        return (root, query, builder) ->
            forenamesPredicate(joinDefendantParty(root, builder), builder, forename);
    }

    public static Specification<DefendantAccountEntity> likeInitials(String initials) {
        return (root, query, builder) ->
            initialsPredicate(joinDefendantParty(root, builder), builder, initials);
    }

    public static Specification<DefendantAccountEntity> likeOrganisationName(String organisation) {
        return (root, query, builder) ->
            organisationNamePredicate(joinDefendantParty(root, builder), builder, organisation);
    }

    public static Specification<DefendantAccountEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) ->
            dateOfBirthPredicate(joinDefendantParty(root, builder), builder, dob);
    }

    public static Specification<DefendantAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) ->
            niNumberPredicate(joinDefendantParty(root, builder), builder, niNumber);
    }

    public static Specification<DefendantAccountEntity> likeAnyAddressLine(String addressLine) {
        return (root, query, builder) ->
            addressLinesPredicate(joinDefendantParty(root, builder), builder, addressLine);
    }

    public static Specification<DefendantAccountEntity> likePostcode(String postcode) {
        return (root, query, builder) ->
            postcodePredicate(joinDefendantParty(root, builder), builder, postcode);
    }


    public static Join<DefendantAccountEntity, CourtEntity> joinEnforcingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.enforcingCourt);
    }

    public static Join<DefendantAccountEntity, CourtEntity> joinLastHearingCourt(Root<DefendantAccountEntity> root) {
        return root.join(DefendantAccountEntity_.lastHearingCourt);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
        Root<DefendantAccountEntity> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountEntity_.parties), builder, DEFENDANT_ASSOC_TYPE);
    }
}
