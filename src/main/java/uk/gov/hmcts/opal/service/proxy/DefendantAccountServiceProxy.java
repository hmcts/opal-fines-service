package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.response.GetHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.UserStateService;


@Service
@Slf4j(topic = "opal.DefendantAccountServiceProxy")
@RequiredArgsConstructor
public class DefendantAccountServiceProxy implements DefendantAccountServiceInterface, ProxyInterface {

    private final OpalDefendantAccountService draftAccountPromotion;
    private final LegacyDefendantAccountService legacyDraftAccountPromotion;
    private final DynamicConfigService dynamicConfigService;

    private final UserStateService userStateService;

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

    public GetHeaderSummaryResponse getHeaderSummaryWithVersion(Long defendantAccountId, String authHeader) {
        if (isLegacyMode(dynamicConfigService)) {
            return legacyDraftAccountPromotion.getHeaderSummaryWithVersion(defendantAccountId, authHeader);
        } else {
            return draftAccountPromotion.getHeaderSummaryWithVersion(defendantAccountId, authHeader);
        }
    }
}