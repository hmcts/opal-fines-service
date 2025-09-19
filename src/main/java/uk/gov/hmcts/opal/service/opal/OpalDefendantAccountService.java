package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PaymentTermsType;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.ContactDetails;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountHeaderViewRepository repository;

    private final DefendantAccountRepository defendantAccountRepository;
    private final DefendantAccountSpecs defendantAccountSpecs;
    private final DefendantAccountPaymentTermsRepository defendantAccountPaymentTermsRepository;

    @Autowired
    private DebtorDetailRepository debtorDetailRepository;

    @Autowired
    private AliasRepository aliasRepository;

    @Override
    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountHeaderViewEntity entity = repository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));

        return mapToDto(entity);
    }


    DefendantAccountHeaderSummary mapToDto(DefendantAccountHeaderViewEntity e) {
        return DefendantAccountHeaderSummary.builder()
            .defendantPartyId(
                e.getPartyId() != null ? e.getPartyId().toString() : null
            )
            .parentGuardianPartyId(
                e.getParentGuardianAccountPartyId() != null ? e.getParentGuardianAccountPartyId().toString() : null
            )
            .accountNumber(e.getAccountNumber())
            .accountType(e.getAccountType())
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .fixedPenaltyTicketNumber(e.getFixedPenaltyTicketNumber())
            .accountStatusReference(buildAccountStatusReference(e.getAccountStatus()))
            .businessUnitSummary(buildBusinessUnitSummary(e))
            .paymentStateSummary(buildPaymentStateSummary(e))
            .partyDetails(buildPartyDetails(e))
            .build();
    }


    PaymentStateSummary buildPaymentStateSummary(DefendantAccountHeaderViewEntity e) {
        return PaymentStateSummary.builder()
            .imposedAmount(nz(e.getImposed()))
            .arrearsAmount(nz(e.getArrears()))
            .paidAmount(nz(e.getPaid()))
            .accountBalance(nz(e.getAccountBalance()))
            .build();
    }

    static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }


    PartyDetails buildPartyDetails(DefendantAccountHeaderViewEntity e) {
        return PartyDetails.builder()
            .partyId(
                e.getPartyId() != null ? e.getPartyId().toString() : null
            )
            .organisationFlag(e.getOrganisation())
            .organisationDetails(
                OrganisationDetails.builder()
                    .organisationName(e.getOrganisationName())
                    .build()
            )
            .individualDetails(
                IndividualDetails.builder()
                    .title(e.getTitle())
                    .forenames(e.getFirstnames())
                    .surname(e.getSurname())
                    .dateOfBirth(e.getBirthDate() != null ? e.getBirthDate().toString() : null)
                    .age(e.getBirthDate() != null ? String.valueOf(calculateAge(e.getBirthDate())) : null)
                    .individualAliases(Collections.emptyList())
                    .nationalInsuranceNumber(null)
                    .build()
            )
            .build();
    }

    AccountStatusReference buildAccountStatusReference(String code) {
        return AccountStatusReference.builder()
            .accountStatusCode(code)
            .accountStatusDisplayName(resolveStatusDisplayName(code))
            .build();
    }

    BusinessUnitSummary buildBusinessUnitSummary(DefendantAccountHeaderViewEntity e) {
        return BusinessUnitSummary.builder()
            .businessUnitId(e.getBusinessUnitId() != null ? String.valueOf(e.getBusinessUnitId()) : null)
            .businessUnitName(e.getBusinessUnitName())
            .welshSpeaking("N")
            .build();
    }

    int calculateAge(LocalDate birthDate) {
        return birthDate != null
            ? java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears()
            : 0;
    }

    String resolveStatusDisplayName(String code) {
        return switch (code) {
            case "L" -> "Live";
            case "C" -> "Completed";
            case "TO" -> "TFO to be acknowledged";
            case "TS" -> "TFO to NI/Scotland to be acknowledged";
            case "TA" -> "TFO acknowledged";
            case "CS" -> "Account consolidated";
            case "WO" -> "Account written off";
            default -> "Unknown";
        };
    }

    @Override
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts (Opal): criteria: {}", accountSearchDto);

        Specification<DefendantAccountEntity> spec =
            defendantAccountSpecs.filterByBusinessUnits(accountSearchDto.getBusinessUnitIds())
                .and(defendantAccountSpecs.filterByActiveOnly(accountSearchDto.getActiveAccountsOnly()))
                .and(defendantAccountSpecs.filterByAccountNumberStartsWithWithCheckLetter(accountSearchDto))
                .and(defendantAccountSpecs.filterByPcrExact(accountSearchDto))
                .and(
                    accountSearchDto.getDefendant() != null
                        && Boolean.TRUE.equals(accountSearchDto.getDefendant().getOrganisation())
                        ? defendantAccountSpecs.filterByAliasesIfRequested(accountSearchDto)
                        : defendantAccountSpecs.filterByNameIncludingAliases(accountSearchDto)
                )
                .and(defendantAccountSpecs.filterByDobStartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByNiStartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByAddress1StartsWith(accountSearchDto))
                .and(defendantAccountSpecs.filterByPostcodeStartsWith(accountSearchDto));


        List<DefendantAccountEntity> rows = defendantAccountRepository.findAll(spec);

        List<DefendantAccountSummaryDto> summaries = new ArrayList<>(rows.size());
        for (DefendantAccountEntity e : rows) {
            summaries.add(toSummaryDto(e));
        }

        return DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(summaries)
            .count(summaries.size())
            .build();
    }

    @Override
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        log.debug(":getPaymentTerms (Opal): criteria: {}", defendantAccountId);

        PaymentTermsEntity entity = defendantAccountPaymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
                defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("payment terms not found for id: "
                                                               + defendantAccountId));

        return toPaymentTermsResponse(entity);
    }

    private DefendantAccountSummaryDto toSummaryDto(DefendantAccountEntity e) {
        PartyEntity party = Optional.ofNullable(e.getParties())
            .flatMap(list -> list.stream()
                .map(DefendantAccountPartiesEntity::getParty)
                .findFirst())
            .orElse(null);

        boolean isOrganisation = party != null && party.isOrganisation();
        String organisationName = party != null ? party.getOrganisationName() : null;
        String title = party != null ? party.getTitle() : null;
        String forenames = party != null ? party.getForenames() : null;
        String surname = party != null ? party.getSurname() : null;

        List<AliasDto> aliases = party != null
            ? aliasRepository.findByParty_PartyId(party.getPartyId()).stream()
            .map(a -> AliasDto.builder()
                .aliasNumber(a.getSequenceNumber())
                .organisationName(a.getOrganisationName())
                .surname(a.getSurname())
                .forenames(a.getForenames())
                .build())
            .toList()
            : List.of();

        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(e.getDefendantAccountId()))
            .accountNumber(e.getAccountNumber())
            .organisation(isOrganisation)
            .organisationName(organisationName)
            .defendantTitle(!isOrganisation ? title : null)
            .defendantFirstnames(!isOrganisation ? forenames : null)
            .defendantSurname(!isOrganisation ? surname : null)
            .addressLine1(party != null ? party.getAddressLine1() : null)
            .postcode(party != null ? party.getPostcode() : null)
            .businessUnitName(e.getBusinessUnit() != null ? e.getBusinessUnit().getBusinessUnitName() : null)
            .businessUnitId(e.getBusinessUnit() != null
                                ? String.valueOf(e.getBusinessUnit().getBusinessUnitId()) : null)
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .lastEnforcementAction(e.getLastEnforcement())
            .accountBalance(e.getAccountBalance())
            .birthDate(party != null && !isOrganisation
                           ? uk.gov.hmcts.opal.util.DateTimeUtils.toString(party.getBirthDate())
                           : null)
            .aliases(aliases)
            .build();
    }

    @Override
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
                                                                     Long defendantAccountPartyId) {
        log.debug(":getDefendantAccountParty: Opal mode: accountId={}, partyId={}", defendantAccountId,
            defendantAccountPartyId);

        // Find the DefendantAccountEntity by ID
        DefendantAccountEntity account = defendantAccountRepository
            .findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));

        // Find the DefendantAccountPartiesEntity by Party ID
        DefendantAccountPartiesEntity party = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(defendantAccountPartyId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Party not found for accountId=" + defendantAccountId
                    + ", partyId=" + defendantAccountPartyId));

        // Map entity to PartyDetails DTO
        DefendantAccountParty defendantAccountParty = mapDefendantAccountParty(party);

        return GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(defendantAccountParty)
            .build();

    }

    private DefendantAccountParty mapDefendantAccountParty(DefendantAccountPartiesEntity partyEntity) {
        PartyEntity party = partyEntity.getParty();
        DebtorDetailEntity debtorDetail = debtorDetailRepository.findByPartyId(party.getPartyId());

        String defendantAccountPartyType = partyEntity.getAssociationType();
        Boolean isDebtor = partyEntity.getDebtor();

        // ----- Individual details (person) -----
        IndividualDetails individualDetails = null;
        if (!party.isOrganisation()) {
            individualDetails = IndividualDetails.builder()
                .title(party.getTitle())
                .forenames(party.getForenames())
                .surname(party.getSurname())
                .dateOfBirth(party.getBirthDate() != null ? party.getBirthDate().toString() : null)
                .age(party.getAge() != null ? String.valueOf(party.getAge()) : null)
                .nationalInsuranceNumber(party.getNiNumber())
                .individualAliases(null)
                .build();
        }

        // ----- Organisation details (organisation) -----
        OrganisationDetails organisationDetails = party.isOrganisation()
            ? OrganisationDetails.builder()
            .organisationName(party.getOrganisationName())
            .organisationAliases(null)
            .build()
            : null;

        // ----- Party details -----
        PartyDetails partyDetails = PartyDetails.builder()
            .partyId(String.valueOf(party.getPartyId()))
            .organisationFlag(party.isOrganisation())
            .organisationDetails(organisationDetails)
            .individualDetails(individualDetails)
            .build();

        // ----- Address: build only if at least one field is present -----
        String a1 = party.getAddressLine1();
        String a2 = party.getAddressLine2();
        String a3 = party.getAddressLine3();
        String a4 = party.getAddressLine4();
        String a5 = party.getAddressLine5();
        String pc = party.getPostcode();

        boolean hasAddress =
            (a1 != null && !a1.isBlank())
                || (a2 != null && !a2.isBlank())
                || (a3 != null && !a3.isBlank())
                || (a4 != null && !a4.isBlank())
                || (a5 != null && !a5.isBlank())
                || (pc != null && !pc.isBlank());

        AddressDetails address = hasAddress
            ? AddressDetails.builder()
            .addressLine1(a1)
            .addressLine2(a2)
            .addressLine3(a3)
            .addressLine4(a4)
            .addressLine5(a5)
            .postcode(pc)
            .build()
            : null;

        // ----- Contact details: null if all fields are blank/missing -----
        boolean hasContact =
            (party.getPrimaryEmailAddress() != null && !party.getPrimaryEmailAddress().isBlank())
                || (party.getSecondaryEmailAddress() != null && !party.getSecondaryEmailAddress().isBlank())
                || (party.getMobileTelephoneNumber() != null && !party.getMobileTelephoneNumber().isBlank())
                || (party.getHomeTelephoneNumber() != null && !party.getHomeTelephoneNumber().isBlank())
                || (party.getWorkTelephoneNumber() != null && !party.getWorkTelephoneNumber().isBlank());

        ContactDetails contactDetails = hasContact
            ? ContactDetails.builder()
            .primaryEmailAddress(party.getPrimaryEmailAddress())
            .secondaryEmailAddress(party.getSecondaryEmailAddress())
            .mobileTelephoneNumber(party.getMobileTelephoneNumber())
            .homeTelephoneNumber(party.getHomeTelephoneNumber())
            .workTelephoneNumber(party.getWorkTelephoneNumber())
            .build()
            : null;

        // ----- Vehicle details: null if both fields are empty -----
        String vehicleMake = debtorDetail != null ? debtorDetail.getVehicleMake() : null;
        String vehicleReg  = debtorDetail != null ? debtorDetail.getVehicleRegistration() : null;
        VehicleDetails vehicleDetails = (vehicleMake != null || vehicleReg != null)
            ? VehicleDetails.builder()
            .vehicleMakeAndModel(vehicleMake)
            .vehicleRegistration(vehicleReg)
            .build()
            : null;

        // --- Employer details: null if entirely empty; if present, employer_address line_1 must be non-null string ---
        String empName  = debtorDetail != null ? debtorDetail.getEmployerName() : null;
        String empRef   = debtorDetail != null ? debtorDetail.getEmployeeReference() : null;
        String empEmail = debtorDetail != null ? debtorDetail.getEmployerEmail() : null;
        String empPhone = debtorDetail != null ? debtorDetail.getEmployerTelephone() : null;
        String empA1    = debtorDetail != null ? debtorDetail.getEmployerAddressLine1() : null;
        String empA2    = debtorDetail != null ? debtorDetail.getEmployerAddressLine2() : null;
        String empA3    = debtorDetail != null ? debtorDetail.getEmployerAddressLine3() : null;
        String empA4    = debtorDetail != null ? debtorDetail.getEmployerAddressLine4() : null;
        String empA5    = debtorDetail != null ? debtorDetail.getEmployerAddressLine5() : null;
        String empPost  = debtorDetail != null ? debtorDetail.getEmployerPostcode() : null;

        boolean hasEmployer =
            (empName != null && !empName.isBlank())
                || (empRef != null && !empRef.isBlank())
                || (empEmail != null && !empEmail.isBlank())
                || (empPhone != null && !empPhone.isBlank())
                || (empA1 != null && !empA1.isBlank())
                || (empA2 != null && !empA2.isBlank())
                || (empA3 != null && !empA3.isBlank())
                || (empA4 != null && !empA4.isBlank())
                || (empA5 != null && !empA5.isBlank())
                || (empPost != null && !empPost.isBlank());

        EmployerDetails employerDetails = hasEmployer
            ? EmployerDetails.builder()
            .employerName(empName)
            .employerReference(empRef)
            .employerEmailAddress(empEmail)
            .employerTelephoneNumber(empPhone)
            .employerAddress(
                AddressDetails.builder()
                    .addressLine1(empA1)
                    .addressLine2(empA2)
                    .addressLine3(empA3)
                    .addressLine4(empA4)
                    .addressLine5(empA5)
                    .postcode(empPost)
                    .build()
            )
            .build()
            : null;

        // ----- Language preferences: null if both codes are empty -----
        String docLang  = debtorDetail != null ? debtorDetail.getDocumentLanguage() : null;
        String hearLang = debtorDetail != null ? debtorDetail.getHearingLanguage() : null;
        LanguagePreferences languagePreferences =
            (docLang != null || hearLang != null)
                ? LanguagePreferences.builder()
                .documentLanguagePreference(
                    docLang != null
                        ? LanguagePreferences.LanguagePreference.builder()
                        .languageCode(docLang)
                        .languageDisplayName(null)
                        .build()
                        : null
                )
                .hearingLanguagePreference(
                    hearLang != null
                        ? LanguagePreferences.LanguagePreference.builder()
                        .languageCode(hearLang)
                        .languageDisplayName(null)
                        .build()
                        : null
                )
                .build()
                : null;

        return DefendantAccountParty.builder()
            .defendantAccountPartyType(defendantAccountPartyType)
            .isDebtor(isDebtor)
            .partyDetails(partyDetails)
            .address(address)
            .contactDetails(contactDetails)
            .vehicleDetails(vehicleDetails)
            .employerDetails(employerDetails)
            .languagePreferences(languagePreferences)
            .build();
    }


    private static GetDefendantAccountPaymentTermsResponse toPaymentTermsResponse(PaymentTermsEntity entity) {
        if (entity == null) {
            return null;
        }

        DefendantAccountEntity account = entity.getDefendantAccount();

        PaymentTerms paymentTerms = PaymentTerms.builder()
            .daysInDefault(entity.getJailDays())
            .dateDaysInDefaultImposed(account.getSuspendedCommittalDate())
            .reasonForExtension(entity.getReasonForExtension())
            .paymentTermsType(
                PaymentTermsType.builder()
                    .paymentTermsTypeCode(
                        safePaymentTermsTypeCode(entity.getTermsTypeCode())
                    )
                    .build()
            )
            .effectiveDate(entity.getEffectiveDate())
            .instalmentPeriod(
                InstalmentPeriod.builder()
                    .instalmentPeriodCode(
                        safeInstalmentPeriodCode(entity.getInstalmentPeriod())
                    )
                    .build()
            )
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .build();

        PostedDetails postedDetails = PostedDetails.builder()
            .postedDate(entity.getPostedDate())
            .postedBy(entity.getPostedBy())
            .postedByName(entity.getPostedByUsername())
            .build();

        return GetDefendantAccountPaymentTermsResponse.builder()
            .paymentTerms(paymentTerms)
            .postedDetails(postedDetails)
            .paymentCardLastRequested(account.getPaymentCardRequestedDate())
            .dateLastAmended(account.getLastChangedDate())
            .extension(entity.getExtension())
            .lastEnforcement(account.getLastEnforcement())
            .build();
    }

    private static PaymentTermsType.PaymentTermsTypeCode safePaymentTermsTypeCode(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return PaymentTermsType.PaymentTermsTypeCode.fromValue(dbValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static InstalmentPeriod.InstalmentPeriodCode safeInstalmentPeriodCode(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return InstalmentPeriod.InstalmentPeriodCode.fromValue(dbValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }


}
