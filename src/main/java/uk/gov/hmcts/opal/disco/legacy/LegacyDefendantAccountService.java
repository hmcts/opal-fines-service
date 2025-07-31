package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsResponseDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.disco.DefendantAccountServiceInterface;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService extends LegacyService implements DefendantAccountServiceInterface {

    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";
    public static final String PUT_DEFENDANT_ACCOUNT = "putDefendantAccount";
    public static final String GET_DEFENDANT_ACCOUNT = "getDefendantAccount";
    public static final String GET_ACCOUNT_DETAILS = "getAccountDetails";

    public LegacyDefendantAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }


    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        log.debug("Get defendant account for {} from {}", request.toJson(), legacyGateway.getUrl());
        return postToGateway(GET_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, request);
    }

    @Override
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        log.debug("Sending defendantAccount to {}", legacyGateway.getUrl());
        return postToGateway(PUT_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return Collections.emptyList();
    }

    @Override
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        DefendantAccountSearchCriteria criteria = DefendantAccountSearchCriteria.fromAccountSearchDto(accountSearchDto);
        log.debug(":searchDefendantAccounts: criteria: {} via gateway {}", criteria.toJson(), legacyGateway.getUrl());
        return postToGateway(SEARCH_DEFENDANT_ACCOUNTS, DefendantAccountsSearchResults.class, criteria)
            .toAccountSearchResultsDto();
    }

    @Override
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        log.debug("Get defendant account for id: {}", defendantAccountId);

        LegacyAccountDetailsRequestDto request = LegacyAccountDetailsRequestDto.builder()
            .defendantAccountId(defendantAccountId)
            .build();

        LegacyAccountDetailsResponseDto response = postToGateway(
            GET_ACCOUNT_DETAILS,
            LegacyAccountDetailsResponseDto.class,
            request
        );

        return LegacyAccountDetailsResponseDto.toAccountDetailsDto(response);
    }

}
