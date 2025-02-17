package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ListJoin;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityCore;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntityCore_;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountPartiesEntity_;
import uk.gov.hmcts.opal.entity.PartyEntity;

public class DefendantAccountPartyCoreSpecs extends EntitySpecs<DefendantAccountPartiesEntityCore> {

    public static Join<DefendantAccountPartiesEntityCore, DefendantAccountCore> joinDefendantAccountOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntityCore> defendants,
                                                CriteriaBuilder builder, String assocType) {
        return onAssociationType(builder, defendants, assocType)
            .join(DefendantAccountPartiesEntityCore_.defendantAccount);
    }

    public static Join<DefendantAccountPartiesEntityCore, PartyEntity> joinPartyOnAssociationType(
        ListJoin<?, DefendantAccountPartiesEntityCore> parties, CriteriaBuilder builder, String assocType) {
        return onAssociationType(builder, parties, assocType).join(DefendantAccountPartiesEntityCore_.party);
    }

    public static <Z> ListJoin<Z, DefendantAccountPartiesEntityCore> onAssociationType(
        CriteriaBuilder builder, ListJoin<Z, DefendantAccountPartiesEntityCore> parties, String assocType) {
        return parties.on(builder.equal(parties.get(DefendantAccountPartiesEntity_.associationType), assocType));
    }
}
