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
            notBlank(criteria.getAddressLine()).map(PartySpecs::likeAddressLine),
            notBlank(criteria.getPostcode()).map(PartySpecs::likePostcode)
        ));
    }

    public static Specification<PartyEntity> equalsPartyId(Long partyId) {
        return (root, query, builder) -> partyIdPredicate(root, builder, partyId);
    }

    public static Predicate partyIdPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, Long partyId) {
        return builder.equal(from.get(PartyEntity_.partyId), partyId);
    }

    public static Specification<PartyEntity> likeOrganisationName(String organisationName) {
        return (root, query, builder) -> organisationNamePredicate(root, builder, organisationName);
    }

    public static Predicate organisationNamePredicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String organisationName) {
        return likeWildcardPredicate(from.get(PartyEntity_.organisationName), builder, organisationName);
    }

    public static Specification<PartyEntity> likeSurname(String surname) {
        return (root, query, builder) -> surnamePredicate(root, builder, surname);
    }

    public static Predicate surnamePredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String surname) {
        return likeWildcardPredicate(from.get(PartyEntity_.surname), builder, surname);
    }


    public static Specification<PartyEntity> likeForenames(String forenames) {
        return (root, query, builder) -> forenamesPredicate(root, builder, forenames);
    }

    public static Predicate forenamesPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String forenames) {
        return likeWildcardPredicate(from.get(PartyEntity_.forenames), builder, forenames);
    }

    public static Specification<PartyEntity> likeAddressLine(String addressLine) {
        return (root, query, builder) -> addressLinePredicate(root, builder, addressLine);
    }

    public static Predicate addressLinePredicate(
        From<?, PartyEntity> from, CriteriaBuilder builder, String addressLine) {
        return likeWildcardPredicate(from.get(PartyEntity_.addressLine1), builder, addressLine);
    }

    public static Specification<PartyEntity> likePostcode(String postcode) {
        return (root, query, builder) -> postcodePredicate(root, builder, postcode);
    }

    public static Predicate postcodePredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String postcode) {
        return likeWildcardPredicate(from.get(PartyEntity_.postcode), builder, postcode);
    }

    public static Predicate niNumberPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String niNumber) {
        return likeWildcardPredicate(from.get(PartyEntity_.niNumber), builder, niNumber);
    }

    public static Predicate initialsPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, String initials) {
        return likeWildcardPredicate(from.get(PartyEntity_.initials), builder, initials);
    }

    public static Predicate dateOfBirthPredicate(From<?, PartyEntity> from, CriteriaBuilder builder, LocalDate dob) {
        return builder.equal(from.get(PartyEntity_.initials), dob);
    }

}
