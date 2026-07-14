package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountBuilders.toEnforcementStatusResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.legacy.config.LegacyGatewayProperties;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentTermsLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentTermsLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountHistoryLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.ResultResponsesLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.CourtReference;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.request.RemoveDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.response.RemoveDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.AccountStatusReferenceCommon.AccountStatusCodeEnum;
import uk.gov.hmcts.opal.generated.model.BusinessUnitSummaryCommon;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceEnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountEnforcementOverrideResult;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountEnforcer;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountLja;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceEnforcementStatusSummaryDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceInstalmentPeriodDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceLanguagePreferenceDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceLanguagePreferencesDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceLastEnforcementActionDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlancePaymentTermsSummaryDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlancePaymentTermsTypeDefendantAccount;
import uk.gov.hmcts.opal.generated.model.DefendantAccountAtAGlanceResponseDefendantAccount;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.AccountTypeEnum;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountHeaderSummary200Response.DebtorTypeEnum;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PaymentStateSummaryCommon;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.mapper.legacy.DefendantAccountHistoryLegacyResponseMapper;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateDefendantAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateDefendantAccountRequestMapper;
import uk.gov.hmcts.opal.repository.jpa.SpecificationUtils;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.opal.CourtService;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;
import uk.gov.hmcts.opal.service.opal.history.HistoryItemOrderingService;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService implements DefendantAccountServiceInterface {

    public static final String GET_HEADER_SUMMARY = "LIBRA.get_header_summary";
    public static final String GET_DEFENDANT_ACCOUNT_HISTORY = "LIBRA.get_defendant_account_history";
    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";
    public static final String GET_PAYMENT_TERMS = "LIBRA.get_payment_terms";
    public static final String ADD_PAYMENT_TERMS = "LIBRA.add_payment_terms";
    public static final String GET_DEFENDANT_AT_A_GLANCE = "LIBRA.getDefendantAtAGlance";
    public static final String ADD_ENFORCEMENT = "LIBRA.addEnforcement";

    public static final String GET_DEFENDANT_ACCOUNT_PARTY = "LIBRA.get_defendant_account_party";
    public static final String ADD_DEFENDANT_ACCOUNT_PARTY = "LIBRA.add_defendant_account_party";
    public static final String REPLACE_DEFENDANT_ACCOUNT_PARTY = "LIBRA.replace_defendant_account_party";
    public static final String PATCH_DEFENDANT_ACCOUNT = "LIBRA.patchDefendantAccount";
    public static final String GET_ENFORCEMENT_STATUS = "LIBRA.of_get_defendant_account_enf_status";

    public static final String ADD_PAYMENT_CARD_REQUEST = "LIBRA.of_add_defendant_account_pcr";

    private final GatewayService gatewayService;
    private final LegacyGatewayProperties legacyGatewayProperties;
    private final CourtService courtService;
    private final LocalJusticeAreaService ljaService;
    private final HistoryItemOrderingService historyItemOrderingService;

    /* ---- Mappers ---- */
    private final DefendantAccountHistoryLegacyResponseMapper legacyDefendantAccountHistoryResponseMapper;
    private final UpdateDefendantAccountRequestMapper updateDefendantAccountRequestMapper;
    private final LegacyUpdateDefendantAccountResponseMapper legacyUpdateDefendantAccountResponseMapper;

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: id: {}", defendantAccountId);

        try {

            Response<LegacyGetDefendantAccountHeaderSummaryResponse> response = gatewayService.postToGateway(
                GET_HEADER_SUMMARY, LegacyGetDefendantAccountHeaderSummaryResponse.class,
                createGetDefendantAccountRequest(defendantAccountId.toString()), null
            );

            checkResponseForError(response, "getHeaderSummary");

            return toHeaderSumaryDto(response.responseEntity);

        } catch (RuntimeException e) {
            log.error(":getHeaderSummary: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getHeaderSummary:", e);
            throw e;
        }
    }

    @Override
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        Response<GetDefendantAccountHistoryLegacyResponse> response = gatewayService.postToGateway(
            GET_DEFENDANT_ACCOUNT_HISTORY,
            GetDefendantAccountHistoryLegacyResponse.class,
            createGetDefendantAccountHistoryRequest(defendantAccountId, filter),
            null
        );

        checkResponseForError(response, "getHistory");

        DefendantAccountHistoryResponse mappedResponse =
            legacyDefendantAccountHistoryResponseMapper.toOpal(response.responseEntity);

        mappedResponse.setHistoryItems(
            mappedResponse.getHistoryItems().stream()
                .sorted(historyItemOrderingService.newestFirstDefendantHistoryComparator())
                .toList()
        );

        return mappedResponse;
    }

    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        LegacyDefendantAccountSearchCriteria criteria =
            LegacyDefendantAccountSearchCriteria.fromAccountSearchDto(accountSearchDto);
        log.debug(
            ":searchDefendantAccounts: criteria: {} via gateway {}", criteria.toJson(),
            legacyGatewayProperties.getUrl()
        );
        Response<LegacyDefendantAccountsSearchResults> response = gatewayService.postToGateway(
            SEARCH_DEFENDANT_ACCOUNTS, LegacyDefendantAccountsSearchResults.class, criteria, null);

        return response.responseEntity.toDefendantAccountSearchResultsDto();

    }

    @Override
    //TODO: Remove method, duplicated in refactored class
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        Response<LegacyGetDefendantAccountPaymentTermsResponse> response = gatewayService.postToGateway(
            GET_PAYMENT_TERMS, LegacyGetDefendantAccountPaymentTermsResponse.class,
            createGetDefendantAccountRequest(defendantAccountId.toString()), null
        );

        checkResponseForError(response, "getPaymentTerms");

        return toPaymentTermsResponse(response.responseEntity);
    }

    /* This is probably common code that will be needed across multiple Legacy requests to get
    Defendant Account details. */
    public static LegacyGetDefendantAccountRequest createGetDefendantAccountRequest(String defendantAccountId) {
        return LegacyGetDefendantAccountRequest.builder()
            .defendantAccountId(defendantAccountId)
            .build();
    }

    static GetDefendantAccountHistoryLegacyRequest createGetDefendantAccountHistoryRequest(
        Long defendantAccountId,
        DefendantAccountHistoryFilter filter
    ) {
        return GetDefendantAccountHistoryLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .fromDate(filter != null ? filter.getDateFrom() : null)
            .toDate(filter != null ? filter.getDateTo() : null)
            .itemTypes(filter == null || filter.getItemTypes() == null || filter.getItemTypes().isEmpty() ? null
                : filter.getItemTypes().stream()
                .map(LegacyDefendantAccountService::toLegacyHistoryItemType)
                .toList())
            .build();
    }

    private static String toLegacyHistoryItemType(HistoryItemType itemType) {
        return itemType == HistoryItemType.PAYMENT_TERMS ? "Payment Terms" : itemType.getResponseValue();
    }

    DefendantAccountHeaderSummary toHeaderSumaryDto(
        LegacyGetDefendantAccountHeaderSummaryResponse response) {

        var legacyParty = response.getPartyDetails();
        PartyDetailsCommon opalPartyDetails = null;

        if (legacyParty != null) {
            var legacyOrg = legacyParty.getOrganisationDetails();
            var legacyInd = legacyParty.getIndividualDetails();

            List<OrganisationAliasCommon> orgAliases = (legacyOrg != null && legacyOrg.getOrganisationAliases() != null)
                ? Arrays.stream(legacyOrg.getOrganisationAliases())
                  .filter(a -> a.getAliasId() != null && a.getOrganisationName() != null)
                  .map(a -> OrganisationAliasCommon.builder()
                            .aliasId(a.getAliasId())
                            .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                            .organisationName(a.getOrganisationName())
                            .build())
                  .collect(Collectors.toList())
                : Collections.emptyList();

            List<IndividualAliasCommon> indAliases = (legacyInd != null && legacyInd.getIndividualAliases() != null)
                ? Arrays.stream(legacyInd.getIndividualAliases())
                  .filter(a -> a.getAliasId() != null)
                  .map(a -> IndividualAliasCommon.builder()
                            .aliasId(a.getAliasId())
                            .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                            .surname(a.getSurname())
                            .forenames(a.getForenames())
                            .build())
                  .collect(Collectors.toList())
                : Collections.emptyList();

            OrganisationDetailsCommon opalOrg =
                Boolean.TRUE.equals(legacyParty.getOrganisationFlag()) && legacyOrg != null
                    ? OrganisationDetailsCommon.builder()
                      .organisationName(legacyOrg.getOrganisationName())
                      .organisationAliases(orgAliases)
                      .build()
                    : null;

            IndividualDetailsCommon opalInd =
                !Boolean.TRUE.equals(legacyParty.getOrganisationFlag()) && legacyInd != null
                    ? IndividualDetailsCommon.builder()
                      .title(legacyInd.getTitle())
                      .forenames(legacyInd.getForenames())
                      .surname(legacyInd.getSurname())
                      .dateOfBirth(legacyInd.getDateOfBirth() != null ? legacyInd.getDateOfBirth().toString() : null)
                      .age(legacyInd.getAge())
                      .nationalInsuranceNumber(legacyInd.getNationalInsuranceNumber())
                      .individualAliases(indAliases)
                      .build()
                    : null;

            opalPartyDetails = PartyDetailsCommon.builder()
                .partyId(legacyParty.getPartyId())
                .organisationFlag(legacyParty.getOrganisationFlag())
                .organisationDetails(opalOrg)
                .individualDetails(opalInd)
                .build();
        }

        BusinessUnitSummaryCommon bu = response.getBusinessUnitSummary() == null ? null
            : BusinessUnitSummaryCommon.builder()
              .businessUnitId(Short.valueOf(response.getBusinessUnitSummary().getBusinessUnitId()))
              .businessUnitName(response.getBusinessUnitSummary().getBusinessUnitName())
              .welshSpeaking("N")
              .build();

        AccountStatusReferenceCommon status = response.getAccountStatusReference() == null ? null
            : AccountStatusReferenceCommon.builder()
              .accountStatusCode(
                  AccountStatusCodeEnum.fromValue(response.getAccountStatusReference().getAccountStatusCode()))
              .accountStatusDisplayName(
                  Optional.ofNullable(response.getAccountStatusReference().getAccountStatusDisplayName())
                  .orElse(SpecificationUtils.mapAccountStatusDisplayName(
                      response.getAccountStatusReference().getAccountStatusCode()))
              )
              .build();

        // ----- Payment State Summary (never null numbers) -----
        PaymentStateSummaryCommon pay = response.getPaymentStateSummary() == null ? null
            : PaymentStateSummaryCommon.builder()
              .imposedAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getImposedAmount()))
              .arrearsAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getArrearsAmount()))
              .paidAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getPaidAmount()))
              .accountBalance(toBigDecimalOrZero(response.getPaymentStateSummary().getAccountBalance()))
              .build();

        GetDefendantAccountHeaderSummary200Response defendantAccHeaderSummaryResponse =
            GetDefendantAccountHeaderSummary200Response.builder()
                .defendantAccountId(response.getDefendantAccountId())
                .defendantAccountPartyId(response.getDefendantPartyId())
                .accountNumber(response.getAccountNumber())
                .parentGuardianPartyId(response.getParentGuardianPartyId())
                .debtorType(response.getDebtorType() == null ? DebtorTypeEnum.DEFENDANT
                    : DebtorTypeEnum.fromValue(response.getDebtorType()))
                .isYouth(Optional.ofNullable(response.getIsYouth()).orElse(Boolean.FALSE))
                .accountStatusReference(status)
                .accountType(
                    response.getAccountType() == null ? null : AccountTypeEnum.fromValue(response.getAccountType()))
                .prosecutorCaseReference(response.getProsecutorCaseReference())
                .fixedPenaltyTicketNumber(response.getFixedPenaltyTicketNumber())
                .businessUnitSummary(bu)
                .paymentStateSummary(pay)
                .partyDetails(opalPartyDetails)
                .hasConsolidatedAccounts(
                    Optional.ofNullable(response.getHasConsolidatedAccounts()).orElse(Boolean.FALSE))
                .build();

        return DefendantAccountHeaderSummary.builder()
            .version(new BigInteger(Optional.ofNullable(response.getVersion()).orElse("1")))
            .response(defendantAccHeaderSummaryResponse)
            .build();
    }


    static BigDecimal toBigDecimalOrZero(Object input) {
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

    private GetDefendantAccountPaymentTermsResponse toPaymentTermsResponse(
        LegacyGetDefendantAccountPaymentTermsResponse legacy) {

        if (legacy == null) {
            return null;
        }

        return GetDefendantAccountPaymentTermsResponse.builder()
            .version(Optional.ofNullable(legacy.getVersion())
                .map(v -> BigInteger.valueOf(v.longValue()))
                .orElse(BigInteger.ONE))
            .paymentTerms(toPaymentTerms(legacy.getPaymentTerms()))
            .paymentCardLastRequested(legacy.getPaymentCardLastRequested())
            .lastEnforcement(legacy.getLastEnforcement())
            .build();
    }

    private static PaymentTerms toPaymentTerms(LegacyPaymentTerms legacy) {
        if (legacy == null) {
            return null;
        }
        return PaymentTerms.builder()
            .daysInDefault(legacy.getDaysInDefault())
            .dateDaysInDefaultImposed(legacy.getDateDaysInDefaultImposed())
            .extension(legacy.isExtension())
            .reasonForExtension(legacy.getReasonForExtension())
            .paymentTermsType(toPaymentTermsType(legacy.getPaymentTermsType()))
            .effectiveDate(legacy.getEffectiveDate())
            .instalmentPeriod(toInstalmentPeriod(legacy.getInstalmentPeriod()))
            .lumpSumAmount(legacy.getLumpSumAmount())
            .instalmentAmount(legacy.getInstalmentAmount())
            .postedDetails(toPostedDetails(legacy.getPostedDetails()))
            .build();
    }

    static PaymentTermsType toPaymentTermsType(LegacyPaymentTermsType legacy) {
        if (legacy == null) {
            return null;
        }

        PaymentTermsType.PaymentTermsTypeCode code = null;
        if (legacy.getPaymentTermsTypeCode() != null) {
            code = PaymentTermsType.PaymentTermsTypeCode.fromValue(
                legacy.getPaymentTermsTypeCode().name()
            );
        }

        return PaymentTermsType.builder()
            .paymentTermsTypeCode(code)
            .build();
    }

    static InstalmentPeriod toInstalmentPeriod(LegacyInstalmentPeriod legacy) {
        if (legacy == null) {
            return null;
        }

        InstalmentPeriod.InstalmentPeriodCode code = null;
        if (legacy.getInstalmentPeriodCode() != null) {
            code = InstalmentPeriod.InstalmentPeriodCode.fromValue(
                legacy.getInstalmentPeriodCode().name()
            );
        }

        return InstalmentPeriod.builder()
            .instalmentPeriodCode(code)
            .build();
    }

    private static PostedDetails toPostedDetails(LegacyPostedDetails legacy) {
        if (legacy == null) {
            return null;
        }

        return PostedDetails.builder()
            .postedDate(legacy.getPostedDate())
            .postedBy(legacy.getPostedBy())
            .postedByName(legacy.getPostedByName())
            .build();
    }

    private static <T, R> R mapSafe(T obj, Function<T, R> f) {
        return obj == null ? null : f.apply(obj);
    }

    @Override
    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.info(":getAtAGlance: id: {}", defendantAccountId);

        Response<LegacyGetDefendantAccountAtAGlanceResponse> response = gatewayService.postToGateway(
            GET_DEFENDANT_AT_A_GLANCE, LegacyGetDefendantAccountAtAGlanceResponse.class,
            createGetDefendantAccountRequest(defendantAccountId.toString()), null
        );

        checkResponseForError(response, "getAtAGlance");

        return toDefendantAtAGlanceResponse(response.responseEntity);

    }

    private GetDefendantAccountAtAGlanceResponse toDefendantAtAGlanceResponse(
        LegacyGetDefendantAccountAtAGlanceResponse src) {
        if (src == null) {
            return null;
        }

        return GetDefendantAccountAtAGlanceResponse.builder()
            .payload(DefendantAccountAtAGlanceResponseDefendantAccount.builder()
                .defendantAccountId(src.getDefendantAccountId())
                .accountNumber(src.getAccountNumber())
                .debtorType(toDebtorType(src.getDebtorType()))
                .isYouth(src.isYouth())
                .partyDetails(toPartyDetails(src.getPartyDetails()))
                .address(toAddress(src.getAddress()))
                .languagePreferences(src.getLanguagePreferences() == null
                    ? JsonNullable.undefined()
                    : JsonNullable.of(toLanguagePreferences(src.getLanguagePreferences())))
                .paymentTerms(toPaymentTermsFromSummary(src.getPaymentTermsSummary()))
                .enforcementStatus(toEnforcementStatus(src.getEnforcementStatusSummary()))
                .commentsAndNotes(src.getCommentsAndNotes() == null
                    ? JsonNullable.undefined()
                    : JsonNullable.of(toComments(src.getCommentsAndNotes())))
                .build())
            .version(BigInteger.valueOf(src.getVersion()))
            .build();
    }

    private DefendantAccountAtAGlanceResponseDefendantAccount.DebtorTypeEnum toDebtorType(String debtorType) {
        if (debtorType == null) {
            return null;
        }

        return switch (debtorType) {
            case "PERSON", "Defendant" -> DefendantAccountAtAGlanceResponseDefendantAccount.DebtorTypeEnum.DEFENDANT;
            case "PARENT/GUARDIAN", "Parent/Guardian" ->
                DefendantAccountAtAGlanceResponseDefendantAccount.DebtorTypeEnum.PARENT_GUARDIAN;
            default -> DefendantAccountAtAGlanceResponseDefendantAccount.DebtorTypeEnum.fromValue(debtorType);
        };
    }

    private PartyDetailsCommon toPartyDetails(
        LegacyPartyDetails src) {
        if (src == null) {
            return null;
        }
        Boolean org = src.getOrganisationFlag();
        return PartyDetailsCommon.builder()
            .partyId(src.getPartyId())
            .organisationFlag(org)
            .organisationDetails(Boolean.TRUE.equals(org) ? toOrganisationDetails(src.getOrganisationDetails()) : null)
            .individualDetails(!Boolean.TRUE.equals(org) ? toIndividualDetails(src.getIndividualDetails()) : null)
            .build();
    }

    private OrganisationDetailsCommon toOrganisationDetails(
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails src) {
        if (src == null) {
            return null;
        }
        List<OrganisationAliasCommon> aliases = Optional
            .ofNullable(src.getOrganisationAliases())
            .map(Arrays::asList)
            .orElseGet(List::of)
            .stream()
            .map(this::toOrganisationAlias)
            .filter(Objects::nonNull)
            .toList();

        return OrganisationDetailsCommon.builder()
            .organisationName(src.getOrganisationName())
            .organisationAliases(aliases.isEmpty() ? null : aliases)
            .build();
    }

    private OrganisationAliasCommon toOrganisationAlias(
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias el) {
        if (el == null) {
            return null;
        }
        return OrganisationAliasCommon.builder()
            .aliasId(el.getAliasId())
            .sequenceNumber(el.getSequenceNumber() == null ? null : el.getSequenceNumber().intValue())
            .organisationName(el.getOrganisationName())
            .build();
    }

    private IndividualDetailsCommon toIndividualDetails(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails src) {
        if (src == null) {
            return null;
        }

        List<IndividualAliasCommon> aliases = Optional
            .ofNullable(src.getIndividualAliases())
            .map(Arrays::asList)
            .orElseGet(List::of)
            .stream()
            .map(this::toIndividualAlias)
            .filter(Objects::nonNull)
            .toList();

        return IndividualDetailsCommon.builder()
            .title(src.getTitle())
            .forenames(src.getForenames())
            .surname(src.getSurname())
            .dateOfBirth(src.getDateOfBirth() == null ? null : src.getDateOfBirth().toString())
            .age(src.getAge())
            .nationalInsuranceNumber(src.getNationalInsuranceNumber())
            .individualAliases(aliases.isEmpty() ? null : aliases)
            .build();
    }

    private IndividualAliasCommon toIndividualAlias(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias el) {
        if (el == null) {
            return null;
        }
        return IndividualAliasCommon.builder()
            .aliasId(el.getAliasId())
            .sequenceNumber(el.getSequenceNumber() == null ? null : el.getSequenceNumber().intValue())
            .surname(el.getSurname())
            .forenames(el.getForenames())
            .build();
    }

    private AddressDetailsCommon toAddress(
        AddressDetailsLegacy src) {
        if (src == null) {
            return null;
        }
        return AddressDetailsCommon.builder()
            .addressLine1(src.getAddressLine1())
            .addressLine2(src.getAddressLine2())
            .addressLine3(src.getAddressLine3())
            .addressLine4(src.getAddressLine4())
            .addressLine5(src.getAddressLine5())
            .postcode(src.getPostcode())
            .build();
    }

    private DefendantAccountAtAGlanceLanguagePreferencesDefendantAccount toLanguagePreferences(
        uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences src) {
        if (src == null) {
            return null;
        }

        String docCode = Optional.ofNullable(src.getDocumentLanguagePreference())
            .map(uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.DocumentLanguagePreference
                ::getDocumentLanguageCode)
            .orElse(null);

        String hearingCode = Optional.ofNullable(src.getHearingLanguagePreference())
            .map(uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.HearingLanguagePreference
                ::getHearingLanguageCode)
            .orElse(null);

        return DefendantAccountAtAGlanceLanguagePreferencesDefendantAccount.builder()
            .documentLanguagePreference(toLanguagePreference(docCode))
            .hearingLanguagePreference(toLanguagePreference(hearingCode))
            .build();
    }

    private DefendantAccountAtAGlanceLanguagePreferenceDefendantAccount toLanguagePreference(String code) {
        if (code == null) {
            return null;
        }

        return DefendantAccountAtAGlanceLanguagePreferenceDefendantAccount.builder()
            .languageCode(DefendantAccountAtAGlanceLanguagePreferenceDefendantAccount.LanguageCodeEnum.fromValue(code))
            .languageDisplayName(
                DefendantAccountAtAGlanceLanguagePreferenceDefendantAccount.LanguageDisplayNameEnum.fromValue(
                    "CY".equals(code) ? "Welsh and English" : "English only"))
            .build();
    }

    private DefendantAccountAtAGlancePaymentTermsSummaryDefendantAccount toPaymentTermsFromSummary(
        uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary src) {
        if (src == null) {
            return null;
        }

        String typeCode = Optional.ofNullable(src.getPaymentTermsType())
            .map(LegacyPaymentTermsType::getPaymentTermsTypeCode)
            .map(Enum::name)
            .orElse(null);

        String instalmentCode = Optional.ofNullable(src.getInstalmentPeriod())
            .map(LegacyInstalmentPeriod::getInstalmentPeriodCode)
            .map(Enum::name)
            .orElse(null);

        return DefendantAccountAtAGlancePaymentTermsSummaryDefendantAccount.builder()
            .paymentTermsType(toGeneratedPaymentTermsType(typeCode))
            .effectiveDate(JsonNullable.of(src.getEffectiveDate()))
            .instalmentPeriod(instalmentCode == null
                ? JsonNullable.undefined()
                : JsonNullable.of(toGeneratedInstalmentPeriod(instalmentCode)))
            .lumpSumAmount(JsonNullable.of(src.getLumpSumAmount()))
            .instalmentAmount(JsonNullable.of(src.getInstalmentAmount()))
            .build();
    }

    private DefendantAccountAtAGlancePaymentTermsTypeDefendantAccount toGeneratedPaymentTermsType(String code) {
        if (code == null) {
            return null;
        }

        return DefendantAccountAtAGlancePaymentTermsTypeDefendantAccount.builder()
            .paymentTermsTypeCode(
                DefendantAccountAtAGlancePaymentTermsTypeDefendantAccount.PaymentTermsTypeCodeEnum.fromValue(code))
            .paymentTermsTypeDisplayName(
                DefendantAccountAtAGlancePaymentTermsTypeDefendantAccount.PaymentTermsTypeDisplayNameEnum.fromValue(
                    switch (code) {
                        case "B" -> "By date";
                        case "P" -> "Paid";
                        case "I" -> "Instalments";
                        default -> throw new IllegalArgumentException("Invalid payment terms type code: " + code);
                    }))
            .build();
    }

    private DefendantAccountAtAGlanceInstalmentPeriodDefendantAccount toGeneratedInstalmentPeriod(String code) {
        if (code == null) {
            return null;
        }

        return DefendantAccountAtAGlanceInstalmentPeriodDefendantAccount.builder()
            .instalmentPeriodCode(
                DefendantAccountAtAGlanceInstalmentPeriodDefendantAccount.InstalmentPeriodCodeEnum.fromValue(code))
            .instalmentPeriodDisplayName(
                DefendantAccountAtAGlanceInstalmentPeriodDefendantAccount.InstalmentPeriodDisplayNameEnum.fromValue(
                    switch (code) {
                        case "W" -> "Weekly";
                        case "M" -> "Monthly";
                        case "F" -> "Fortnightly";
                        default -> throw new IllegalArgumentException("Invalid instalment period code: " + code);
                    }))
            .build();
    }

    private DefendantAccountAtAGlanceEnforcementStatusSummaryDefendantAccount toEnforcementStatus(
        uk.gov.hmcts.opal.dto.legacy.common.EnforcementStatusSummary src) {
        if (src == null) {
            return null;
        }

        return DefendantAccountAtAGlanceEnforcementStatusSummaryDefendantAccount.builder()
            .lastEnforcementAction(src.getLastEnforcementAction() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(toLastEnforcementAction(src.getLastEnforcementAction())))
            .collectionOrderMade(src.getCollectionOrderMade())
            .defaultDaysInJail(JsonNullable.of(src.getDefaultDaysInJail()))
            .enforcementOverride(src.getEnforcementOverride() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(toEnforcementOverride(src.getEnforcementOverride())))
            .lastMovementDate(JsonNullable.of(src.getLastMovementDate()))
            .build();
    }

    private DefendantAccountAtAGlanceLastEnforcementActionDefendantAccount toLastEnforcementAction(
        uk.gov.hmcts.opal.dto.common.LastEnforcementAction src) {
        if (src == null) {
            return null;
        }

        return DefendantAccountAtAGlanceLastEnforcementActionDefendantAccount.builder()
            .lastEnforcementActionId(src.getLastEnforcementActionId())
            .lastEnforcementActionTitle(JsonNullable.of(src.getLastEnforcementActionTitle()))
            .build();
    }

    private DefendantAccountAtAGlanceEnforcementOverrideDefendantAccount toEnforcementOverride(
        uk.gov.hmcts.opal.dto.common.EnforcementOverride src) {
        if (src == null) {
            return null;
        }

        return DefendantAccountAtAGlanceEnforcementOverrideDefendantAccount.builder()
            .enforcementOverrideResult(src.getEnforcementOverrideResult() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountEnforcementOverrideResult
                    .builder()
                    .enforcementOverrideResultId(src.getEnforcementOverrideResult().getEnforcementOverrideId())
                    .enforcementOverrideResultTitle(src.getEnforcementOverrideResult().getEnforcementOverrideTitle())
                    .build()))
            .enforcer(src.getEnforcer() == null ? JsonNullable.undefined()
                : JsonNullable.of(DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountEnforcer.builder()
                    .enforcerId(src.getEnforcer().getEnforcerId())
                    .enforcerName(src.getEnforcer().getEnforcerName())
                    .build()))
            .lja(src.getLja() == null ? JsonNullable.undefined()
                : JsonNullable.of(DefendantAccountAtAGlanceEnforcementOverrideDefendantAccountLja.builder()
                    .ljaId(src.getLja().getLjaId() == null ? null : src.getLja().getLjaId().longValue())
                    .ljaCode(src.getLja().getLjaCode())
                    .ljaName(src.getLja().getLjaName())
                    .build()))
            .build();
    }

    private CommentsAndNotesCommon toComments(
        uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes src) {
        if (src == null) {
            return null;
        }

        return CommentsAndNotesCommon.builder()
            .accountComment(src.getAccountComment())
            .freeTextNote1(src.getFreeTextNote1())
            .freeTextNote2(src.getFreeTextNote2())
            .freeTextNote3(src.getFreeTextNote3())
            .build();
    }

    @Override
    //TODO: Remove method, duplicated in refactored class
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        throw new UnsupportedOperationException("Legacy GetDefendantAccountFixedPenalty not implemented yet");
    }

    @Override
    public UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
        String businessUnitId,
        @NonNull UpdateDefendantAccountRequest request,
        String postedBy,
        String postedByName) {

        log.info("Legacy :updateDefendantAccount: id: {}", defendantAccountId);

        // build legacy request object with mapped fields from UpdateDefendantAccountRequest
        // pass 'version' into the mapper/to-legacy request builder
        LegacyUpdateDefendantAccountRequest legacyRequest =
            updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(request);

        // Send the request to the gateway service
        Response<LegacyUpdateDefendantAccountResponse> gwResponse = gatewayService.postToGateway(
            PATCH_DEFENDANT_ACCOUNT, LegacyUpdateDefendantAccountResponse.class,
            legacyRequest, null
        );

        checkResponseForError(gwResponse, "updateDefendantAccount");

        return legacyUpdateDefendantAccountResponseMapper.toUpdateDefendantAccountResponse(gwResponse.responseEntity);
    }

    @Override
    //TODO: Remove method, duplicated in refactored class
    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch
    ) {
        log.info(":addPaymentCardRequest (Legacy): accountId={}, bu={}", defendantAccountId, businessUnitId);

        BigInteger version = VersionUtils.extractBigInteger(ifMatch);
        AddPaymentCardLegacyRequest request = buildLegacyRequest(
            defendantAccountId, businessUnitId,
            businessUnitUserId, version.toString()
        );

        AddPaymentCardLegacyResponse response = callGateway(request);
        Long id = Long.valueOf(response.getDefendantAccountId());

        return new AddPaymentCardRequestResponse(id);
    }

    private AddPaymentCardLegacyRequest buildLegacyRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String version
    ) {
        return AddPaymentCardLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .version(version)
            .build();
    }

    private AddPaymentCardLegacyResponse callGateway(AddPaymentCardLegacyRequest request) {

        Response<AddPaymentCardLegacyResponse> gw =
            gatewayService.postToGateway(
                ADD_PAYMENT_CARD_REQUEST,
                AddPaymentCardLegacyResponse.class,
                request,
                null
            );

        if (gw.isError()) {
            handleGatewayError(gw);
        }

        if (gw.responseEntity == null) {
            throw new IllegalArgumentException("Legacy response missing");
        }

        return gw.responseEntity;
    }

    private void handleGatewayError(Response<?> gw) {

        log.error(":addPaymentCardRequest: Legacy Gateway error {}", gw.code);

        if (gw.isException()) {
            log.error(":addPaymentCardRequest: exception", gw.exception);
            throw new IllegalArgumentException("Legacy gateway exception", gw.exception);
        }

        if (gw.isLegacyFailure()) {
            log.error(":addPaymentCardRequest: legacy failure:\n{}", gw.body);
            throw new IllegalArgumentException("Legacy gateway returned failure");
        }

        throw new IllegalArgumentException("Legacy gateway error: " + gw.code);
    }

    @Override
    //TODO: Remove method, duplicated in refactored class
    public AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch, AddDefendantAccountEnforcementRequest request) {

        // build legacy request object
        AddDefendantAccountEnforcementLegacyRequest legacyRequest =
            AddDefendantAccountEnforcementLegacyRequest.builder()
                .defendantAccountId(String.valueOf(defendantAccountId))
                .businessUnitId(businessUnitId)
                .businessUnitUserId(businessUnitUserId)
                .version(VersionUtils.extractBigInteger(ifMatch).intValue())
                .resultId(request != null && request.getResultId() != null ? request.getResultId().value() : null)
                .enforcementResultResponses(
                    mapResultResponses(request != null ? request.getEnforcementResultResponses() : null))
                .paymentTerms(mapPaymentTerms(request != null ? request.getPaymentTerms() : null))
                .build();

        Response<AddDefendantAccountEnforcementLegacyResponse> response = gatewayService.postToGateway(
            ADD_ENFORCEMENT, AddDefendantAccountEnforcementLegacyResponse.class,
            legacyRequest, null);

        checkResponseForError(response, "AddEnforcement");

        AddDefendantAccountEnforcementLegacyResponse enforcementResponse = response.responseEntity;

        return AddEnforcementResponse.builder().enforcementId(enforcementResponse.getEnforcementId())
            .defendantAccountId(enforcementResponse.getDefendantAccountId()).version(enforcementResponse.getVersion())
            .build();

    }

    private List<ResultResponsesLegacy> mapResultResponses(List<ResultResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return Collections.emptyList();
        }
        return responses.stream()
            .filter(Objects::nonNull)
            .map(r -> ResultResponsesLegacy.builder()
                .parameterName(r.getParameterName())
                .response(r.getResponse())
                .build())
            .collect(Collectors.toList());
    }

    private LegacyPaymentTerms mapPaymentTerms(PaymentTerms pt) {
        if (pt == null) {
            return null;
        }

        return LegacyPaymentTerms.builder()
            .daysInDefault(pt.getDaysInDefault())
            .dateDaysInDefaultImposed(pt.getDateDaysInDefaultImposed())
            .extension(pt.isExtension())
            .reasonForExtension(pt.getReasonForExtension())
            .paymentTermsType(mapLegacyPaymentTermsType(pt.getPaymentTermsType()))
            .effectiveDate(pt.getEffectiveDate())
            .instalmentPeriod(mapLegacyInstalmentPeriod(pt.getInstalmentPeriod()))
            .lumpSumAmount(pt.getLumpSumAmount())
            .instalmentAmount(pt.getInstalmentAmount())
            .postedDetails(mapLegacyPostedDetails(pt.getPostedDetails()))
            .build();
    }

    LegacyPostedDetails mapLegacyPostedDetails(PostedDetails pd) {
        if (pd == null) {
            return null;
        }
        LegacyPostedDetails lpd = new LegacyPostedDetails();
        lpd.setPostedDate(pd.getPostedDate());
        lpd.setPostedBy(pd.getPostedBy());
        lpd.setPostedByName(pd.getPostedByName());
        return lpd;
    }

    LegacyPaymentTermsType mapLegacyPaymentTermsType(PaymentTermsType modern) {
        if (modern == null || modern.getPaymentTermsTypeCode() == null) {
            return null;
        }
        String code = modern.getPaymentTermsTypeCode().name();
        LegacyPaymentTermsType lpt = new LegacyPaymentTermsType();
        lpt.setPaymentTermsTypeCode(mapPaymentTermsTypeCodeEnum(code));
        return lpt;
    }

    LegacyInstalmentPeriod mapLegacyInstalmentPeriod(InstalmentPeriod modern) {
        if (modern == null || modern.getInstalmentPeriodCode() == null) {
            return null;
        }
        String code = modern.getInstalmentPeriodCode().name();
        LegacyInstalmentPeriod lip = new LegacyInstalmentPeriod();
        lip.setInstalmentPeriodCode(mapInstalmentPeriodCodeEnum(code));
        return lip;
    }

    LegacyPaymentTermsType.PaymentTermsTypeCode mapPaymentTermsTypeCodeEnum(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.toUpperCase()) {
            case "B" -> LegacyPaymentTermsType.PaymentTermsTypeCode.B;
            case "P" -> LegacyPaymentTermsType.PaymentTermsTypeCode.P;
            case "I" -> LegacyPaymentTermsType.PaymentTermsTypeCode.I;
            default -> throw new IllegalArgumentException("Unknown PaymentTermsType code: " + code);
        };
    }

    LegacyInstalmentPeriod.InstalmentPeriodCode mapInstalmentPeriodCodeEnum(String code) {
        if (code == null) {
            return null;
        }
        return switch (code.toUpperCase()) {
            case "W" -> LegacyInstalmentPeriod.InstalmentPeriodCode.W;
            case "M" -> LegacyInstalmentPeriod.InstalmentPeriodCode.M;
            case "F" -> LegacyInstalmentPeriod.InstalmentPeriodCode.F;
            default -> throw new IllegalArgumentException("Unknown InstalmentPeriod code: " + code);
        };
    }


    @Override
    //TODO: Remove method, duplicated in refactored class
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {
        log.debug(":getEnforcementStatus: id: {}", defendantAccountId);

        try {

            Response<LegacyGetDefendantAccountEnforcementStatusResponse> response = gatewayService.postToGateway(
                GET_ENFORCEMENT_STATUS, LegacyGetDefendantAccountEnforcementStatusResponse.class,
                createGetDefendantAccountRequest(defendantAccountId.toString()), null);

            checkResponseForError(response, "getEnforcementStatus");

            LegacyGetDefendantAccountEnforcementStatusResponse enforcementStatus = response.responseEntity;
            populateCourtCode(enforcementStatus);
            populateLjaCode(enforcementStatus);
            return toEnforcementStatusResponse(enforcementStatus);

        } catch (RuntimeException e) {
            log.error(":getEnforcementStatus: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getEnforcementStatus:", e);
            throw e;
        }
    }

    private void populateCourtCode(LegacyGetDefendantAccountEnforcementStatusResponse enforcementStatus) {
        Optional.ofNullable(enforcementStatus)
            .map(es -> es.getEnforcementOverview())
            .map(eo -> eo.getEnforcementCourt()).ifPresent(this::populateCourtCode);
    }

    private void populateCourtCode(CourtReference courtRef) {
        courtRef.setCourtCode(courtService.getCourtById(courtRef.getCourtId()).getCourtCode());
    }

    private void populateLjaCode(LegacyGetDefendantAccountEnforcementStatusResponse enforcementStatus) {
        Optional.ofNullable(enforcementStatus)
            .map(es -> es.getEnforcementOverride())
            .map(eo -> eo.getLja()).ifPresent(this::populateLjaCode);
    }

    private void populateLjaCode(LjaReference ljaRef) {
        ljaRef.setLjaCode(ljaService.getLocalJusticeAreaById(ljaRef.getLjaId()).getLjaCode());
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String postedByName,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        var legacyRequest = createAddPaymentTermsLegacyRequest(
            defendantAccountId, businessUnitId, businessUnitUserId,
            ifMatch, addPaymentTermsRequest
        );

        var response = gatewayService.postToGateway(
            ADD_PAYMENT_TERMS, AddPaymentTermsLegacyResponse.class,
            legacyRequest, null
        );

        checkResponseForError(response, "addPaymentTerms");

        return createGetDefendantAccountPaymentTermsResponse(response.responseEntity);
    }

    private AddPaymentTermsLegacyRequest createAddPaymentTermsLegacyRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        return AddPaymentTermsLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .version(VersionUtils.extractBigInteger(ifMatch))
            .paymentTerms(mapPaymentTerms(addPaymentTermsRequest != null
                ? addPaymentTermsRequest.getPaymentTerms() : null))
            .requestPaymentCard(addPaymentTermsRequest != null ? addPaymentTermsRequest.getRequestPaymentCard() : null)
            .generatePaymentTermsChangeLetter(addPaymentTermsRequest != null
                ? addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter() : null)
            .build();
    }

    private static GetDefendantAccountPaymentTermsResponse createGetDefendantAccountPaymentTermsResponse(
        AddPaymentTermsLegacyResponse addPaymentTermsResponse) {

        return GetDefendantAccountPaymentTermsResponse.builder()
            .version(Optional.ofNullable(addPaymentTermsResponse.getVersion())
                .map(v -> BigInteger.valueOf(v.longValue()))
                .orElse(BigInteger.ONE))
            .paymentTerms(toPaymentTerms(addPaymentTermsResponse.getPaymentTerms()))
            .paymentCardLastRequested(addPaymentTermsResponse.getPaymentCardLastRequested())
            .lastEnforcement(addPaymentTermsResponse.getLastEnforcement())
            .build();
    }

    private static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: legacy error HTTP {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: exception:", method, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: legacy failure body:\n{}", method, response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: legacy success.", method);
        }
    }
}
