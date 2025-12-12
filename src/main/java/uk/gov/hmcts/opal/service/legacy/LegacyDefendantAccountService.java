package uk.gov.hmcts.opal.service.legacy;

import static uk.gov.hmcts.opal.service.legacy.LegacyDefendantAccountBuilders.toEnforcementStatusResponse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddDefendantAccountEnforcementLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardRequestLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.AddPaymentCardRequestLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ResultResponsesLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.mapper.legacy.LegacyUpdateDefendantAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.request.UpdateDefendantAccountRequestMapper;
import uk.gov.hmcts.opal.repository.jpa.SpecificationUtils;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountService")
public class LegacyDefendantAccountService implements DefendantAccountServiceInterface {

    public static final String GET_HEADER_SUMMARY = "LIBRA.get_header_summary";
    public static final String SEARCH_DEFENDANT_ACCOUNTS = "searchDefendantAccounts";
    public static final String GET_PAYMENT_TERMS = "LIBRA.get_payment_terms";
    public static final String GET_DEFENDANT_AT_A_GLANCE = "LIBRA.getDefendantAtAGlance";
    public static final String ADD_ENFORCEMENT = "LIBRA.addEnforcement";


    public static final String GET_DEFENDANT_ACCOUNT_PARTY = "LIBRA.get_defendant_account_party";
    public static final String REPLACE_DEFENDANT_ACCOUNT_PARTY = "LIBRA.replace_defendant_account_party";
    public static final String PATCH_DEFENDANT_ACCOUNT = "LIBRA.patchDefendantAccount";
    public static final String GET_ENFORCEMENT_STATUS = "LIBRA.of_get_defendant_account_enf_status";

    public static final String ADD_PAYMENT_CARD_REQUEST = "LIBRA.of_add_defendant_account_pcr";


    private final GatewayService gatewayService;
    private final LegacyGatewayProperties legacyGatewayProperties;

    /* ---- Mappers ---- */
    private final UpdateDefendantAccountRequestMapper updateDefendantAccountRequestMapper;
    private final LegacyUpdateDefendantAccountResponseMapper legacyUpdateDefendantAccountResponseMapper;

    private final UserStateService userStateService;


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

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {

        Response<LegacyGetDefendantAccountPaymentTermsResponse> response = gatewayService.postToGateway(
            GET_PAYMENT_TERMS, LegacyGetDefendantAccountPaymentTermsResponse.class,
            createGetDefendantAccountRequest(defendantAccountId.toString()), null);

        if (response.isError()) {
            log.error(":getPaymentTerms: Legacy Gateway response: HTTP Response Code: {}", response.code);
            if (response.isException()) {
                log.error(":getPaymentTerms:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getPaymentTerms: Legacy Gateway: body: \n{}", response.body);
                LegacyGetDefendantAccountPaymentTermsResponse responseEntity = response.responseEntity;
                log.error(":getPaymentTerms: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":getPaymentTerms: Legacy Gateway response: Success.");
        }

        return toPaymentTermsResponse(response.responseEntity);
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

        var legacyParty = response.getPartyDetails();
        PartyDetails opalPartyDetails = null;

        if (legacyParty != null) {
            var legacyOrg = legacyParty.getOrganisationDetails();
            var legacyInd = legacyParty.getIndividualDetails();

            List<OrganisationAlias> orgAliases = (legacyOrg != null && legacyOrg.getOrganisationAliases() != null)
                ? Arrays.stream(legacyOrg.getOrganisationAliases())
                .filter(a -> a.getAliasId() != null && a.getOrganisationName() != null)
                .map(a -> OrganisationAlias.builder()
                    .aliasId(a.getAliasId())
                    .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                    .organisationName(a.getOrganisationName())
                    .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

            List<IndividualAlias> indAliases = (legacyInd != null && legacyInd.getIndividualAliases() != null)
                ? Arrays.stream(legacyInd.getIndividualAliases())
                .filter(a -> a.getAliasId() != null)
                .map(a -> IndividualAlias.builder()
                    .aliasId(a.getAliasId())
                    .sequenceNumber(a.getSequenceNumber() != null ? a.getSequenceNumber().intValue() : null)
                    .surname(a.getSurname())
                    .forenames(a.getForenames())
                    .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

            OrganisationDetails opalOrg = Boolean.TRUE.equals(legacyParty.getOrganisationFlag()) && legacyOrg != null
                ? OrganisationDetails.builder()
                .organisationName(legacyOrg.getOrganisationName())
                .organisationAliases(orgAliases)
                .build()
                : null;

            IndividualDetails opalInd = !Boolean.TRUE.equals(legacyParty.getOrganisationFlag()) && legacyInd != null
                ? IndividualDetails.builder()
                .title(legacyInd.getTitle())
                .forenames(legacyInd.getFirstNames())
                .surname(legacyInd.getSurname())
                .dateOfBirth(legacyInd.getDateOfBirth() != null ? legacyInd.getDateOfBirth().toString() : null)
                .age(legacyInd.getAge())
                .nationalInsuranceNumber(legacyInd.getNationalInsuranceNumber())
                .individualAliases(indAliases)
                .build()
                : null;

            opalPartyDetails = PartyDetails.builder()
                .partyId(legacyParty.getPartyId())
                .organisationFlag(legacyParty.getOrganisationFlag())
                .organisationDetails(opalOrg)
                .individualDetails(opalInd)
                .build();
        }

        BusinessUnitSummary bu = response.getBusinessUnitSummary() == null ? null
            : BusinessUnitSummary.builder()
                .businessUnitId(response.getBusinessUnitSummary().getBusinessUnitId())
                .businessUnitName(response.getBusinessUnitSummary().getBusinessUnitName())
                .welshSpeaking("N")
                .build();

        AccountStatusReference status = response.getAccountStatusReference() == null ? null
            : AccountStatusReference.builder()
                .accountStatusCode(response.getAccountStatusReference().getAccountStatusCode())
                .accountStatusDisplayName(
                    Optional.ofNullable(response.getAccountStatusReference().getAccountStatusDisplayName())
                        .orElse(SpecificationUtils.mapAccountStatusDisplayName(
                            response.getAccountStatusReference().getAccountStatusCode()))
                )
                .build();

        // ----- Payment State Summary (never null numbers) -----
        PaymentStateSummary pay = response.getPaymentStateSummary() == null ? null
            : PaymentStateSummary.builder()
                .imposedAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getImposedAmount()))
                .arrearsAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getArrearsAmount()))
                .paidAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getPaidAmount()))
                .accountBalance(toBigDecimalOrZero(response.getPaymentStateSummary().getAccountBalance()))
                .build();

        return DefendantAccountHeaderSummary.builder()
            .version(new BigInteger(Optional.ofNullable(response.getVersion()).orElse("1")))
            .defendantAccountId(response.getDefendantAccountId())
            .accountNumber(response.getAccountNumber())
            .defendantAccountPartyId(response.getDefendantPartyId())
            .parentGuardianPartyId(response.getParentGuardianPartyId())
            .debtorType(Optional.ofNullable(response.getDebtorType()).orElse("Defendant"))
            .isYouth(Optional.ofNullable(response.getIsYouth()).orElse(Boolean.FALSE))
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

    private GetDefendantAccountPaymentTermsResponse toPaymentTermsResponse(
        LegacyGetDefendantAccountPaymentTermsResponse legacy) {

        if (legacy == null) {
            return null;
        }

        return GetDefendantAccountPaymentTermsResponse.builder()
            .version(legacy.getVersion())
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

    private static PaymentTermsType toPaymentTermsType(LegacyPaymentTermsType legacy) {
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

    private static InstalmentPeriod toInstalmentPeriod(LegacyInstalmentPeriod legacy) {
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

    @Override
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId) {
        log.debug(":getDefendantAccountParty: Legacy call for accountId={}, partyId={}",
            defendantAccountId, defendantAccountPartyId);

        GetDefendantAccountPartyLegacyRequest req = GetDefendantAccountPartyLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .defendantAccountPartyId(String.valueOf(defendantAccountPartyId))
            .build();

        Response<GetDefendantAccountPartyLegacyResponse> response = gatewayService.postToGateway(
            GET_DEFENDANT_ACCOUNT_PARTY,
            GetDefendantAccountPartyLegacyResponse.class,
            req,
            null
        );

        if (response.isError()) {
            log.error(":getDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":getDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":getDefendantAccountParty: Legacy success.");
        }

        return toDefendantAccountPartyResponse(response.responseEntity);
    }

    private GetDefendantAccountPartyResponse toDefendantAccountPartyResponse(
        GetDefendantAccountPartyLegacyResponse legacy) {
        GetDefendantAccountPartyResponse response = new GetDefendantAccountPartyResponse();
        // Always return the legacy JSON wrapper so top-level "version" exists for schema validation
        if (legacy == null || legacy.getDefendantAccountParty() == null) {
            return response;
        }

        DefendantAccountPartyLegacy src = legacy.getDefendantAccountParty();

        // ----- Party details -----
        PartyDetailsLegacy pd = src.getPartyDetails();
        boolean isOrg = Boolean.TRUE.equals(mapSafe(pd, PartyDetailsLegacy::getOrganisationFlag));

        OrganisationDetails opalOrg = null;
        if (isOrg) {
            opalOrg = OrganisationDetails.builder()
                .organisationName(mapSafe(pd != null ? pd.getOrganisationDetails() : null,
                    OrganisationDetailsLegacy::getOrganisationName))
                // arrays must not be null (schema expects an array)
                .organisationAliases(java.util.Collections.emptyList())
                .build();
        }

        IndividualDetails opalInd = null;
        if (!isOrg) {
            opalInd = IndividualDetails.builder()
                .title(mapSafe(pd != null ? pd.getIndividualDetails() : null, IndividualDetailsLegacy::getTitle))
                .forenames(mapSafe(pd != null ? pd.getIndividualDetails() : null,
                    IndividualDetailsLegacy::getForenames))
                .surname(mapSafe(pd != null ? pd.getIndividualDetails() : null,
                    IndividualDetailsLegacy::getSurname))
                .dateOfBirth(mapSafe(pd != null ? pd.getIndividualDetails() : null,
                    IndividualDetailsLegacy::getDateOfBirth))
                .age(mapSafe(pd != null ? pd.getIndividualDetails() : null, IndividualDetailsLegacy::getAge))
                .nationalInsuranceNumber(mapSafe(pd != null ? pd.getIndividualDetails() : null,
                    IndividualDetailsLegacy::getNationalInsuranceNumber))
                // arrays must not be null (schema expects an array)
                .individualAliases(java.util.Collections.emptyList())
                .build();
        }

        final PartyDetails apiPartyDetails = PartyDetails.builder()
            .partyId(mapSafe(pd, PartyDetailsLegacy::getPartyId))
            .organisationFlag(isOrg)
            .organisationDetails(opalOrg)
            .individualDetails(opalInd)
            .build();

        // ----- Address -----
        AddressDetailsLegacy a = src.getAddress();
        final AddressDetails apiAddress =
            (a == null) ? null
                : AddressDetails.builder()
                    .addressLine1(mapSafe(a, AddressDetailsLegacy::getAddressLine1))
                    .addressLine2(mapSafe(a, AddressDetailsLegacy::getAddressLine2))
                    .addressLine3(mapSafe(a, AddressDetailsLegacy::getAddressLine3))
                    .addressLine4(mapSafe(a, AddressDetailsLegacy::getAddressLine4))
                    .addressLine5(mapSafe(a, AddressDetailsLegacy::getAddressLine5))
                    .postcode(mapSafe(a, AddressDetailsLegacy::getPostcode))
                    .build();

        // ----- Contact -----
        ContactDetailsLegacy c = src.getContactDetails();
        ContactDetails apiContact =
            (c == null) ? null
                : ContactDetails.builder()
                    .primaryEmailAddress(mapSafe(c, ContactDetailsLegacy::getPrimaryEmailAddress))
                    .secondaryEmailAddress(mapSafe(c, ContactDetailsLegacy::getSecondaryEmailAddress))
                    .mobileTelephoneNumber(mapSafe(c, ContactDetailsLegacy::getMobileTelephoneNumber))
                    .homeTelephoneNumber(mapSafe(c, ContactDetailsLegacy::getHomeTelephoneNumber))
                    .workTelephoneNumber(mapSafe(c, ContactDetailsLegacy::getWorkTelephoneNumber))
                    .build();
        // Drop empty {} contact_details
        if (apiContact != null
            && apiContact.getPrimaryEmailAddress() == null
            && apiContact.getSecondaryEmailAddress() == null
            && apiContact.getMobileTelephoneNumber() == null
            && apiContact.getHomeTelephoneNumber() == null
            && apiContact.getWorkTelephoneNumber() == null) {
            apiContact = null;
        }

        // ----- Vehicle -----
        VehicleDetailsLegacy v = src.getVehicleDetails();
        VehicleDetails apiVehicle =
            (v == null) ? null
                : VehicleDetails.builder()
                    .vehicleMakeAndModel(mapSafe(v, VehicleDetailsLegacy::getVehicleMakeAndModel))
                    .vehicleRegistration(mapSafe(v, VehicleDetailsLegacy::getVehicleRegistration))
                    .build();
        // Drop empty {} vehicle_details
        if (apiVehicle != null
            && apiVehicle.getVehicleMakeAndModel() == null
            && apiVehicle.getVehicleRegistration() == null) {
            apiVehicle = null;
        }

        // ----- Employer -----
        EmployerDetailsLegacy e = src.getEmployerDetails();
        AddressDetailsLegacy ea = e != null ? e.getEmployerAddress() : null;

        // Build employer address but ONLY keep it if address_line_1 exists (schema requires it)
        AddressDetails apiEmpAddress = null;
        if (ea != null) {
            String empLine1 = mapSafe(ea, AddressDetailsLegacy::getAddressLine1);
            if (empLine1 != null && !empLine1.isBlank()) {
                apiEmpAddress = AddressDetails.builder()
                    .addressLine1(empLine1)
                    .addressLine2(mapSafe(ea, AddressDetailsLegacy::getAddressLine2))
                    .addressLine3(mapSafe(ea, AddressDetailsLegacy::getAddressLine3))
                    .addressLine4(mapSafe(ea, AddressDetailsLegacy::getAddressLine4))
                    .addressLine5(mapSafe(ea, AddressDetailsLegacy::getAddressLine5))
                    .postcode(mapSafe(ea, AddressDetailsLegacy::getPostcode))
                    .build();
            }
        }

        EmployerDetails apiEmployer = null;
        if (e != null) {
            EmployerDetails tmp = EmployerDetails.builder()
                .employerName(mapSafe(e, EmployerDetailsLegacy::getEmployerName))
                .employerReference(mapSafe(e, EmployerDetailsLegacy::getEmployerReference))
                .employerEmailAddress(mapSafe(e, EmployerDetailsLegacy::getEmployerEmailAddress))
                .employerTelephoneNumber(mapSafe(e, EmployerDetailsLegacy::getEmployerTelephoneNumber))
                .employerAddress(apiEmpAddress) // may be null if address_line_1 absent
                .build();

            // Drop entire employer_details if all fields (including address) are null
            if (tmp.getEmployerName() != null
                || tmp.getEmployerReference() != null
                || tmp.getEmployerEmailAddress() != null
                || tmp.getEmployerTelephoneNumber() != null
                || tmp.getEmployerAddress() != null) {
                apiEmployer = tmp;
            }
        }

        // ----- Language Preferences -----
        LanguagePreferencesLegacy lp = src.getLanguagePreferences();
        LanguagePreferences apiLangs = null;

        if (lp != null) {
            // Extract legacy document and hearing preferences
            LanguagePreferencesLegacy.LanguagePreference doc = lp.getDocumentLanguagePreference();
            LanguagePreferencesLegacy.LanguagePreference hear = lp.getHearingLanguagePreference();

            String docCode = mapSafe(doc, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode);
            String hearCode = mapSafe(hear, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode);

            // Build the new legacy JSON DTOs (no language_display_name)
            LanguagePreference docPrefJson = null;
            LanguagePreference hearPrefJson = null;

            if (docCode != null && !docCode.isBlank()) {
                docPrefJson = LanguagePreference.fromCode(docCode);
            }

            if (hearCode != null && !hearCode.isBlank()) {
                hearPrefJson = LanguagePreference.fromCode(hearCode);
            }

            // Only build the container if at least one child exists (avoid {} which fails schema)
            if (docPrefJson != null || hearPrefJson != null) {
                apiLangs = LanguagePreferences.builder()
                    .documentLanguagePreference(docPrefJson)
                    .hearingLanguagePreference(hearPrefJson)
                    .build();
            }
        }

        // Build the JSON party that includes required defendant_account_party_id
        DefendantAccountParty legacyParty = new DefendantAccountParty();
        legacyParty.setDefendantAccountPartyType(src.getDefendantAccountPartyType());
        legacyParty.setIsDebtor(src.getIsDebtor());
        legacyParty.setPartyDetails(apiPartyDetails);
        legacyParty.setAddress(apiAddress);
        legacyParty.setContactDetails(apiContact);
        legacyParty.setVehicleDetails(apiVehicle);
        legacyParty.setEmployerDetails(apiEmployer);
        legacyParty.setLanguagePreferences(apiLangs);

        // Return the legacy wrapper with version + correctly-shaped party
        response.setDefendantAccountParty(legacyParty);
        response.setVersion(BigInteger.valueOf(legacy.getVersion()));
        return response;
    }

    private static <T, R> R mapSafe(T obj, java.util.function.Function<T, R> f) {
        return obj == null ? null : f.apply(obj);
    }

    @Override
    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.info(":getAtAGlance: id: {}", defendantAccountId);

        Response<LegacyGetDefendantAccountAtAGlanceResponse> response = gatewayService.postToGateway(
            GET_DEFENDANT_AT_A_GLANCE, LegacyGetDefendantAccountAtAGlanceResponse.class,
            createGetDefendantAccountRequest(defendantAccountId.toString()), null);

        if (response.isError()) {
            log.error(":getAtAGlance: Legacy Gateway response: HTTP Response Code: {}", response.code);
            if (response.isException()) {
                log.error(":getAtAGlance:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":getAtAGlance: Legacy Gateway: body: \n{}", response.body);
                LegacyGetDefendantAccountAtAGlanceResponse responseEntity = response.responseEntity;
                log.error(":getAtAGlance: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (response.isSuccessful()) {
            log.info(":getAtAGlance: Legacy Gateway response: Success.");
        }

        return toDefendantAtAGlanceResponse(response.responseEntity);

    }

    private DefendantAccountAtAGlanceResponse toDefendantAtAGlanceResponse(
        LegacyGetDefendantAccountAtAGlanceResponse src) {
        if (src == null) {
            return null;
        }

        return DefendantAccountAtAGlanceResponse.builder()
            .defendantAccountId(src.getDefendantAccountId())
            .accountNumber(src.getAccountNumber())
            .debtorType(src.getDebtorType())
            .isYouth(src.isYouth())
            .partyDetails(toPartyDetails(src.getPartyDetails()))
            .addressDetails(toAddress(src.getAddress()))
            .languagePreferences(toLanguagePreferences(src.getLanguagePreferences()))
            .paymentTermsSummary(toPaymentTermsFromSummary(src.getPaymentTermsSummary()))
            .enforcementStatus(toEnforcementStatus(src.getEnforcementStatusSummary()))
            .commentsAndNotes(toComments(src.getCommentsAndNotes()))
            .version(BigInteger.valueOf(src.getVersion()))
            .build();
    }

    private PartyDetails toPartyDetails(
        LegacyPartyDetails src) {
        if (src == null) {
            return null;
        }
        Boolean org = src.getOrganisationFlag();
        return PartyDetails.builder()
            .partyId(src.getPartyId())
            .organisationFlag(org)
            .organisationDetails(Boolean.TRUE.equals(org) ? toOrganisationDetails(src.getOrganisationDetails()) : null)
            .individualDetails(!Boolean.TRUE.equals(org) ? toIndividualDetails(src.getIndividualDetails()) : null)
            .build();
    }

    private OrganisationDetails toOrganisationDetails(
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails src) {
        if (src == null) {
            return null;
        }
        java.util.List<OrganisationAlias> aliases = java.util.Optional
            .ofNullable(src.getOrganisationAliases())
            .map(java.util.Arrays::asList)
            .orElseGet(java.util.List::of)
            .stream()
            .map(this::toOrganisationAlias)
            .filter(java.util.Objects::nonNull)
            .toList();

        return OrganisationDetails.builder()
            .organisationName(src.getOrganisationName())
            .organisationAliases(aliases.isEmpty() ? null : aliases)
            .build();
    }

    private OrganisationAlias toOrganisationAlias(
        uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails.OrganisationAlias el) {
        if (el == null) {
            return null;
        }
        return OrganisationAlias.builder()
            .aliasId(el.getAliasId())
            .sequenceNumber(el.getSequenceNumber() == null ? null : el.getSequenceNumber().intValue())
            .organisationName(el.getOrganisationName())
            .build();
    }

    private IndividualDetails toIndividualDetails(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails src) {
        if (src == null) {
            return null;
        }

        java.util.List<IndividualAlias> aliases = java.util.Optional
            .ofNullable(src.getIndividualAliases())
            .map(java.util.Arrays::asList)
            .orElseGet(java.util.List::of)
            .stream()
            .map(this::toIndividualAlias)
            .filter(java.util.Objects::nonNull)
            .toList();

        return IndividualDetails.builder()
            .title(src.getTitle())
            .forenames(src.getFirstNames())
            .surname(src.getSurname())
            .dateOfBirth(src.getDateOfBirth() == null ? null : src.getDateOfBirth().toString())
            .age(src.getAge())
            .nationalInsuranceNumber(src.getNationalInsuranceNumber())
            .individualAliases(aliases.isEmpty() ? null : aliases)
            .build();
    }

    private IndividualAlias toIndividualAlias(
        uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails.IndividualAlias el) {
        if (el == null) {
            return null;
        }
        return IndividualAlias.builder()
            .aliasId(el.getAliasId())
            .sequenceNumber(el.getSequenceNumber() == null ? null : el.getSequenceNumber().intValue())
            .surname(el.getSurname())
            .forenames(el.getForenames())
            .build();
    }

    private AddressDetails toAddress(
        uk.gov.hmcts.opal.dto.legacy.common.AddressDetails src) {
        if (src == null) {
            return null;
        }
        return AddressDetails.builder()
            .addressLine1(src.getAddressLine1())
            .addressLine2(src.getAddressLine2())
            .addressLine3(src.getAddressLine3())
            .addressLine4(src.getAddressLine4())
            .addressLine5(src.getAddressLine5())
            .postcode(src.getPostcode())
            .build();
    }

    private LanguagePreferences toLanguagePreferences(
        uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences src) {
        if (src == null) {
            return null;
        }

        String docCode = java.util.Optional.ofNullable(src.getDocumentLanguagePreference())
            .map(uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.DocumentLanguagePreference
                ::getDocumentLanguageCode)
            .orElse(null);

        String hearingCode = java.util.Optional.ofNullable(src.getHearingLanguagePreference())
            .map(uk.gov.hmcts.opal.dto.legacy.common.LanguagePreferences.HearingLanguagePreference
                ::getHearingLanguageCode)
            .orElse(null);

        return LanguagePreferences.ofCodes(docCode, hearingCode);
    }

    private PaymentTermsSummary toPaymentTermsFromSummary(
        uk.gov.hmcts.opal.dto.legacy.common.PaymentTermsSummary src) {
        if (src == null) {
            return null;
        }

        String typeCode = java.util.Optional.ofNullable(src.getPaymentTermsType())
            .map(uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType::getPaymentTermsTypeCode)
            .map(Enum::name)
            .orElse(null);

        String instalmentCode = java.util.Optional.ofNullable(src.getInstalmentPeriod())
            .map(uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod::getInstalmentPeriodCode)
            .map(Enum::name)
            .orElse(null);

        return PaymentTermsSummary.builder()
            .paymentTermsType(typeCode == null ? null : PaymentTermsType.fromCode(typeCode))
            .effectiveDate(src.getEffectiveDate())
            .instalmentPeriod(instalmentCode == null ? null : InstalmentPeriod.fromCode(instalmentCode))
            .lumpSumAmount(src.getLumpSumAmount())
            .instalmentAmount(src.getInstalmentAmount())
            .build();
    }

    private EnforcementStatusSummary toEnforcementStatus(
        uk.gov.hmcts.opal.dto.legacy.common.EnforcementStatusSummary src) {
        if (src == null) {
            return null;
        }

        return EnforcementStatusSummary.builder()
            .lastEnforcementAction(src.getLastEnforcementAction())
            .collectionOrderMade(src.getCollectionOrderMade())
            .defaultDaysInJail(src.getDefaultDaysInJail())
            .enforcementOverride(src.getEnforcementOverride())
            .lastMovementDate(src.getLastMovementDate()) // LocalDate
            .build();
    }

    private CommentsAndNotes toComments(
        uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes src) {
        if (src == null) {
            return null;
        }

        return CommentsAndNotes.builder()
            .accountNotesAccountComments(src.getAccountComment())
            .accountNotesFreeTextNote1(src.getFreeTextNote1())
            .accountNotesFreeTextNote2(src.getFreeTextNote2())
            .accountNotesFreeTextNote3(src.getFreeTextNote3())
            .build();
    }

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        throw new UnsupportedOperationException("Legacy GetDefendantAccountFixedPenalty not implemented yet");
    }

    @Override
    public DefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
        String businessUnitId,
        @NonNull UpdateDefendantAccountRequest request,
        String ifMatch,
        String postedBy) {

        log.info("Legacy :updateDefendantAccount: id: {}", defendantAccountId);

        // in legacy system, If-Match header value is passed in as version.
        Integer version = Integer.parseInt(ifMatch);

        // build legacy request object with mapped fields from UpdateDefendantAccountRequest
        // pass 'version' into the mapper/to-legacy request builder
        LegacyUpdateDefendantAccountRequest legacyRequest =
            updateDefendantAccountRequestMapper.toLegacyUpdateDefendantAccountRequest(request,
                String.valueOf(defendantAccountId),
                businessUnitId,
                postedBy,
                version);

        // Send the request to the gateway service
        Response<LegacyUpdateDefendantAccountResponse> gwResponse = gatewayService.postToGateway(
            PATCH_DEFENDANT_ACCOUNT, LegacyUpdateDefendantAccountResponse.class,
            legacyRequest, null);

        if (gwResponse.isError()) {
            log.error(":updateDefendantAccount: Legacy Gateway response: HTTP Response Code: {}", gwResponse.code);
            if (gwResponse.isException()) {
                log.error(":updateDefendantAccount: Legacy Gateway response exception: {}", gwResponse.exception);
            } else if (gwResponse.isLegacyFailure()) {
                log.error(":updateDefendantAccount: Legacy Gateway: body: \n{}", gwResponse.body);
                LegacyUpdateDefendantAccountResponse responseEntity = gwResponse.responseEntity;
                log.error(":updateDefendantAccount: Legacy Gateway: entity: \n{}", responseEntity.toXml());
            }
        } else if (gwResponse.isSuccessful()) {
            log.info(":updateDefendantAccount: Legacy Gateway response: Success.");
        }

        return legacyUpdateDefendantAccountResponseMapper.toDefendantAccountResponse(gwResponse.responseEntity);
    }

    @Override
    public AddPaymentCardRequestResponse addPaymentCardRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader
    ) {
        log.info(":addPaymentCardRequest (Legacy): accountId={}, bu={}", defendantAccountId, businessUnitId);

        Integer version = parseVersion(ifMatch);
        AddPaymentCardRequestLegacyRequest request = buildLegacyRequest(defendantAccountId, businessUnitId,
            businessUnitUserId, version);

        AddPaymentCardRequestLegacyResponse response = callGateway(request);
        Long id = Long.valueOf(response.getDefendantAccountId());

        return new AddPaymentCardRequestResponse(id);
    }

    private Integer parseVersion(String ifMatch) {
        try {
            return Integer.parseInt(ifMatch);
        } catch (Exception ex) {
            log.error(":addPaymentCardRequest: invalid If-Match '{}'", ifMatch);
            throw new IllegalArgumentException("Invalid version/If-Match header");
        }
    }

    private AddPaymentCardRequestLegacyRequest buildLegacyRequest(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        Integer version
    ) {
        return AddPaymentCardRequestLegacyRequest.builder()
            .defendantAccountId(String.valueOf(defendantAccountId))
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .version(version)
            .build();
    }

    private AddPaymentCardRequestLegacyResponse callGateway(AddPaymentCardRequestLegacyRequest request) {

        Response<AddPaymentCardRequestLegacyResponse> gw =
            gatewayService.postToGateway(
                ADD_PAYMENT_CARD_REQUEST,
                AddPaymentCardRequestLegacyResponse.class,
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
    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId,
        DefendantAccountParty defendantAccountParty, String ifMatch, String businessUnitId, String postedBy,
        String businessUnitUserId) {

        LegacyReplaceDefendantAccountPartyRequest req = LegacyReplaceDefendantAccountPartyRequest.builder()
            .version(Long.parseLong(ifMatch.replace("\"", "").trim()))
            .defendantAccountId(defendantAccountId)
            .businessUnitId(businessUnitId)
            .businessUnitUserId(businessUnitUserId)
            .defendantAccountParty(defendantAccountParty)
            .build();

        Response<LegacyReplaceDefendantAccountPartyResponse> response = gatewayService.postToGateway(
            REPLACE_DEFENDANT_ACCOUNT_PARTY,
            LegacyReplaceDefendantAccountPartyResponse.class,
            req,
            null
        );

        if (response.isError()) {
            log.error(":replaceDefendantAccountParty: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":replaceDefendantAccountParty: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":replaceDefendantAccountParty: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":replaceDefendantAccountParty: Legacy success.");
        }

        return fromReplaceDefendantAccountPartyLegacy(response.responseEntity);
    }

    private GetDefendantAccountPartyResponse fromReplaceDefendantAccountPartyLegacy(
        LegacyReplaceDefendantAccountPartyResponse legacy) {

        if (legacy == null) {
            return null;
        }

        DefendantAccountPartyLegacy legacyDefendantAccountParty = legacy.getDefendantAccountParty();

        PartyDetails party = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getPartyDetails() != null) {
            PartyDetailsLegacy partyDetailsLegacy = legacyDefendantAccountParty.getPartyDetails();

            OrganisationDetails org = null;
            if (partyDetailsLegacy.getOrganisationDetails() != null) {
                org = OrganisationDetails.builder()
                    .organisationName(partyDetailsLegacy.getOrganisationDetails().getOrganisationName())
                    .build();
            }

            IndividualDetails ind = null;
            if (partyDetailsLegacy.getIndividualDetails() != null) {
                IndividualDetailsLegacy individualDetailsLegacy = partyDetailsLegacy.getIndividualDetails();
                ind = IndividualDetails.builder()
                    .title(individualDetailsLegacy.getTitle())
                    .forenames(individualDetailsLegacy.getForenames())
                    .surname(individualDetailsLegacy.getSurname())
                    .dateOfBirth(individualDetailsLegacy.getDateOfBirth())
                    .age(individualDetailsLegacy.getAge())
                    .nationalInsuranceNumber(individualDetailsLegacy.getNationalInsuranceNumber())
                    .build();
            }

            party = PartyDetails.builder()
                .partyId(partyDetailsLegacy.getPartyId())
                .organisationFlag(partyDetailsLegacy.getOrganisationFlag())
                .organisationDetails(org)
                .individualDetails(ind)
                .build();
        }

        // Map Address
        AddressDetails address = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getAddress() != null) {
            AddressDetailsLegacy addressDetailsLegacy = legacyDefendantAccountParty.getAddress();
            address = AddressDetails.builder()
                .addressLine1(addressDetailsLegacy.getAddressLine1())
                .addressLine2(addressDetailsLegacy.getAddressLine2())
                .addressLine3(addressDetailsLegacy.getAddressLine3())
                .addressLine4(addressDetailsLegacy.getAddressLine4())
                .addressLine5(addressDetailsLegacy.getAddressLine5())
                .postcode(addressDetailsLegacy.getPostcode())
                .build();
        }

        // Map Contact
        ContactDetails contact = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getContactDetails() != null) {
            ContactDetailsLegacy contactDetailsLegacy = legacyDefendantAccountParty.getContactDetails();
            contact = ContactDetails.builder()
                .primaryEmailAddress(contactDetailsLegacy.getPrimaryEmailAddress())
                .secondaryEmailAddress(contactDetailsLegacy.getSecondaryEmailAddress())
                .mobileTelephoneNumber(contactDetailsLegacy.getMobileTelephoneNumber())
                .homeTelephoneNumber(contactDetailsLegacy.getHomeTelephoneNumber())
                .workTelephoneNumber(contactDetailsLegacy.getWorkTelephoneNumber())
                .build();
        }

        // Map Vehicle
        VehicleDetails vehicle = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getVehicleDetails() != null) {
            VehicleDetailsLegacy vehicleDetailsLegacy = legacyDefendantAccountParty.getVehicleDetails();
            vehicle = VehicleDetails.builder()
                .vehicleMakeAndModel(vehicleDetailsLegacy.getVehicleMakeAndModel())
                .vehicleRegistration(vehicleDetailsLegacy.getVehicleRegistration())
                .build();
        }

        // Map Employer
        EmployerDetails employer = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getEmployerDetails() != null) {
            EmployerDetailsLegacy employerDetailsLegacy = legacyDefendantAccountParty.getEmployerDetails();
            AddressDetails employerAddr = null;
            if (employerDetailsLegacy.getEmployerAddress() != null) {
                employerAddr = AddressDetails.builder()
                    .addressLine1(employerDetailsLegacy.getEmployerAddress().getAddressLine1())
                    .addressLine2(employerDetailsLegacy.getEmployerAddress().getAddressLine2())
                    .addressLine3(employerDetailsLegacy.getEmployerAddress().getAddressLine3())
                    .addressLine4(employerDetailsLegacy.getEmployerAddress().getAddressLine4())
                    .addressLine5(employerDetailsLegacy.getEmployerAddress().getAddressLine5())
                    .postcode(employerDetailsLegacy.getEmployerAddress().getPostcode())
                    .build();
            }
            employer = EmployerDetails.builder()
                .employerName(employerDetailsLegacy.getEmployerName())
                .employerReference(employerDetailsLegacy.getEmployerReference())
                .employerEmailAddress(employerDetailsLegacy.getEmployerEmailAddress())
                .employerTelephoneNumber(employerDetailsLegacy.getEmployerTelephoneNumber())
                .employerAddress(employerAddr)
                .build();
        }

        // Map Language Preferences (use codes; never toString)
        LanguagePreferences languages = null;
        if (legacyDefendantAccountParty != null && legacyDefendantAccountParty.getLanguagePreferences() != null) {
            LanguagePreferencesLegacy legacyLanguagePreference = legacyDefendantAccountParty.getLanguagePreferences();
            String docCode = legacyLanguagePreference.getDocumentLanguagePreference() == null
                ? null : legacyLanguagePreference.getDocumentLanguagePreference().getLanguageCode();
            String hearCode = legacyLanguagePreference.getHearingLanguagePreference() == null
                ? null : legacyLanguagePreference.getHearingLanguagePreference().getLanguageCode();
            languages = LanguagePreferences.ofCodes(docCode, hearCode);
        }

        // Assemble modern DefendantAccountParty
        DefendantAccountParty modernParty = null;
        if (legacyDefendantAccountParty != null) {
            modernParty = DefendantAccountParty.builder()
                .defendantAccountPartyType(legacyDefendantAccountParty.getDefendantAccountPartyType())
                .isDebtor(legacyDefendantAccountParty.getIsDebtor())
                .partyDetails(party)
                .address(address)
                .contactDetails(contact)
                .vehicleDetails(vehicle)
                .employerDetails(employer)
                .languagePreferences(languages)
                .build();
        }

        return GetDefendantAccountPartyResponse.builder()
            .version(legacy.getVersion() == null ? null : BigInteger.valueOf(legacy.getVersion()))
            .defendantAccountParty(modernParty)
            .build();
    }

    @Override
    public AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch, String authHeader, AddDefendantAccountEnforcementRequest request) {

        String cleanVersion = ifMatch.replace("\"", "");

        // build legacy request object
        AddDefendantAccountEnforcementLegacyRequest legacyRequest =
            AddDefendantAccountEnforcementLegacyRequest.builder()
                .defendantAccountId(String.valueOf(defendantAccountId))
                .businessUnitId(businessUnitId)
                .businessUnitUserId(businessUnitUserId)
                .version(Integer.parseInt(cleanVersion))
                .resultId(request != null && request.getResultId() != null ? request.getResultId().value() : null)
                .enforcementResultResponses(
                    mapResultResponses(request != null ? request.getEnforcementResultResponses() : null))
                .paymentTerms(mapPaymentTerms(request != null ? request.getPaymentTerms() : null))
                .build();

        Response<AddDefendantAccountEnforcementLegacyResponse> response = gatewayService.postToGateway(
            ADD_ENFORCEMENT, AddDefendantAccountEnforcementLegacyResponse.class,
            legacyRequest, null);

        if (response.isError()) {
            log.error(":AddEnforcement: Legacy error HTTP {}", response.code);
            if (response.isException()) {
                log.error(":AddEnforcement: exception:", response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":AddEnforcement: legacy failure body:\n{}", response.body);
            }
        } else if (response.isSuccessful()) {
            log.info(":AddEnforcement: Legacy success.");
        }

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

    private LegacyPostedDetails mapLegacyPostedDetails(PostedDetails pd) {
        if (pd == null) {
            return null;
        }
        LegacyPostedDetails lpd = new LegacyPostedDetails();
        lpd.setPostedDate(pd.getPostedDate());
        lpd.setPostedBy(pd.getPostedBy());
        lpd.setPostedByName(pd.getPostedByName());
        return lpd;
    }

    private LegacyPaymentTermsType mapLegacyPaymentTermsType(PaymentTermsType modern) {
        if (modern == null || modern.getPaymentTermsTypeCode() == null) {
            return null;
        }
        String code = modern.getPaymentTermsTypeCode().name();
        LegacyPaymentTermsType lpt = new LegacyPaymentTermsType();
        lpt.setPaymentTermsTypeCode(mapPaymentTermsTypeCodeEnum(code));
        return lpt;
    }

    private LegacyInstalmentPeriod mapLegacyInstalmentPeriod(InstalmentPeriod modern) {
        if (modern == null || modern.getInstalmentPeriodCode() == null) {
            return null;
        }
        String code = modern.getInstalmentPeriodCode().name();
        LegacyInstalmentPeriod lip = new LegacyInstalmentPeriod();
        lip.setInstalmentPeriodCode(mapInstalmentPeriodCodeEnum(code));
        return lip;
    }

    private LegacyPaymentTermsType.PaymentTermsTypeCode mapPaymentTermsTypeCodeEnum(String code) {
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

    private LegacyInstalmentPeriod.InstalmentPeriodCode mapInstalmentPeriodCodeEnum(String code) {
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
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {
        log.debug(":getEnforcementStatus: id: {}", defendantAccountId);

        try {

            Response<LegacyGetDefendantAccountEnforcementStatusResponse> response = gatewayService.postToGateway(
                GET_ENFORCEMENT_STATUS, LegacyGetDefendantAccountEnforcementStatusResponse.class,
                createGetDefendantAccountRequest(defendantAccountId.toString()), null);

            if (response.isError()) {
                log.error(":getEnforcementStatus: Legacy Gateway response: HTTP Response Code: {}", response.code);
                if (response.isException()) {
                    log.error(":getEnforcementStatus:", response.exception);
                } else if (response.isLegacyFailure()) {
                    log.error(":getEnforcementStatus: Legacy Gateway: body: \n{}", response.body);
                    LegacyGetDefendantAccountEnforcementStatusResponse responseEntity = response.responseEntity;
                    log.error(":getEnforcementStatus: Legacy Gateway: entity: \n{}", responseEntity.toXml());
                }
            } else if (response.isSuccessful()) {
                log.info(":getEnforcementStatus: Legacy Gateway response: Success.");
            }

            return toEnforcementStatusResponse(response.responseEntity);

        } catch (RuntimeException e) {
            log.error(":getEnforcementStatus: problem with call to Legacy: {}", e.getClass().getName());
            log.error(":getEnforcementStatus:", e);
            throw e;
        }
    }

}
