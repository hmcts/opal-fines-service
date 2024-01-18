package uk.gov.hmcts.opal.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.service.legacy.dto.DefendantAccountsSearchResults;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class LegacyDefendantAccountService extends LegacyService implements DefendantAccountServiceInterface {

    public static final String GET_DEFENDANT_ACCOUNT_BY_ID = "getDefendantAccountById";
    public static final String PUT_DEFENDANT_ACCOUNT = "putDefendantAccount";
    public static final String GET_DEFENDANT_ACCOUNT = "getDefendantAccount";
    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";

    @Autowired
    protected LegacyDefendantAccountService(@Value("${legacy-gateway-url}") String gatewayUrl,
                                            RestTemplate restTemplate) {
        super(gatewayUrl, restTemplate);
    }

    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        log.info("Get defendant account for {} from {}", request.toJson(), gatewayUrl);
        return getFromGateway(GET_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, request);
    }

    @Override
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        log.info("Sending defendantAccount to {}", gatewayUrl);
        return postToGateway(PUT_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return Collections.emptyList();
    }

    @Override
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        DefendantAccountSearchCriteria criteria = DefendantAccountSearchCriteria.fromAccountSearchDto(accountSearchDto);
        log.info(":searchDefendantAccounts: criteria: {} via gateway {}", criteria.toJson(), gatewayUrl);
        return postToGateway(SEARCH_DEFENDANT_ACCOUNTS, DefendantAccountsSearchResults.class, criteria)
            .toAccountSearchResultsDto();
    }

    @Override
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        log.info("Get defendant account for id: {}", defendantAccountId);
        return getFromGateway(GET_DEFENDANT_ACCOUNT_BY_ID, AccountDetailsDto.class, defendantAccountId);
    }

}
