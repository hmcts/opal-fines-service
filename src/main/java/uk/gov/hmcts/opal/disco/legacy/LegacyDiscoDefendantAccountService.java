package uk.gov.hmcts.opal.disco.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsResponseDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.disco.DiscoDefendantAccountServiceInterface;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j(topic = "opal.LegacyDiscoDefendantAccountService")
public class LegacyDiscoDefendantAccountService extends LegacyService implements DiscoDefendantAccountServiceInterface {

    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";
    public static final String PUT_DEFENDANT_ACCOUNT = "putDefendantAccount";
    public static final String GET_DEFENDANT_ACCOUNT = "getDefendantAccount";
    public static final String GET_ACCOUNT_DETAILS = "getAccountDetails";

    public LegacyDiscoDefendantAccountService(LegacyGatewayProperties legacyGatewayProperties, RestClient restClient) {
        super(legacyGatewayProperties, restClient);
    }


    @Override
    public Logger getLog() {
        return log;
    }

    @Override
    public DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request) {
        log.debug(":getDefendantAccount: Get defendant account for {} from {}", request.toJson(),
                  legacyGateway.getUrl());
        return postToGateway(GET_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, request);
    }

    @Override
    public DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity) {
        log.debug(":putDefendantAccount: Sending defendantAccount to {}", legacyGateway.getUrl());
        return postToGateway(PUT_DEFENDANT_ACCOUNT, DefendantAccountEntity.class, defendantAccountEntity);
    }

    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return Collections.emptyList();
    }

    @Override
    public AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId) {
        log.debug(":getAccountDetailsByDefendantAccountId: id: {}", defendantAccountId);

        LegacyAccountDetailsRequestDto request = LegacyAccountDetailsRequestDto.builder()
            .defendantAccountId(defendantAccountId)
            .build();
        try {

            LegacyAccountDetailsResponseDto response = postToGateway(
                GET_ACCOUNT_DETAILS,
                LegacyAccountDetailsResponseDto.class,
                request
            );

            return LegacyAccountDetailsResponseDto.toAccountDetailsDto(response);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Defendant account not found", e);
        }
    }
}
