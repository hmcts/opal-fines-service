package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;

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
