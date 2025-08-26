package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

import java.math.BigDecimal;
import java.util.Optional;

import static uk.gov.hmcts.opal.disco.legacy.LegacyDiscoDefendantAccountService.SEARCH_DEFENDANT_ACCOUNTS;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService implements DefendantAccountServiceInterface {

    public static final String GET_HEADER_SUMMARY = "LIBRA.get_header_summary";

    private final GatewayService gatewayService;
    private final LegacyGatewayProperties legacyGatewayProperties;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: id: {}", defendantAccountId);

        try {

            Response<LegacyGetDefendantAccountHeaderSummaryResponse> response = gatewayService.postToGateway(
                GET_HEADER_SUMMARY, LegacyGetDefendantAccountHeaderSummaryResponse.class,
                createGetDefendantAccountRequest(defendantAccountId.toString()), null);

            if (response.isError()) {
                log.error(":getHeaderSummary: Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(":getHeaderSummary:", response.exception);
                } else if (response.isLegacyFailure()) {
                    log.error(":getHeaderSummary: Legacy Gateway: body: \n{}", response.body);
                    LegacyGetDefendantAccountHeaderSummaryResponse responseEntity = response.responseEntity;
                    log.error(":getHeaderSummary: Legacy Gateway: entity: \n{}", responseEntity.toXml());
                }
            } else if (response.isSuccessful()) {
                log.info(":getHeaderSummary: Legacy Gateway response: Success.");
            }

            return toHeaderSumaryDto(response.responseEntity);

        } catch (RuntimeException e) {
            log.error(":getHeaderSummary: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getHeaderSummary:", e);
            throw e;
        }
    }

    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        LegacyDefendantAccountSearchCriteria criteria =
            LegacyDefendantAccountSearchCriteria.fromAccountSearchDto(accountSearchDto);
        log.debug(":searchDefendantAccounts: criteria: {} via gateway {}", criteria.toJson(),
                  legacyGatewayProperties.getUrl());
        Response<LegacyDefendantAccountsSearchResults> response = gatewayService.postToGateway(
            SEARCH_DEFENDANT_ACCOUNTS, LegacyDefendantAccountsSearchResults.class, criteria, null);

        return response.responseEntity.toDefendantAccountSearchResultsDto();

    }

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    public static LegacyGetDefendantAccountRequest createGetDefendantAccountRequest(String defendantAccountId) {
        return LegacyGetDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    private DefendantAccountHeaderSummary toHeaderSumaryDto(LegacyGetDefendantAccountHeaderSummaryResponse response) {
        return DefendantAccountHeaderSummary.builder()
            .defendantAccountId(response.getDefendantAccountId())
            .version(response.getVersion())
            .accountNumber(response.getAccountNumber())
            .hasParentGuardian(!Optional.ofNullable(response.getParentGuardianPartyId())
                .map(String::isBlank).orElse(true))  // TODO - is this the correct way?
            .debtorType(response.getDefendantDetails().getDebtorType())
            .organisation(response.getDefendantDetails().getOrganisationFlag())
            .accountStatusDisplayName(response.getAccountStatusReference().getAccountStatusDisplayName())
            .accountType(response.getAccountType())
            .prosecutorCaseReference(response.getProsecutorCaseReference())
            .fixedPenaltyTicketNumber(response.getFixedPenaltyTicketNumber())
            .businessUnitName(response.getBusinessUnitSummary().getBusinessUnitName())
            .businessUnitId(response.getBusinessUnitSummary().getBusinessUnitId())
            .businessUnitCode(response.getBusinessUnitSummary().getBusinessUnitCode())
            .imposed(toBigDecimal(response.getPaymentStateSummary().getImposedAmount()))
            .arrears(toBigDecimal(response.getPaymentStateSummary().getArrearsAmount()))
            .paid(toBigDecimal(response.getPaymentStateSummary().getPaidAmount()))
            .writtenOff(BigDecimal.ZERO) // TODO - how do we derive written off?
            .accountBalance(toBigDecimal(response.getPaymentStateSummary().getAccountBalance()))
            .organisationName(response.getDefendantDetails().getOrganisationDetails().getOrganisationName())
            .isYouth(response.getDefendantDetails().getIsYouthFlag())
            .title(response.getDefendantDetails().getIndividualDetails().getTitle())
            .firstnames(response.getDefendantDetails().getIndividualDetails().getFirstNames())
            .surname(response.getDefendantDetails().getIndividualDetails().getSurname())
            .build();
    }

    private BigDecimal toBigDecimal(String candidate) {
        return Optional.ofNullable(candidate).filter(s -> s.length() > 1).map(BigDecimal::new).orElse(BigDecimal.ZERO);
    }
}
