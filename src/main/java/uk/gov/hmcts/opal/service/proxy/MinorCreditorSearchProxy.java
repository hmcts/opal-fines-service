package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.UpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.iface.MinorCreditorAccountServiceInterface;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.service.legacy.LegacyMinorCreditorService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalMinorCreditorService;

@Service
@Slf4j(topic = "opal.MinorCreditorSearchProxy")
@RequiredArgsConstructor
public class MinorCreditorSearchProxy implements MinorCreditorServiceInterface, ProxyInterface,
    MinorCreditorAccountServiceInterface {

    private final OpalMinorCreditorService opalMinorCreditorService;
    private final LegacyMinorCreditorService legacyMinorCreditorService;
    private final DynamicConfigService dynamicConfigService;

    private MinorCreditorServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyMinorCreditorService : opalMinorCreditorService;
    }

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch criteria) {
        return getCurrentModeService().searchMinorCreditors(criteria);
    }

    public GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long minorCreditorAccountId) {
        log.debug(":getHeaderSummary: minorCreditorAccountId={}", minorCreditorAccountId);
        return getCurrentModeService().getHeaderSummary(minorCreditorAccountId);
    }

    @Override
    public MinorCreditorAccountResponse updateMinorCreditorAccount(Long minorCreditorAccountId,
                                                                   UpdateMinorCreditorAccountRequest request,
                                                                   String ifMatch,
                                                                   String postedBy) {
        if (getCurrentModeService() instanceof MinorCreditorAccountServiceInterface accountService) {
            return accountService.updateMinorCreditorAccount(minorCreditorAccountId, request, ifMatch, postedBy);
        }

        throw new UnsupportedOperationException(
            "Legacy mode not implemented for PATCH /minor-creditor-accounts/{id}");
    }
}
