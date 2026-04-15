package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPartyRequest;

public interface DefendantAccountPartyServiceInterface {

    GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);

    GetDefendantAccountPartyResponse addDefendantAccountParty(Long defendantAccountId,
                                                                String businessUnitId,
                                                                String businessUserId,
                                                                String postedBy,
                                                                String ifMatch,
                                                                AddDefendantAccountPartyRequest request);

    GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           DefendantAccountParty defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String businessUserId);

    RemoveDefendantAccountPartyResponse removeDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        Short businessUnitId,
        String businessUserId,
        String ifMatch,
        String postedBy,
        DefendantAccountParty defendantAccountParty);
}
