package uk.gov.hmcts.opal.repository.jpa;

import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.equalNormalized;
import static uk.gov.hmcts.opal.repository.jpa.SpecificationUtils.likeStartsWithNormalized;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.AliasEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartyEntity_;

/**
 * Specifications for AliasEntity queries.
 */
@Component
public class AliasSpecs {

    /**
     * Returns all AliasEntity records for a given defendant account ID.
     * Replaces the old @Query-based implementation with a type-safe Specification.
     */
    public static Specification<AliasEntity> byDefendantAccountId(Long defendantAccountId) {
        return (root, query, cb) -> {
            query.distinct(true);
            query.orderBy(cb.asc(root.get(AliasEntity_.sequenceNumber)));

            // AliasEntity → PartyEntity → DefendantAccountPartiesEntity → DefendantAccountEntity
            Join<AliasEntity, PartyEntity> partyJoin = root.join(AliasEntity_.party);
            Join<PartyEntity, DefendantAccountPartiesEntity> dapJoin =
                partyJoin.join(PartyEntity_.defendantAccountParties);
            Join<DefendantAccountPartiesEntity, DefendantAccountEntity> daJoin =
                dapJoin.join(DefendantAccountPartiesEntity_.defendantAccount);

            return cb.equal(daJoin.get(DefendantAccountEntity_.defendantAccountId), defendantAccountId);
        };
    }

    public static Predicate aliasPartyPredicate(From<?, AliasEntity> alias, From<?, PartyEntity> party,
        CriteriaBuilder builder) {

        return builder.equal(
            alias.get(AliasEntity_.party).get(PartyEntity_.partyId),
            party.get(PartyEntity_.partyId));
    }

    public static Predicate surnamePredicate(From<?, AliasEntity> alias, CriteriaBuilder builder, String surname,
        Boolean exactMatch) {

        return namePredicate(builder, alias.get(AliasEntity_.surname), surname, exactMatch);
    }

    public static Predicate forenamesPredicate(From<?, AliasEntity> alias, CriteriaBuilder builder, String forenames,
        Boolean exactMatch) {

        return namePredicate(builder, alias.get(AliasEntity_.forenames), forenames, exactMatch);
    }

    public static Predicate organisationNamePredicate(From<?, AliasEntity> alias, CriteriaBuilder builder,
        String organisationName, Boolean exactMatch) {

        return namePredicate(builder, alias.get(AliasEntity_.organisationName), organisationName, exactMatch);
    }

    private static Predicate namePredicate(CriteriaBuilder builder, Path<String> path, String value,
        Boolean exactMatch) {

        return Boolean.TRUE.equals(exactMatch)
            ? equalNormalized(builder, path, value)
            : likeStartsWithNormalized(builder, path, value);
    }
}
