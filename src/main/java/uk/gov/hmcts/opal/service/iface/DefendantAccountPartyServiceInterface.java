package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyRequestDefendantAccount;
import uk.gov.hmcts.opal.generated.model.RemoveDefendantAccountPartyResponseDefendantAccount;

public interface DefendantAccountPartyServiceInterface {

    GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);

    GetDefendantAccountPartyResponse addDefendantAccountParty(Long defendantAccountId,
                                                                String businessUnitId,
                                                                String businessUserId,
                                                                String postedBy,
                                                                String postedByName,
                                                                String ifMatch,
                                                                AddDefendantAccountPartyRequest request);

    default GetDefendantAccountPartyResponse addDefendantAccountParty(Long defendantAccountId,
                                                                String businessUnitId,
                                                                String businessUserId,
                                                                String postedBy,
                                                                String ifMatch,
                                                                AddDefendantAccountPartyRequest request) {
        return addDefendantAccountParty(defendantAccountId, businessUnitId, businessUserId, postedBy, postedBy,
                                        ifMatch, request);
    }

    GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           DefendantAccountParty defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String postedByName,
                                           String businessUserId);

    default GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           DefendantAccountParty defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String businessUserId) {
        return replaceDefendantAccountParty(defendantAccountId, defendantAccountPartyId, defendantAccountParty,
                                            ifMatch, businessUnitId, postedBy, postedBy, businessUserId);
    }

    RemoveDefendantAccountPartyResponseDefendantAccount removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId, String postedBy,
        String postedByName, String ifMatch, RemoveDefendantAccountPartyRequestDefendantAccount request);

    default RemoveDefendantAccountPartyResponseDefendantAccount removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId, Short businessUnitId, String businessUserId, String postedBy,
        String ifMatch, RemoveDefendantAccountPartyRequestDefendantAccount request) {
        return removeDefendantAccountParty(defendantAccountId, defendantAccountPartyId, businessUnitId,
                                           businessUserId, postedBy, postedBy, ifMatch, request);
    }
}
