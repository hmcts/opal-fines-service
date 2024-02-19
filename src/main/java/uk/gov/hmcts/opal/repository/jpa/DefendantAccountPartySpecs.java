package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;

public class DefendantAccountPartySpecs extends EntitySpecs<DefendantAccountPartiesEntity> {

    public static Join<DefendantAccountPartiesEntity, DefendantAccountEntity> joinDefendantAccountOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntity> defendants,
                                                CriteriaBuilder builder, String assocType) {
        return onAssociationType(builder, defendants, assocType).join(DefendantAccountPartiesEntity_.defendantAccount);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinPartyOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntity> parties, CriteriaBuilder builder, String assocType) {
        return onAssociationType(builder, parties, assocType).join(DefendantAccountPartiesEntity_.party);
    }

    public static <Z> ListJoin<Z, DefendantAccountPartiesEntity> onAssociationType(
        CriteriaBuilder builder, ListJoin<Z, DefendantAccountPartiesEntity> parties, String assocType) {
        return parties.on(builder.equal(parties.get(DefendantAccountPartiesEntity_.associationType), assocType));
    }
}
