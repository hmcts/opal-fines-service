package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.dto.search.AliasSearchDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.AliasEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;

import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeAnyAddressLinesPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likeNiNumberPredicate;
import static uk.gov.hmcts.opal.repository.jpa.PartySpecs.likePostcodePredicate;

public class AliasSpecs extends EntitySpecs<AliasEntity> {

    public Specification<AliasEntity> findBySearchCriteria(AliasSearchDto criteria) {
        return Specification.allOf(specificationList(
            numericLong(criteria.getAliasId()).map(AliasSpecs::equalsAliasId),
            notBlank(criteria.getSurname()).map(AliasSpecs::likeEitherSurname),
            notBlank(criteria.getForenames()).map(AliasSpecs::likeEitherForenames),
            notBlank(criteria.getNiNumber()).map(AliasSpecs::likePartyNiNumber),
            notBlank(criteria.getAddressLine()).map(AliasSpecs::likeAnyPartyAddressLine),
            notBlank(criteria.getPostcode()).map(AliasSpecs::likePartyPostcode),
            numericInteger(criteria.getSequenceNumber()).map(AliasSpecs::equalsSequenceNumber)
        ));
    }

    public static Specification<AliasEntity> equalsAliasId(Long aliasId) {
        return (root, query, builder) -> builder.equal(root.get(AliasEntity_.aliasId), aliasId);
    }

    public static Specification<AliasEntity> likeEitherSurname(String surname) {
        return Specification.anyOf(
            likeSurname(surname),
            likePartySurname(surname));
    }

    private static Specification<AliasEntity> likeSurname(String surname) {
        return (root, query, builder) ->
             likeWildcardPredicate(root.get(AliasEntity_.surname), builder, surname);
    }

    private static Specification<AliasEntity> likePartySurname(String surname) {
        return (root, query, builder) ->
            PartySpecs.likeSurnamePredicate(joinParty(root), builder, surname);
    }

    public static Specification<AliasEntity> likeEitherForenames(String forenames) {
        return Specification.anyOf(
            likeForenames(forenames),
            likePartyForenames(forenames));
    }

    private static Specification<AliasEntity> likeForenames(String forenames) {
        return (root, query, builder) ->
            likeWildcardPredicate(root.get(AliasEntity_.forenames), builder, forenames);
    }

    public static Specification<AliasEntity> likePartyForenames(String forenames) {
        return (root, query, builder) ->
            PartySpecs.likeForenamesPredicate(joinParty(root), builder, forenames);
    }

    public static Specification<AliasEntity> likePartyNiNumber(String niNumber) {
        return (root, query, builder) ->
            likeNiNumberPredicate(joinParty(root), builder, niNumber);
    }

    public static Specification<AliasEntity> likeAnyPartyAddressLine(String addressLine) {
        return (root, query, builder) ->
            likeAnyAddressLinesPredicate(joinParty(root), builder, addressLine);
    }

    public static Specification<AliasEntity> likePartyPostcode(String postcode) {
        return (root, query, builder) ->
            likePostcodePredicate(joinParty(root), builder, postcode);
    }

    public static Specification<AliasEntity> equalsSequenceNumber(Integer sequenceNumber) {
        return (root, query, builder) -> builder.equal(root.get(AliasEntity_.sequenceNumber), sequenceNumber);
    }

    public static Join<AliasEntity, PartyEntity> joinParty(From<?, AliasEntity> from) {
        return from.join(AliasEntity_.party);
    }
}
