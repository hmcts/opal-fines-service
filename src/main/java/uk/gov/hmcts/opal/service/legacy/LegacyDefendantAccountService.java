package uk.gov.hmcts.opal.service.legacy;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
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
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacyJson;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponseJson;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
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

    public static final String GET_DEFENDANT_ACCOUNT_PARTY = "LIBRA.get_defendant_account_party";

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

        LegacyPartyDetails legacyParty = response.getPartyDetails();
        PartyDetails opalPartyDetails = null;

        if (legacyParty != null) {
            uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails legacyOrg = legacyParty.getOrganisationDetails();
            uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails legacyInd = legacyParty.getIndividualDetails();

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
                    .forenames(legacyInd.getFirstNames())
                    .surname(legacyInd.getSurname())
                    .dateOfBirth(legacyInd.getDateOfBirth() != null ? legacyInd.getDateOfBirth().toString() : null)
                    .age(legacyInd.getAge())
                    .nationalInsuranceNumber(legacyInd.getNationalInsuranceNumber())
                    .individualAliases(indAliases)
                    .build();

            opalPartyDetails = PartyDetails.builder()
                .partyId(legacyParty.getDefendantAccountPartyId())
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
                .accountStatusDisplayName(response.getAccountStatusReference().getAccountStatusDisplayName())
                .build();

        PaymentStateSummary pay = response.getPaymentStateSummary() == null ? null
            : PaymentStateSummary.builder()
                .imposedAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getImposedAmount()))
                .arrearsAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getArrearsAmount()))
                .paidAmount(toBigDecimalOrZero(response.getPaymentStateSummary().getPaidAmount()))
                .accountBalance(toBigDecimalOrZero(response.getPaymentStateSummary().getAccountBalance()))
                .build();

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

        // Always return the legacy JSON wrapper so top-level "version" exists for schema validation
        if (legacy == null) {
            return GetDefendantAccountPartyLegacyResponseJson.of(null, null);
        }
        if (legacy.getDefendantAccountParty() == null) {
            return GetDefendantAccountPartyLegacyResponseJson.of(legacy.getVersion(), null);
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
            LanguagePreference docPref = null;
            LanguagePreference hearPref = null;

            LanguagePreferencesLegacy.LanguagePreference doc = lp.getDocumentLanguagePreference();
            LanguagePreferencesLegacy.LanguagePreference hear = lp.getHearingLanguagePreference();

            String docCode = mapSafe(doc, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode);
            if (docCode != null && !docCode.isBlank()) {
                docPref = LanguagePreference.builder()
                        .languageCode(LanguagePreference.LanguageCode.fromValue(docCode))
                    .build();
            }

            String hearCode = mapSafe(hear, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode);
            if (hearCode != null && !hearCode.isBlank()) {
                hearPref = LanguagePreference.builder()
                        .languageCode(LanguagePreference.LanguageCode.fromValue(hearCode))
                    .build();
            }

            // Only build the container if at least one child exists (avoid {} which fails schema)
            if (docPref != null || hearPref != null) {
                apiLangs = LanguagePreferences.builder()
                    .documentLanguagePreference(docPref)
                    .hearingLanguagePreference(hearPref)
                    .build();
            }
        }

        // Build the JSON party that includes required defendant_account_party_id
        DefendantAccountPartyLegacyJson legacyParty =
            new DefendantAccountPartyLegacyJson();
        legacyParty.setDefendantAccountPartyId(mapSafe(pd, PartyDetailsLegacy::getPartyId));
        legacyParty.setDefendantAccountPartyType(src.getDefendantAccountPartyType());
        legacyParty.setIsDebtor(src.getIsDebtor());
        legacyParty.setPartyDetails(apiPartyDetails);
        legacyParty.setAddress(apiAddress);
        legacyParty.setContactDetails(apiContact);
        legacyParty.setVehicleDetails(apiVehicle);
        legacyParty.setEmployerDetails(apiEmployer);
        legacyParty.setLanguagePreferences(apiLangs);

        // Return the legacy wrapper with version + correctly-shaped party
        return GetDefendantAccountPartyLegacyResponseJson.of(legacy.getVersion(), legacyParty);
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
            .version(src.getVersion())
            .build();
    }

    private PartyDetails toPartyDetails(
        LegacyPartyDetails src) {
        if (src == null) {
            return null;
        }
        Boolean org = src.getOrganisationFlag();
        return PartyDetails.builder()
            .partyId(src.getDefendantAccountPartyId())
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
    public DefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                           String businessUnitId,
                                                           UpdateDefendantAccountRequest request,
                                                           String ifMatch,
                                                           String postedBy) {
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_IMPLEMENTED,
            "Update Defendant Account is not implemented in legacy mode");
    }

}
