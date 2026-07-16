package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;

@Service
@Slf4j(topic = "opal.DefendantAccountServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountServiceProxy implements DefendantAccountServiceInterface, ProxyInterface {

    private final OpalDefendantAccountService draftAccountPromotion;
    private final LegacyDefendantAccountService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDraftAccountPromotion : draftAccountPromotion;
    }

    @Override
    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        return getCurrentModeService().getHeaderSummary(defendantAccountId);
    }

    @Override
    public GetDefendantAccountConsolidatedAccountsResult getConsolidatedAccounts(Long defendantAccountId) {
        return draftAccountPromotion.getConsolidatedAccounts(defendantAccountId);
    }

    @Override
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        return getCurrentModeService().getHistory(defendantAccountId, filter);
    }

    @Override
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        return getCurrentModeService().searchDefendantAccounts(accountSearchDto);
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        return getCurrentModeService().getPaymentTerms(defendantAccountId);
    }

    @Override
    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        return getCurrentModeService().getAtAGlance(defendantAccountId);
    }

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        return getCurrentModeService().getDefendantAccountFixedPenalty(defendantAccountId);
    }

    @Override
    public UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                           String businessUnitId,
                                                           UpdateDefendantAccountRequest request, String postedBy,
                                                           String postedByName) {
        return getCurrentModeService().updateDefendantAccount(defendantAccountId, businessUnitId, request, postedBy,
                                                              postedByName);
    }

    @Override
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {
        return getCurrentModeService().getEnforcementStatus(defendantAccountId);
    }

    @Override
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch) {
        return getCurrentModeService().addPaymentCardRequest(defendantAccountId, businessUnitId,
            businessUnitUserId, postedByName, ifMatch);
    }

    @Override
    public AddEnforcementResponse addEnforcement(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddDefendantAccountEnforcementRequest request) {
        return getCurrentModeService().addEnforcement(defendantAccountId, businessUnitId, businessUnitUserId,
            ifMatch, request);
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        return getCurrentModeService().addPaymentTerms(defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            postedByName,
            ifMatch,
            addPaymentTermsRequest);
    }
}
