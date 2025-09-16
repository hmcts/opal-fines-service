package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PaymentTermsType;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountSearchCriteria;
import uk.gov.hmcts.opal.dto.legacy.LegacyDefendantAccountsSearchResults;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
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
    public static final String GET_PAYMENT_TERMS = "LIBRA.get_payment_terms";

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

        uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails legacyParty = response.getPartyDetails();
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
            .postedDetails(toPostedDetails(legacy.getPostedDetails()))
            .paymentCardLastRequested(legacy.getPaymentCardLastRequested())
            .dateLastAmended(legacy.getDateLastAmended())
            .extension(legacy.getExtension())
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
            .reasonForExtension(legacy.getReasonForExtension())
            .paymentTermsType(toPaymentTermsType(legacy.getPaymentTermsType()))
            .effectiveDate(legacy.getEffectiveDate())
            .instalmentPeriod(toInstalmentPeriod(legacy.getInstalmentPeriod()))
            .lumpSumAmount(legacy.getLumpSumAmount())
            .instalmentAmount(legacy.getInstalmentAmount())
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

        if (legacy == null || legacy.getDefendantAccountParty() == null) {
            return GetDefendantAccountPartyResponse.builder().build();
        }

        DefendantAccountPartyLegacy src = legacy.getDefendantAccountParty();

        PartyDetailsLegacy pd = src.getPartyDetails();
        uk.gov.hmcts.opal.dto.common.PartyDetails apiPartyDetails = uk.gov.hmcts.opal.dto.common.PartyDetails.builder()
            .partyId(s(pd, PartyDetailsLegacy::getPartyId))
            .organisationFlag(Boolean.TRUE.equals(s(pd, PartyDetailsLegacy::getOrganisationFlag)))
            .organisationDetails(Boolean.TRUE.equals(s(pd, PartyDetailsLegacy::getOrganisationFlag))
                ? uk.gov.hmcts.opal.dto.common.OrganisationDetails.builder()
                .organisationName(s(pd.getOrganisationDetails(), OrganisationDetailsLegacy::getOrganisationName))
                .build()
                : null)
            .individualDetails(Boolean.TRUE.equals(s(pd, PartyDetailsLegacy::getOrganisationFlag)) ? null
                : uk.gov.hmcts.opal.dto.common.IndividualDetails.builder()
                    .title(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getTitle))
                    .forenames(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getForenames))
                    .surname(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getSurname))
                    .dateOfBirth(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getDateOfBirth))
                    .age(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getAge))
                    .nationalInsuranceNumber(s(pd.getIndividualDetails(), IndividualDetailsLegacy::getNationalInsuranceNumber))
                    .build())
            .build();

        AddressDetailsLegacy a = src.getAddress();
        uk.gov.hmcts.opal.dto.common.AddressDetails apiAddress = uk.gov.hmcts.opal.dto.common.AddressDetails.builder()
            .addressLine1(s(a, AddressDetailsLegacy::getAddressLine1))
            .addressLine2(s(a, AddressDetailsLegacy::getAddressLine2))
            .addressLine3(s(a, AddressDetailsLegacy::getAddressLine3))
            .addressLine4(s(a, AddressDetailsLegacy::getAddressLine4))
            .addressLine5(s(a, AddressDetailsLegacy::getAddressLine5))
            .postcode(s(a, AddressDetailsLegacy::getPostcode))
            .build();

        ContactDetailsLegacy c = src.getContactDetails();
        uk.gov.hmcts.opal.dto.common.ContactDetails apiContact = uk.gov.hmcts.opal.dto.common.ContactDetails.builder()
            .primaryEmailAddress(s(c, ContactDetailsLegacy::getPrimaryEmailAddress))
            .secondaryEmailAddress(s(c, ContactDetailsLegacy::getSecondaryEmailAddress))
            .mobileTelephoneNumber(s(c, ContactDetailsLegacy::getMobileTelephoneNumber))
            .homeTelephoneNumber(s(c, ContactDetailsLegacy::getHomeTelephoneNumber))
            .workTelephoneNumber(s(c, ContactDetailsLegacy::getWorkTelephoneNumber))
            .build();

        VehicleDetailsLegacy v = src.getVehicleDetails();
        uk.gov.hmcts.opal.dto.common.VehicleDetails apiVehicle = uk.gov.hmcts.opal.dto.common.VehicleDetails.builder()
            .vehicleMakeAndModel(s(v, VehicleDetailsLegacy::getVehicleMakeAndModel))
            .vehicleRegistration(s(v, VehicleDetailsLegacy::getVehicleRegistration))
            .build();

        EmployerDetailsLegacy e = src.getEmployerDetails();
        AddressDetailsLegacy ea = e != null ? e.getEmployerAddress() : null;
        uk.gov.hmcts.opal.dto.common.AddressDetails apiEmpAddress = uk.gov.hmcts.opal.dto.common.AddressDetails.builder()
            .addressLine1(s(ea, AddressDetailsLegacy::getAddressLine1))
            .addressLine2(s(ea, AddressDetailsLegacy::getAddressLine2))
            .addressLine3(s(ea, AddressDetailsLegacy::getAddressLine3))
            .addressLine4(s(ea, AddressDetailsLegacy::getAddressLine4))
            .addressLine5(s(ea, AddressDetailsLegacy::getAddressLine5))
            .postcode(s(ea, AddressDetailsLegacy::getPostcode))
            .build();

        uk.gov.hmcts.opal.dto.common.EmployerDetails apiEmployer = uk.gov.hmcts.opal.dto.common.EmployerDetails.builder()
            .employerName(s(e, EmployerDetailsLegacy::getEmployerName))
            .employerReference(s(e, EmployerDetailsLegacy::getEmployerReference))
            .employerEmailAddress(s(e, EmployerDetailsLegacy::getEmployerEmailAddress))
            .employerTelephoneNumber(s(e, EmployerDetailsLegacy::getEmployerTelephoneNumber))
            .employerAddress(apiEmpAddress)
            .build();

        LanguagePreferencesLegacy lp = src.getLanguagePreferences();
        LanguagePreferencesLegacy.LanguagePreference doc = lp != null ? lp.getDocumentLanguagePreference() : null;
        LanguagePreferencesLegacy.LanguagePreference hear = lp != null ? lp.getHearingLanguagePreference() : null;

        uk.gov.hmcts.opal.dto.common.LanguagePreferences apiLangs =
            uk.gov.hmcts.opal.dto.common.LanguagePreferences.builder()
                .documentLanguagePreference(
                    uk.gov.hmcts.opal.dto.common.LanguagePreferences.LanguagePreference.builder()
                        .languageCode(s(doc, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode))
                        .languageDisplayName(s(doc, LanguagePreferencesLegacy.LanguagePreference::getLanguageDisplayName))
                        .build())
                .hearingLanguagePreference(
                    uk.gov.hmcts.opal.dto.common.LanguagePreferences.LanguagePreference.builder()
                        .languageCode(s(hear, LanguagePreferencesLegacy.LanguagePreference::getLanguageCode))
                        .languageDisplayName(s(hear, LanguagePreferencesLegacy.LanguagePreference::getLanguageDisplayName))
                        .build())
                .build();

        uk.gov.hmcts.opal.dto.common.DefendantAccountParty apiParty =
            uk.gov.hmcts.opal.dto.common.DefendantAccountParty.builder()
                .defendantAccountPartyType(src.getDefendantAccountPartyType())
                .isDebtor(src.getIsDebtor())
                .partyDetails(apiPartyDetails)
                .address(apiAddress)
                .contactDetails(apiContact)
                .vehicleDetails(apiVehicle)
                .employerDetails(apiEmployer)
                .languagePreferences(apiLangs)
                .build();

        return uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(apiParty)
            .build();
    }

    private static <T, R> R s(T obj, java.util.function.Function<T, R> f) {
        return obj == null ? null : f.apply(obj);
    }

}
