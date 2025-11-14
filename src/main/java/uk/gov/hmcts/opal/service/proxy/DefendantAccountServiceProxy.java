package uk.gov.hmcts.opal.service.proxy;

import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
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
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        return getCurrentModeService().searchDefendantAccounts(accountSearchDto);
    }

    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
                                                                     Long defendantAccountPartyId) {
        return getCurrentModeService().getDefendantAccountParty(defendantAccountId, defendantAccountPartyId);
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        return getCurrentModeService().getPaymentTerms(defendantAccountId);
    }

    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        return getCurrentModeService().getAtAGlance(defendantAccountId);
    }

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        return getCurrentModeService().getDefendantAccountFixedPenalty(defendantAccountId);
    }

    @Override
    public DefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                           String businessUnitId,
                                                           UpdateDefendantAccountRequest request,
                                                           String ifMatch, String postedBy) {
        return getCurrentModeService().updateDefendantAccount(defendantAccountId, businessUnitId, request,
            ifMatch, postedBy);
    }


}
