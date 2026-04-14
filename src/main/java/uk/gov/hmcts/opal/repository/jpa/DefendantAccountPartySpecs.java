package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity_;

public class DefendantAccountPartySpecs extends EntitySpecs<DefendantAccountPartiesEntity> {

    public static Join<DefendantAccountPartiesEntity, DefendantAccountEntity> joinDefendantAccountOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntity> defendants,
                                                CriteriaBuilder builder, AssociationType assocType) {
        return onAssociationType(builder, defendants, assocType).join(DefendantAccountPartiesEntity_.defendantAccount);
    }

    public static Join<DefendantAccountPartiesEntity, PartyEntity> joinPartyOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntity> parties, CriteriaBuilder builder, AssociationType assocType) {
        return onAssociationType(builder, parties, assocType).join(DefendantAccountPartiesEntity_.party);
    }

    public static <Z> ListJoin<Z, DefendantAccountPartiesEntity> onAssociationType(
        CriteriaBuilder builder, ListJoin<Z, DefendantAccountPartiesEntity> parties, AssociationType assocType) {
        return parties.on(builder.equal(parties.get(DefendantAccountPartiesEntity_.associationType), assocType));
    }
}
