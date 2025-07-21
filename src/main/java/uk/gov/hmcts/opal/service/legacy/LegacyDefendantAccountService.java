package uk.gov.hmcts.opal.service.legacy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountDto;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsRequestDto;
import uk.gov.hmcts.opal.dto.legacy.LegacyAccountDetailsResponseDto;
import uk.gov.hmcts.opal.dto.legacy.PartyDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.opal.UserStateService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService extends LegacyService implements DefendantAccountServiceInterface {

    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";
    public static final String PUT_DEFENDANT_ACCOUNT = "putDefendantAccount";
    public static final String GET_DEFENDANT_ACCOUNT = "getDefendantAccount";
    public static final String GET_ACCOUNT_DETAILS = "getAccountDetails";
    private final UserStateService userStateService;

    public LegacyDefendantAccountService(LegacyGatewayProperties legacyGatewayProperties,
                                         RestClient restClient,UserStateService userStateService) {
        super(legacyGatewayProperties, restClient);
        this.userStateService = userStateService;
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
        DefendantAccountDto request = DefendantAccountDto.fromEntity(defendantAccountEntity);
        DefendantAccountDto dto = postToGateway(PUT_DEFENDANT_ACCOUNT, DefendantAccountDto.class, request);
        return dto.toEntity();
    }


    @Override
    public List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId) {
        return Collections.emptyList();
    }

    @Override
    public AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {

        UserState userState = userStateService.getUserStateUsingAuthToken(accountSearchDto.getAuthHeader());

        if (!userState.hasPermissionToSearchDefendantAccounts()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"User not permitted to search defendant accounts "
                + "in legacy mode");
        }

        short businessUnitId = accountSearchDto.getNumericCourt()
            .map(Long::shortValue)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Court ID is required or invalid"));
        return postToGateway(
            "searchDefendantAccounts",
            DefendantAccountsSearchResults.class,
            accountSearchDto
        ).toAccountSearchResultsDto();
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

        if (response == null
            || response.getDefendantAccount() == null
            || !defendantAccountId.equals(response.getDefendantAccount().getDefendantAccountId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Defendant account not found for ID " + defendantAccountId);
        }

        return LegacyAccountDetailsResponseDto.toAccountDetailsDto(response);
    }

    public AccountSummaryDto toDto(DefendantAccountDto legacy) {
        PartyDto firstParty = Optional.ofNullable(legacy.getParties())
            .map(p -> p.getParty())
            .filter(pList -> !pList.isEmpty())
            .map(pList -> pList.get(0))
            .orElse(null); // safe fallback if no party data

        String name = (firstParty != null && firstParty.getOrganisationName() != null)
            ? firstParty.getOrganisationName()
            : (firstParty != null ? firstParty.getFullName() : "");

        String addressLine1 = (firstParty != null ? firstParty.getAddressLine1() : "");
        LocalDate dateOfBirth = (firstParty != null ? firstParty.getBirthDate() : null);

        return AccountSummaryDto.builder()
            .accountNumber(legacy.getAccountNumber())
            .defendantAccountId(String.valueOf(legacy.getDefendantAccountId()))
            .organisationName(firstParty.getOrganisationName())
            .defendantTitle(firstParty.getTitle())
            .defendantFirstnames(legacy.getDefendantFirstNames())
            .defendantSurname(firstParty.getSurname())
            .addressLine1(addressLine1)
            .birthDate(dateOfBirth)
            .accountBalance(legacy.getAccountBalance())
            .court(legacy.getCourt())
            .build();
    }
}
