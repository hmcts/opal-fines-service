package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.ContactDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.DefendantAccountPartyLegacy;
import uk.gov.hmcts.opal.dto.legacy.EmployerDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyRequest;
import uk.gov.hmcts.opal.dto.legacy.GetDefendantAccountPartyLegacyResponse;
import uk.gov.hmcts.opal.dto.legacy.IndividualDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LanguagePreferencesLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyReplaceDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.legacy.OrganisationDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.PartyDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.VehicleDetailsLegacy;
import uk.gov.hmcts.opal.service.iface.DefendantAccountPartyServiceInterface;
import uk.gov.hmcts.opal.service.legacy.GatewayService.Response;

import java.math.BigInteger;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyDefendantAccountPartyService")
public class LegacyDefendantAccountPartyService implements DefendantAccountPartyServiceInterface {

    public static final String GET_DEFENDANT_ACCOUNT_PARTY = "LIBRA.get_defendant_account_party";
    public static final String REPLACE_DEFENDANT_ACCOUNT_PARTY = "LIBRA.replace_defendant_account_party";

    private final GatewayService gatewayService;

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
                .organisationAliases(Collections.emptyList())
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
                .individualAliases(Collections.emptyList())
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
}
