package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.AliasEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity_;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity_;
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
}
