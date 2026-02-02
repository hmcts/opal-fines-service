package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.PartySearchDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

import java.time.LocalDate;

public class PartySpecs extends EntitySpecs<PartyEntity> {

    public Specification<PartyEntity> findBySearchCriteria(PartySearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getPartyId()).map(PartySpecs::equalsPartyId),
            notBlank(criteria.getOrganisationName()).map(PartySpecs::likeOrganisationName),
            notBlank(criteria.getSurname()).map(PartySpecs::likeSurname),
            notBlank(criteria.getForenames()).map(PartySpecs::likeForenames),
            notNullLocalDate(criteria.getDateOfBirth()).map(PartySpecs::equalsDateOfBirth),
            notBlank(criteria.getNiNumber()).map(PartySpecs::likeNiNumber),
            notBlank(criteria.getAddressLine()).map(PartySpecs::likeAnyAddressLine),
            notBlank(criteria.getPostcode()).map(PartySpecs::likePostcode)
        ));
    }

    public static Specification<PartyEntity> equalsPartyId(Long partyId) {
        return (root, query, builder) -> equalsPartyIdPredicate(root, builder, partyId);
    }

    public static Predicate equalsPartyIdPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, Long partyId) {
        return builder.equal(from.get(PartyEntity_.partyId), partyId);
    }

    public static Specification<PartyEntity> likeOrganisationName(String organisationName) {
        return (root, query, builder) -> likeOrganisationNamePredicate(root, builder, organisationName);
    }

    public static Predicate likeOrganisationNamePredicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String organisationName) {
        return likeWildcardPredicate(from.get(PartyEntity_.organisationName), builder, organisationName);
    }

    public static Specification<PartyEntity> likeSurname(String surname) {
        return (root, query, builder) -> likeSurnamePredicate(root, builder, surname);
    }

    public static Predicate likeSurnamePredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String surname) {
        return likeWildcardPredicate(from.get(PartyEntity_.surname), builder, surname);
    }

    public static Specification<PartyEntity> likeForenames(String forenames) {
        return (root, query, builder) -> likeForenamesPredicate(root, builder, forenames);
    }

    public static Predicate likeForenamesPredicate(From<?, PartyEntity> from, CriteriaBuilder builder,
                                                   String forenames) {
        return likeWildcardPredicate(from.get(PartyEntity_.forenames), builder, forenames);
    }

    public static Specification<PartyEntity> equalsDateOfBirth(LocalDate dob) {
        return (root, query, builder) -> equalsDateOfBirthPredicate(root, builder, dob);
    }

    public static Predicate equalsDateOfBirthPredicate(From<?, PartyEntity> from, CriteriaBuilder builder,
                                                       LocalDate dob) {
        return builder.equal(from.get(PartyEntity_.birthDate), dob);
    }

    public static Specification<PartyEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) -> likeNiNumberPredicate(root, builder, niNumber);
    }

    public static Predicate likeNiNumberPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String niNumber) {
        return likeWildcardPredicate(from.get(PartyEntity_.niNumber), builder, niNumber);
    }

    private static Specification<PartyEntity> likeAnyAddressLine(String addressLine) {
        return (root, query, builder) -> likeAnyAddressLinesPredicate(root, builder, addressLine);
    }

    public static Predicate likeAnyAddressLinesPredicate(From<?, PartyEntity> from, CriteriaBuilder builder,
                                                         String addressLine) {
        String addressLinePattern = "%" + addressLine.toLowerCase() + "%";
        return builder.or(
            likeAddressLine1Predicate(from, builder, addressLinePattern),
            likeAddressLine2Predicate(from, builder, addressLinePattern),
            likeAddressLine3Predicate(from, builder, addressLinePattern),
            likeAddressLine4Predicate(from, builder, addressLinePattern),
            likeAddressLine5Predicate(from, builder, addressLinePattern)
        );
    }

    private static Predicate likeAddressLine1Predicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLinePattern) {
        return likeLowerCaseBothPredicate(from.get(PartyEntity_.addressLine1), builder, addressLinePattern);
    }

    private static Predicate likeAddressLine2Predicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLinePattern) {
        return likeLowerCaseBothPredicate(from.get(PartyEntity_.addressLine2), builder, addressLinePattern);
    }

    private static Predicate likeAddressLine3Predicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLinePattern) {
        return likeLowerCaseBothPredicate(from.get(PartyEntity_.addressLine3), builder, addressLinePattern);
    }

    private static Predicate likeAddressLine4Predicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLinePattern) {
        return likeLowerCaseBothPredicate(from.get(PartyEntity_.addressLine4), builder, addressLinePattern);
    }

    private static Predicate likeAddressLine5Predicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLinePattern) {
        return likeLowerCaseBothPredicate(from.get(PartyEntity_.addressLine5), builder, addressLinePattern);
    }

    public static Specification<PartyEntity> likePostcode(String postcode) {
        return (root, query, builder) -> likePostcodePredicate(root, builder, postcode);
    }

    public static Predicate likePostcodePredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String postcode) {
        return likeWildcardPredicate(from.get(PartyEntity_.postcode), builder, postcode);
    }

}
