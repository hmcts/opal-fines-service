package uk.gov.hmcts.opal.service.proxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.DynamicConfigService;
import uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountService;
import uk.gov.hmcts.opal.service.opal.DefendantAccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DefendantAccountServiceProxy")
@Qualifier("defendantAccountServiceProxy")
public class DefendantAccountServiceProxy implements DefendantAccountServiceInterface, ProxyInterface {

    private final DefendantAccountService opalDefendantAccountService;
    private final LegacyDefendantAccountService legacyDefendantAccountService;
    private final DynamicConfigService dynamicConfigService;

    private DefendantAccountServiceInterface getCurrentModeService() {
        return isLegacyMode(dynamicConfigService) ? legacyDefendantAccountService : opalDefendantAccountService;
    }

    @Override
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        return getCurrentModeService().getDefendantAccount(request);
    }

    @Override
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        return getCurrentModeService().putDefendantAccount(defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return getCurrentModeService().getDefendantAccountsByBusinessUnit(businessUnitId);
    }

    @Override
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.info(":searchDefendantAccounts: isLegacyMode: {}", isLegacyMode(dynamicConfigService));
        return getCurrentModeService().searchDefendantAccounts(accountSearchDto);
    }

    @Override
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        return getCurrentModeService().getAccountDetailsByDefendantAccountId(defendantAccountId);
    }
}
