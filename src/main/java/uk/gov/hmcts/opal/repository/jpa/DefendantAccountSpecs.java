package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount_;

public class DefendantAccountSpecs extends EntitySpecs<DefendantAccount.Lite> {

    public static final String DEFENDANT_ASSOC_TYPE = "Defendant";

    public Specification<DefendantAccount.Lite> findByAccountSearch(AccountSearchDto accountSearchDto) {
        return Specification.allOf(specificationList(
            // notBlank(accountSearchDto.getSurname()).map(DefendantAccountSpecs::likeSurname),
            // notBlank(accountSearchDto.getForename()).map(DefendantAccountSpecs::likeForename),
            // notBlank(accountSearchDto.getInitials()).map(DefendantAccountSpecs::likeInitials),
            // notNullLocalDate(accountSearchDto.getDateOfBirth()).map(DefendantAccountSpecs::equalsDateOfBirth),
            // notBlank(accountSearchDto.getNiNumber()).map(DefendantAccountSpecs::likeNiNumber),
            // notBlank(accountSearchDto.getAddressLine()).map(DefendantAccountSpecs::likeAnyAddressLine),
            // notBlank(accountSearchDto.getPostcode()).map(DefendantAccountSpecs::likePostcode),
            accountSearchDto.getNumericCourt().map(DefendantAccountSpecs::equalsAnyCourtId)
        ));
    }

    public static Predicate equalsDefendantAccountIdPredicate(
        From<?, DefendantAccount.Lite> from, CriteriaBuilder builder, Long defendantAccountId) {
        return builder.equal(from.get(DefendantAccount_.defendantAccountId), defendantAccountId);
    }

    public static Specification<DefendantAccount.Lite> equalsAccountNumber(String accountNo) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.accountNumber), accountNo);
    }

    public static Specification<DefendantAccount.Lite> equalsAnyCourtId(Long courtId) {
        return Specification.anyOf(
            equalsEnforcingCourtId(courtId),
            equalsLastHearingCourtId(courtId),
            equalsImposingCourtId(courtId));
    }

    public static Specification<DefendantAccount.Lite> equalsImposingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.imposingCourtId), courtId);
    }

    public static Specification<DefendantAccount.Lite> equalsEnforcingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.enforcingCourtId), courtId);
    }

    public static Specification<DefendantAccount.Lite> equalsLastHearingCourtId(Long courtId) {
        return (root, query, builder) -> builder.equal(root.get(DefendantAccount_.lastHearingCourtId), courtId);
    }

    // public static Specification<DefendantAccount.Lite> likeSurname(String surname) {
    //     return (root, query, builder) ->
    //         likeSurnamePredicate(joinDefendantParty(root, builder), builder, surname);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likeForename(String forename) {
    //     return (root, query, builder) ->
    //         likeForenamesPredicate(joinDefendantParty(root, builder), builder, forename);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likeInitials(String initials) {
    //     return (root, query, builder) ->
    //         likeInitialsPredicate(joinDefendantParty(root, builder), builder, initials);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likeOrganisationName(String organisation) {
    //     return (root, query, builder) ->
    //         likeOrganisationNamePredicate(joinDefendantParty(root, builder), builder, organisation);
    // }
    //
    // public static Specification<DefendantAccount.Lite> equalsDateOfBirth(LocalDate dob) {
    //     return (root, query, builder) ->
    //         equalsDateOfBirthPredicate(joinDefendantParty(root, builder), builder, dob);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likeNiNumber(String niNumber) {
    //     return (root, query, builder) ->
    //         likeNiNumberPredicate(joinDefendantParty(root, builder), builder, niNumber);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likeAnyAddressLine(String addressLine) {
    //     return (root, query, builder) ->
    //         likeAnyAddressLinesPredicate(joinDefendantParty(root, builder), builder, addressLine);
    // }
    //
    // public static Specification<DefendantAccount.Lite> likePostcode(String postcode) {
    //     return (root, query, builder) ->
    //         likePostcodePredicate(joinDefendantParty(root, builder), builder, postcode);
    // }


    // public static Join<DefendantAccount.Lite, CourtEntity.Lite> joinEnforcingCourt(
    //     Root<DefendantAccount.Lite> root) {
    //     return root.join(DefendantAccount_.enforcingCourtId);
    // }
    //
    // public static Join<DefendantAccount.Lite, CourtEntity.Lite> joinLastHearingCourt(
    //     Root<DefendantAccount.Lite> root) {
    //     return root.join(DefendantAccount_.lastHearingCourtId);
    // }

    // public static Join<DefendantAccountPartiesEntity, PartyEntity> joinDefendantParty(
    //     Root<DefendantAccount.Lite> root, CriteriaBuilder builder) {
    //     return joinPartyOnAssociationType(root.join(DefendantAccount_.parties), builder, DEFENDANT_ASSOC_TYPE);
    // }
}
