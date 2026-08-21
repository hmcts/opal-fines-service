package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.generated.model.PartyResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.AddPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PartyDefendantAccount;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;

public interface DefendantAccountPartyServiceInterface {

    PartyResponseDefendantAccount getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);

    PartyResponseDefendantAccount addDefendantAccountParty(Long defendantAccountId,
                                                                String businessUnitId,
                                                                String businessUserId,
                                                                String postedBy,
                                                                String postedByName,
                                                                String ifMatch,
                                                                AddPartyRequestDefendantAccount request);

    default PartyResponseDefendantAccount addDefendantAccountParty(Long defendantAccountId,
                                                                String businessUnitId,
                                                                String businessUserId,
                                                                String postedBy,
                                                                String ifMatch,
                                                                AddPartyRequestDefendantAccount request) {
        return addDefendantAccountParty(defendantAccountId, businessUnitId, businessUserId, postedBy, postedBy,
                                        ifMatch, request);
    }

    PartyResponseDefendantAccount replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           PartyDefendantAccount defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String postedByName,
                                           String businessUserId);

    default PartyResponseDefendantAccount replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           PartyDefendantAccount defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String businessUserId) {
        return replaceDefendantAccountParty(defendantAccountId, defendantAccountPartyId, defendantAccountParty,
                                            ifMatch, businessUnitId, postedBy, postedBy, businessUserId);
    }

    RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId, String postedBy,
        String postedByName, String ifMatch, RemoveDefendantAccountPartyRequest request);

    default RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId, String postedBy,
        String ifMatch, RemoveDefendantAccountPartyRequest request) {
        return removeDefendantAccountParty(defendantAccountId, defendantAccountPartyId, businessUnitId,
                                           businessUserId, postedBy, postedBy, ifMatch, request);
    }
}
