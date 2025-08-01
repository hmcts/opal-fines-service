package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizedAnyBusinessUnitUserHasPermission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.disco.DiscoDefendantAccountServiceInterface;
import uk.gov.hmcts.opal.disco.legacy.LegacyDiscoDefendantAccountService;
import uk.gov.hmcts.opal.disco.opal.DiscoDefendantAccountService;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.proxy.ProxyInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DiscoDefendantAccountServiceProxy")
@Qualifier("defendantAccountServiceProxy")
public class DiscoDefendantAccountServiceProxy implements DiscoDefendantAccountServiceInterface, ProxyInterface {

    private final DiscoDefendantAccountService opalDiscoDefendantAccountService;
    private final LegacyDiscoDefendantAccountService legacyDefendantAccountService;
    private final DynamicConfigService dynamicConfigService;

    private DiscoDefendantAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDefendantAccountService : opalDiscoDefendantAccountService;
    }

    @Override
    @AuthorizedAnyBusinessUnitUserHasPermission(Permissions.ACCOUNT_ENQUIRY)
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        return getCurrentModeService().getDefendantAccount(request);
    }

    @Override
    @AuthorizedAnyBusinessUnitUserHasPermission(Permissions.ACCOUNT_ENQUIRY)
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        return getCurrentModeService().putDefendantAccount(defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return getCurrentModeService().getDefendantAccountsByBusinessUnit(businessUnitId);
    }

    @Override
    @AuthorizedAnyBusinessUnitUserHasPermission(Permissions.ACCOUNT_ENQUIRY)
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts: isLegacyMode: {}", isLegacyMode(dynamicConfigService));
        return getCurrentModeService().searchDefendantAccounts(accountSearchDto);
    }

    @Override
    @AuthorizedAnyBusinessUnitUserHasPermission(Permissions.ACCOUNT_ENQUIRY)
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        return getCurrentModeService().getAccountDetailsByDefendantAccountId(defendantAccountId);
    }
}
