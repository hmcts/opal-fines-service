package uk.gov.hmcts.opal.disco.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authorisation.aspect.AuthorizedAnyBusinessUnitUserHasPermission;
import uk.gov.hmcts.opal.authorisation.model.Permissions;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.disco.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.disco.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.disco.opal.DefendantAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DefendantAccountServiceProxy")
@Qualifier("defendantAccountServiceProxy")
public class DefendantAccountServiceProxy implements DefendantAccountServiceInterface, ProxyInterface {

    private final DefendantAccountService opalDefendantAccountService;
    private final LegacyDefendantAccountService legacyDefendantAccountService;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDefendantAccountService : opalDefendantAccountService;
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
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts: isLegacyMode: {}", isLegacyMode(dynamicConfigService));
        return getCurrentModeService().searchDefendantAccounts(accountSearchDto);
    }

    @Override
    @AuthorizedAnyBusinessUnitUserHasPermission(Permissions.ACCOUNT_ENQUIRY)
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        return getCurrentModeService().getAccountDetailsByDefendantAccountId(defendantAccountId);
    }
}
