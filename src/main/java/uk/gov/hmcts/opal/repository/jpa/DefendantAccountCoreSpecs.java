package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore_;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityCore;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount_;

import java.time.LocalDate;

import static uk.gov.hmcts.opal.repository.jpa.DefendantAccountPartyCoreSpecs.joinPartyOnAssociationType;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsDateOfBirthPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeInitialsPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeOrganisationNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;

public class DefendantAccountCoreSpecs extends EntitySpecs<DefendantAccountCore> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

    public Specification<DefendantAccountCore> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            notBlank(accountSearchDto.getSurname()).map(DefendantAccountCoreSpecs::likeSurname),
            notBlank(accountSearchDto.getForename()).map(DefendantAccountCoreSpecs::likeForename),
            notBlank(accountSearchDto.getInitials()).map(DefendantAccountCoreSpecs::likeInitials),
            notNullLocalDate(accountSearchDto.getDateOfBirth()).map(DefendantAccountCoreSpecs::equalsDateOfBirth),
            notBlank(accountSearchDto.getNiNumber()).map(DefendantAccountCoreSpecs::likeNiNumber),
            notBlank(accountSearchDto.getAddressLine()).map(DefendantAccountCoreSpecs::likeAnyAddressLine),
            notBlank(accountSearchDto.getPostcode()).map(DefendantAccountCoreSpecs::likePostcode),
            accountSearchDto.getNumericCourt().map(DefendantAccountCoreSpecs::equalsAnyCourtId)
        ));
    }

    public static Predicate equalsDefendantAccountIdPredicate(
        From<?, DefendantAccountCore> from, CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantAccount_.defendantAccountId), defendantAccountId);
    }

    public static Specification<DefendantAccountCore> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.accountNumber), accountNo);
    }

    public static Specification<DefendantAccountCore> equalsAnyCourtId(Long courtId) {
        return Specification.anyOf(
            equalsEnforcingCourtId(courtId),
            equalsLastHearingCourtId(courtId),
            equalsImposingCourtId(courtId));
    }

    public static Specification<DefendantAccountCore> equalsImposingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.imposingCourtId), courtId);
    }

    public static Specification<DefendantAccountCore> equalsEnforcingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.enforcingCourtId), courtId);
    }

    public static Specification<DefendantAccountCore> equalsLastHearingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.lastHearingCourtId), courtId);
    }

    public static Specification<DefendantAccountCore> likeSurname(String surname) {
        return (root, query, builder) ->
            likeSurnamePredicate(joinDefendantParty(root, builder), builder, surname);
    }

    public static Specification<DefendantAccountCore> likeForename(String forename) {
        return (root, query, builder) ->
            likeForenamesPredicate(joinDefendantParty(root, builder), builder, forename);
    }

    public static Specification<DefendantAccountCore> likeInitials(String initials) {
        return (root, query, builder) ->
            likeInitialsPredicate(joinDefendantParty(root, builder), builder, initials);
    }

    public static Specification<DefendantAccountCore> likeOrganisationName(String organisation) {
        return (root, query, builder) ->
            likeOrganisationNamePredicate(joinDefendantParty(root, builder), builder, organisation);
    }

    public static Specification<DefendantAccountCore> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) ->
            equalsDateOfBirthPredicate(joinDefendantParty(root, builder), builder, dob);
    }

    public static Specification<DefendantAccountCore> likeNiNumber(String niNumber) {
        return (root, query, builder) ->
            likeNiNumberPredicate(joinDefendantParty(root, builder), builder, niNumber);
    }

    public static Specification<DefendantAccountCore> likeAnyAddressLine(String addressLine) {
        return (root, query, builder) ->
            likeAnyAddressLinesPredicate(joinDefendantParty(root, builder), builder, addressLine);
    }

    public static Specification<DefendantAccountCore> likePostcode(String postcode) {
        return (root, query, builder) ->
            likePostcodePredicate(joinDefendantParty(root, builder), builder, postcode);
    }


    // public static Join<DefendantAccount.Lite, CourtEntity.Lite> joinEnforcingCourt(
    //     Root<DefendantAccount.Lite> root) {
    //     return root.join(DefendantAccount_.enforcingCourtId);
    // }
    //
    // public static Join<DefendantAccount.Lite, CourtEntity.Lite> joinLastHearingCourt(
    //     Root<DefendantAccount.Lite> root) {
    //     return root.join(DefendantAccount_.lastHearingCourtId);
    // }

    public static Join<DefendantAccountPartiesEntityCore, PartyEntity> joinDefendantParty(
        Root<DefendantAccountCore> root, CriteriaBuilder builder) {
        return joinPartyOnAssociationType(root.join(DefendantAccountCore_.parties), builder, DEFENDANT_ASSOC_TYPE);
    }
}
