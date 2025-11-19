package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);


    GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId);

    GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId);

    DefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                    String businessUnitId,
                                                    UpdateDefendantAccountRequest request,String ifMatch,
                                                    String postedBy);
                                                   

    AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId,
        String ifMatch, String authHeader);

    AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId, String businessUnitUserId,
        String ifMatch, String authHeader, AddDefendantAccountEnforcementRequest request);


    GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
                                           Long defendantAccountPartyId,
                                           DefendantAccountParty defendantAccountParty,
                                           String ifMatch,
                                           String businessUnitId,
                                           String postedBy,
                                           String businessUserId);

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);


    GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
                                            String businessUnitId,
                                            String ifMatch,
                                            String authHeader,
                                            AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest);
}
