package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService implements DefendantAccountServiceInterface {

    public static final String GET_HEADER_SUMMARY = "LIBRA.get_header_summary";
    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";

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

    private DefendantAccountHeaderSummary toHeaderSumaryDto(
        LegacyGetDefendantAccountHeaderSummaryResponse response) {

        // ----- Legacy -> Opal Party Details -----
        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails legacyParty = response.getPartyDetails();
        PartyDetails opalPartyDetails = null;

        if (legacyParty != null) {
            uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails legacyOrg = legacyParty.getOrganisationDetails();
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails legacyInd = legacyParty.getIndividualDetails();

            // organisation aliases
            java.util.List<OrganisationAlias> orgAliases = null;
            if (legacyOrg != null && legacyOrg.getOrganisationAliases() != null) {
                orgAliases = java.util.Arrays.stream(legacyOrg.getOrganisationAliases())
                    .map(a -> OrganisationAlias.builder()
                        .aliasId(a.getAliasId())
                        .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                        .organisationName(a.getOrganisationName())
                        .build())
                    .collect(java.util.stream.Collectors.toList());
            }

            OrganisationDetails opalOrg = legacyOrg == null ? null
                : OrganisationDetails.builder()
                    .organisationName(legacyOrg.getOrganisationName())
                    .organisationAliases(orgAliases)
                    .build();

            // individual aliases
            java.util.List<IndividualAlias> indAliases = null;
            if (legacyInd != null && legacyInd.getIndividualAliases() != null) {
                indAliases = java.util.Arrays.stream(legacyInd.getIndividualAliases())
                    .map(a -> IndividualAlias.builder()
                        .aliasId(a.getAliasId())
                        .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                        .surname(a.getSurname())
                        .forenames(a.getForenames())
                        .build())
                    .collect(java.util.stream.Collectors.toList());
            }

            IndividualDetails opalInd = legacyInd == null ? null
                : IndividualDetails.builder()
                    .title(legacyInd.getTitle())
                    .forenames(legacyInd.getFirstNames()) // legacy accessor is getFirstNames()
                    .surname(legacyInd.getSurname())
                    .dateOfBirth(legacyInd.getDateOfBirth() != null ? legacyInd.getDateOfBirth().toString() : null)
                    .age(legacyInd.getAge())
                    .nationalInsuranceNumber(legacyInd.getNationalInsuranceNumber())
                    .individualAliases(indAliases) // keep key present (can be empty list)
                    .build();

            opalPartyDetails = PartyDetails.builder()
                .partyId(legacyParty.getDefendantAccountPartyId())
                .organisationFlag(legacyParty.getOrganisationFlag())
                .organisationDetails(opalOrg)
                .individualDetails(opalInd)
                .build();
        }

        // ----- Business Unit -----
        BusinessUnitSummary bu = response.getBusinessUnitSummary() == null ? null
            : BusinessUnitSummary.builder()
                .businessUnitId(response.getBusinessUnitSummary().getBusinessUnitId())
                .businessUnitName(response.getBusinessUnitSummary().getBusinessUnitName())
                .welshSpeaking("N") // default; legacy schema doesn’t provide it
                .build();

        // ----- Account Status -----
        AccountStatusReference status = response.getAccountStatusReference() == null ? null
            : AccountStatusReference.builder()
                .accountStatusCode(response.getAccountStatusReference().getAccountStatusCode())
                .accountStatusDisplayName(response.getAccountStatusReference().getAccountStatusDisplayName())
                .build();

        // ----- Payment State Summary (never null numbers) -----
        PaymentStateSummary pay = response.getPaymentStateSummary() == null ? null
            : PaymentStateSummary.builder()
                .imposedAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getImposedAmount()))
                .arrearsAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getArrearsAmount()))
                .paidAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getPaidAmount()))
                .accountBalance(toBigDecimalOrZero(response.getPaymentStateSummary().getAccountBalance()))
                .build();

        // ----- Build Opal DTO (note: NO defendant_account_id in Opal body) -----
        return DefendantAccountHeaderSummary.builder()
            .accountNumber(response.getAccountNumber())
            .defendantPartyId(response.getDefendantPartyId())
            .parentGuardianPartyId(response.getParentGuardianPartyId())
            .accountStatusReference(status)
            .accountType(response.getAccountType())
            .prosecutorCaseReference(response.getProsecutorCaseReference())
            .fixedPenaltyTicketNumber(response.getFixedPenaltyTicketNumber())
            .businessUnitSummary(bu)
            .paymentStateSummary(pay)
            .partyDetails(opalPartyDetails)
            .build();
    }

    private static BigDecimal toBigDecimalOrZero(Object input) {
        if (input == null) {
            return BigDecimal.ZERO;
        }
        if (input instanceof BigDecimal) {
            return (BigDecimal) input;
        }
        if (input instanceof CharSequence) {
            String s = input.toString().trim();
            if (s.isEmpty()) {
                return BigDecimal.ZERO;
            }
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                log.warn(":toBigDecimalOrZero: Invalid number format for input '{}'. Defaulting to ZERO.", s, e);
                return BigDecimal.ZERO;
            }
        }
        if (input instanceof Number) {
            return BigDecimal.valueOf(((Number) input).doubleValue());
        }
        log.warn(":toBigDecimalOrZero: Unsupported type {}. Defaulting to ZERO.", input.getClass().getName());
        return BigDecimal.ZERO;
    }

    @Override
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
                                                                     Long defendantAccountPartyId) {
        throw new UnsupportedOperationException("getDefendantAccountParty is not implemented in"
            + " LegacyDefendantAccountService");
    }
}