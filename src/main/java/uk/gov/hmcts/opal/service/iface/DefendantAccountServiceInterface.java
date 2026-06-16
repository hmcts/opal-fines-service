package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);

    default GetDefendantAccountConsolidatedAccountsResult getConsolidatedAccounts(Long defendantAccountId) {
        throw new UnsupportedOperationException("GetDefendantAccountConsolidatedAccounts is only supported in OPAL");
    }

    DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);


    GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId);

    GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId);

    UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                    String businessUnitId,
                                                    UpdateDefendantAccountRequest request,
                                                    String postedBy,
                                                    String postedByName);

    default UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                    String businessUnitId,
                                                    UpdateDefendantAccountRequest request,
                                                    String postedBy) {
        return updateDefendantAccount(defendantAccountId, businessUnitId, request, postedBy, postedBy);
    }

    AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId,
        String postedByName, String ifMatch);

    AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId, String businessUnitUserId,
        String ifMatch, AddDefendantAccountEnforcementRequest request);


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

    EnforcementStatus getEnforcementStatus(Long defendantAccountId);


    GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
                                            String businessUnitId,
                                            String businessUnitUserId,
                                            String postedByName,
                                            String ifMatch,
                                            AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest);
}
