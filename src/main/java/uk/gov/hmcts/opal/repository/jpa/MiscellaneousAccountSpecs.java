package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.MiscellaneousAccountSearchDto;
import uk.gov.hmcts.opal.entity.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;

import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.equalsParentBusinessUnitIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitNamePredicate;
import static uk.gov.hmcts.opal.repository.jpa.BusinessUnitSpecs.likeBusinessUnitTypePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.equalsPartyIdPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeForenamesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeInitialsPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeSurnamePredicate;

public class MiscellaneousAccountSpecs extends EntitySpecs<MiscellaneousAccountEntity> {

    public Specification<MiscellaneousAccountEntity> findBySearchCriteria(MiscellaneousAccountSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getMiscellaneousAccountId())
                .map(MiscellaneousAccountSpecs::equalsMiscellaneousAccountId),
            numericShort(criteria.getBusinessUnitId()).map(MiscellaneousAccountSpecs::equalsBusinessUnitId),
            notBlank(criteria.getBusinessUnitName()).map(MiscellaneousAccountSpecs::likeBusinessUnitName),
            notBlank(criteria.getBusinessUnitType()).map(MiscellaneousAccountSpecs::likeBusinessUnitType),
            numericShort(criteria.getParentBusinessUnitId()).map(MiscellaneousAccountSpecs::equalsParentBusinessUnitId),
            notBlank(criteria.getAccountNumber()).map(MiscellaneousAccountSpecs::likeAccountNumber),
            numericLong(criteria.getPartyId()).map(MiscellaneousAccountSpecs::equalsPartyId),
            notBlank(criteria.getSurname()).map(MiscellaneousAccountSpecs::likeSurname),
            notBlank(criteria.getForenames()).map(MiscellaneousAccountSpecs::likeForename),
            notBlank(criteria.getInitials()).map(MiscellaneousAccountSpecs::likeInitials),
            notBlank(criteria.getNiNumber()).map(MiscellaneousAccountSpecs::likeNiNumber),
            notBlank(criteria.getAddressLine()).map(MiscellaneousAccountSpecs::likeAnyAddressLine),
            notBlank(criteria.getPostcode()).map(MiscellaneousAccountSpecs::likePostcode)
        ));
    }

    public static Specification<MiscellaneousAccountEntity> equalsMiscellaneousAccountId(Long miscellaneousAccountId) {
        return (root, query, builder) -> builder.equal(root.get(MiscellaneousAccountEntity_.miscellaneousAccountId),
                                                       miscellaneousAccountId);
    }

    public static Specification<MiscellaneousAccountEntity> equalsBusinessUnitId(Short businessUnitId) {
        return (root, query, builder) ->
            equalsBusinessUnitIdPredicate(joinBusinessUnit(root), builder, businessUnitId);
    }

    public static Specification<MiscellaneousAccountEntity> likeBusinessUnitName(String businessUnitName) {
        return (root, query, builder) ->
            likeBusinessUnitNamePredicate(joinBusinessUnit(root), builder, businessUnitName);
    }

    public static Specification<MiscellaneousAccountEntity> likeBusinessUnitType(String businessUnitType) {
        return (root, query, builder) ->
            likeBusinessUnitTypePredicate(joinBusinessUnit(root), builder, businessUnitType);
    }

    public static Specification<MiscellaneousAccountEntity> equalsParentBusinessUnitId(Short parentBusinessUnitId) {
        return (root, query, builder) ->
            equalsParentBusinessUnitIdPredicate(joinBusinessUnit(root), builder, parentBusinessUnitId);
    }

    public static Specification<MiscellaneousAccountEntity> likeAccountNumber(String accountNumber) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(MiscellaneousAccountEntity_.accountNumber), builder, accountNumber);
    }

    public static Specification<MiscellaneousAccountEntity> equalsPartyId(Long partyId) {
        return (root, query, builder) -> equalsPartyIdPredicate(joinParty(root), builder, partyId);
    }

    public static Specification<MiscellaneousAccountEntity> likeSurname(String surname) {
        return (root, query, builder) ->
            likeSurnamePredicate(joinParty(root), builder, surname);
    }

    public static Specification<MiscellaneousAccountEntity> likeForename(String forename) {
        return (root, query, builder) ->
            likeForenamesPredicate(joinParty(root), builder, forename);
    }

    public static Specification<MiscellaneousAccountEntity> likeInitials(String initials) {
        return (root, query, builder) ->
            likeInitialsPredicate(joinParty(root), builder, initials);
    }

    public static Specification<MiscellaneousAccountEntity> likeNiNumber(String niNumber) {
        return (root, query, builder) ->
            likeNiNumberPredicate(joinParty(root), builder, niNumber);
    }

    public static Specification<MiscellaneousAccountEntity> likeAnyAddressLine(String addressLine) {
        return (root, query, builder) ->
            likeAnyAddressLinesPredicate(joinParty(root), builder, addressLine);
    }

    public static Specification<MiscellaneousAccountEntity> likePostcode(String postcode) {
        return (root, query, builder) ->
            likePostcodePredicate(joinParty(root), builder, postcode);
    }

    public static Join<MiscellaneousAccountEntity, BusinessUnitEntity> joinBusinessUnit(
        From<?, MiscellaneousAccountEntity> from) {
        return from.join(MiscellaneousAccountEntity_.businessUnit);
    }

    public static Join<MiscellaneousAccountEntity, PartyEntity> joinParty(From<?, MiscellaneousAccountEntity> from) {
        return from.join(MiscellaneousAccountEntity_.party);
    }
}
