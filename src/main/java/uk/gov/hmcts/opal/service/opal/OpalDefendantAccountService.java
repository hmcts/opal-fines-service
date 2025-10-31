package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
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
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.EnforcementOverrideResult;
import uk.gov.hmcts.opal.dto.common.EnforcementStatusSummary;
import uk.gov.hmcts.opal.dto.common.Enforcer;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.LJA;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.LastEnforcementAction;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsSummary;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AliasDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementOverrideResultRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.AliasSpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.util.DateTimeUtils;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountHeaderViewRepository repository;

    private final DefendantAccountRepository defendantAccountRepository;

    private final SearchDefendantAccountRepository searchDefendantAccountRepository;

    private final SearchDefendantAccountSpecs searchDefendantAccountSpecs;

    private final DefendantAccountPaymentTermsRepository defendantAccountPaymentTermsRepository;

    private final DefendantAccountSummaryViewRepository defendantAccountSummaryViewRepository;

    private final CourtRepository courtRepository;

    private final AmendmentService amendmentService;

    private final EntityManager em;

    private final NoteRepository noteRepository;

    private final EnforcementOverrideResultRepository enforcementOverrideResultRepository;

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private final EnforcerRepository enforcerRepository;

    private CommentsAndNotes commentAndNotes;

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;


    @Autowired
    private DebtorDetailRepository debtorDetailRepository;

    @Autowired
    private AliasRepository aliasRepository;


    public DefendantAccountEntity getDefendantAccountById(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));
    }

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
            .defendantAccountId(
                e.getDefendantAccountId() != null ? e.getDefendantAccountId().toString() : null
            )
            .defendantAccountPartyId(
                e.getDefendantAccountPartyId() != null ? e.getDefendantAccountPartyId().toString() : null
            )
            .debtorType(
                e.getDebtorType() != null
                    ? e.getDebtorType()
                    : (Boolean.TRUE.equals(e.getHasParentGuardian()) ? "Parent/Guardian" : "Defendant")
            )
            .isYouth(
                e.getBirthDate() != null
                    ? java.time.Period.between(e.getBirthDate(), java.time.LocalDate.now()).getYears() < 18
                    : Boolean.FALSE
            )
            .parentGuardianPartyId(Optional.ofNullable(e.getParentGuardianAccountPartyId())
                                       .map(Object::toString).orElse(null))
            .accountNumber(e.getAccountNumber())
            .accountType(normaliseAccountType(e.getAccountType()))
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .fixedPenaltyTicketNumber(e.getFixedPenaltyTicketNumber())
            .accountStatusReference(buildAccountStatusReference(e.getAccountStatus()))
            .businessUnitSummary(buildBusinessUnitSummary(e))
            .paymentStateSummary(buildPaymentStateSummary(e))
            .partyDetails(buildPartyDetails(e))
            .version(e.getVersion())
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

    private static Integer safeInt(Long v) {
        if (v == null) {
            return null;
        }
        if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
            // Optional: log a warning here if you want visibility
            return null; // drop it rather than overflow
        }
        return v.intValue();
    }

    PartyDetails buildPartyDetails(DefendantAccountHeaderViewEntity e) {
        boolean isOrganisation = Boolean.TRUE.equals(e.getOrganisation());

        return PartyDetails.builder()
            .partyId(
                e.getPartyId() != null ? e.getPartyId().toString() : null
            )
            .organisationFlag(e.getOrganisation())

            .organisationDetails(
                isOrganisation
                    ? OrganisationDetails.builder()
                    .organisationName(e.getOrganisationName())
                    .organisationAliases(null)
                    .build()
                    : null
            )

            .individualDetails(
                !isOrganisation
                    ? IndividualDetails.builder()
                    .title(e.getTitle())
                    .forenames(e.getFirstnames())
                    .surname(e.getSurname())
                    .dateOfBirth(e.getBirthDate() != null ? e.getBirthDate().toString() : null)
                    .age(e.getBirthDate() != null ? String.valueOf(calculateAge(e.getBirthDate())) : null)
                    .nationalInsuranceNumber(null)
                    .individualAliases(null)
                    .build()
                    : null
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

    static int calculateAge(LocalDate birthDate) {
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


        boolean hasRef =
            Optional.ofNullable(accountSearchDto.getReferenceNumberDto())
                .map(r ->
                         (r.getAccountNumber() != null && !r.getAccountNumber().isBlank())
                             || (r.getProsecutorCaseReference() != null && !r.getProsecutorCaseReference().isBlank())
                )
                .orElse(false);

        boolean applyActiveOnly =
            Boolean.TRUE.equals(accountSearchDto.getActiveAccountsOnly()) && !hasRef; // ← AC1b: ignore when ref present

        Specification<SearchDefendantAccountEntity> spec =
            searchDefendantAccountSpecs.filterByBusinessUnits(accountSearchDto.getBusinessUnitIds())
                .and(searchDefendantAccountSpecs.filterByActiveOnly(applyActiveOnly))
                .and(searchDefendantAccountSpecs.filterByAccountNumberStartsWithWithCheckLetter(accountSearchDto))
                .and(searchDefendantAccountSpecs.filterByPcrExact(accountSearchDto))
                .and(searchDefendantAccountSpecs.filterByReferenceOrganisationFlag(accountSearchDto))
                .and(
                    accountSearchDto.getDefendant() != null
                        && Boolean.TRUE.equals(accountSearchDto.getDefendant().getOrganisation())
                        ? searchDefendantAccountSpecs.filterByAliasesIfRequested(accountSearchDto)
                        : searchDefendantAccountSpecs.filterByNameIncludingAliases(accountSearchDto)
                )
                .and(searchDefendantAccountSpecs.filterByDobStartsWith(accountSearchDto))
                .and(searchDefendantAccountSpecs.filterByNiStartsWith(accountSearchDto))
                .and(searchDefendantAccountSpecs.filterByAddress1StartsWith(accountSearchDto))
                .and(searchDefendantAccountSpecs.filterByPostcodeStartsWith(accountSearchDto));


        List<SearchDefendantAccountEntity> rows = searchDefendantAccountRepository.findAll(spec);

        List<DefendantAccountSummaryDto> summaries = new ArrayList<>(rows.size());
        for (SearchDefendantAccountEntity e : rows) {
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
            .orElseThrow(() -> new EntityNotFoundException("Payment Terms not found for Defendant Account Id: "
                                                               + defendantAccountId));

        return toPaymentTermsResponse(entity);
    }

    @Override
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Fixed Penalty Offence not found for account: " + defendantAccountId));

        return toFixedPenaltyResponse(account, offence);
    }


    private DefendantAccountSummaryDto toSummaryDto(SearchDefendantAccountEntity e) {
        boolean isOrganisation = Boolean.TRUE.equals(e.getOrganisation());

        // Build aliases from flattened alias1..alias5 columns on the view
        List<AliasDto> aliases = new ArrayList<>();
        String[] aliasValues = { e.getAlias1(), e.getAlias2(), e.getAlias3(), e.getAlias4(), e.getAlias5() };

        for (int i = 0; i < aliasValues.length; i++) {
            String value = aliasValues[i];
            if (value == null || value.isBlank()) {
                continue;
            }
            AliasDto.AliasDtoBuilder b = AliasDto.builder().aliasNumber(i + 1);

            if (isOrganisation) {
                // For organisations the view gives each alias as an organisation name
                b.organisationName(value);
            } else {
                // For individuals the view gives "forenames surname" – split on the last space
                String trimmed = value.trim();
                int cut = trimmed.lastIndexOf(' ');
                if (cut > 0) {
                    b.forenames(trimmed.substring(0, cut).trim())
                        .surname(trimmed.substring(cut + 1).trim());
                } else {
                    // No space: treat the whole thing as a surname
                    b.surname(trimmed);
                }
            }

            aliases.add(b.build());
        }

        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(e.getDefendantAccountId()))
            .accountNumber(e.getAccountNumber())
            .organisation(isOrganisation)
            .organisationName(isOrganisation ? e.getOrganisationName() : null)
            .defendantTitle(isOrganisation ? null : e.getTitle())
            .defendantFirstnames(isOrganisation ? null : e.getForenames())
            .defendantSurname(isOrganisation ? null : e.getSurname())
            .addressLine1(orEmpty(e.getAddressLine1()))
            .postcode(e.getPostcode())
            .businessUnitName(e.getBusinessUnitName())
            .businessUnitId(String.valueOf(e.getBusinessUnitId()))
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .lastEnforcementAction(e.getLastEnforcement()) // entity now maps this as String
            .accountBalance(e.getDefendantAccountBalance())
            .birthDate(e.getBirthDate() != null ? e.getBirthDate().toString() : null)
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
            .version(account.getVersion())
            .build();

    }

    private DefendantAccountParty mapDefendantAccountParty(
        DefendantAccountPartiesEntity partyEntity
    ) {
        PartyEntity party = partyEntity.getParty();
        DebtorDetailEntity debtorDetail = debtorDetailRepository.findByPartyId(party.getPartyId());

        String defendantAccountPartyType = partyEntity.getAssociationType();
        Boolean isDebtor = partyEntity.getDebtor();

        PartyDetails partyDetails = PartyDetails.builder()
            .partyId(String.valueOf(party.getPartyId()))
            .organisationFlag(party.isOrganisation())
            .organisationDetails(
                party.isOrganisation()
                    ? OrganisationDetails.builder()
                    .organisationName(party.getOrganisationName())
                    .organisationAliases(null)
                    .build()
                    : null
            )
            .individualDetails(
                !party.isOrganisation()
                    ? IndividualDetails.builder()
                    .title(party.getTitle())
                    .forenames(party.getForenames())
                    .surname(party.getSurname())
                    .dateOfBirth(party.getBirthDate() != null ? party.getBirthDate().toString() : null)
                    .age(party.getAge() != null ? String.valueOf(party.getAge()) : null)
                    .nationalInsuranceNumber(party.getNiNumber())
                    .individualAliases(null)
                    .build()
                    : null
            )
            .build();

        AddressDetails address = AddressDetails.builder()
            .addressLine1(party.getAddressLine1())
            .addressLine2(party.getAddressLine2())
            .addressLine3(party.getAddressLine3())
            .addressLine4(party.getAddressLine4())
            .addressLine5(party.getAddressLine5())
            .postcode(party.getPostcode())
            .build();

        ContactDetails contactDetails = ContactDetails.builder()
            .primaryEmailAddress(party.getPrimaryEmailAddress())
            .secondaryEmailAddress(party.getSecondaryEmailAddress())
            .mobileTelephoneNumber(party.getMobileTelephoneNumber())
            .homeTelephoneNumber(party.getHomeTelephoneNumber())
            .workTelephoneNumber(party.getWorkTelephoneNumber())
            .build();


        VehicleDetails vehicleDetails = VehicleDetails.builder()
            .vehicleMakeAndModel(debtorDetail != null ? debtorDetail.getVehicleMake() : null)
            .vehicleRegistration(debtorDetail != null ? debtorDetail.getVehicleRegistration() : null)
            .build();

        AddressDetails employerAddress = AddressDetails.builder()
            .addressLine1(debtorDetail != null ? debtorDetail.getEmployerAddressLine1() : "")
            .addressLine2(debtorDetail != null ? debtorDetail.getEmployerAddressLine2() : null)
            .addressLine3(debtorDetail != null ? debtorDetail.getEmployerAddressLine3() : null)
            .addressLine4(debtorDetail != null ? debtorDetail.getEmployerAddressLine4() : null)
            .addressLine5(debtorDetail != null ? debtorDetail.getEmployerAddressLine5() : null)
            .postcode(debtorDetail != null ? debtorDetail.getEmployerPostcode() : null)
            .build();

        EmployerDetails employerDetails = EmployerDetails.builder()
            .employerName(debtorDetail != null ? debtorDetail.getEmployerName() : null)
            .employerReference(debtorDetail != null ? debtorDetail.getEmployeeReference() : null)
            .employerEmailAddress(debtorDetail != null ? debtorDetail.getEmployerEmail() : null)
            .employerTelephoneNumber(debtorDetail != null ? debtorDetail.getEmployerTelephone() : null)
            .employerAddress(employerAddress)
            .build();

        LanguagePreference documentLanguagePref =
            LanguagePreference.fromCode(debtorDetail != null ? debtorDetail.getDocumentLanguage() : null);

        LanguagePreference hearingLanguagePref =
            LanguagePreference.fromCode(debtorDetail != null ? debtorDetail.getHearingLanguage() : null);

        LanguagePreferences languagePreferences = LanguagePreferences.builder()
            .documentLanguagePreference(documentLanguagePref)
            .hearingLanguagePreference(hearingLanguagePref)
            .build();

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

    private List<AliasEntity> getAliasesForDefendantAccount(Long defendantAccountId) {
        return aliasRepository.findAll(AliasSpecs.byDefendantAccountId(defendantAccountId));
    }

    private static GetDefendantAccountPaymentTermsResponse toPaymentTermsResponse(PaymentTermsEntity entity) {
        if (entity == null) {
            return null;
        }

        DefendantAccountEntity account = entity.getDefendantAccount();

        PaymentTerms paymentTerms = PaymentTerms.builder()
            .daysInDefault(entity.getJailDays())
            .dateDaysInDefaultImposed(account.getSuspendedCommittalDate())
            .extension(entity.getExtension())
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
                    .instalmentPeriodCode(safeInstalmentPeriodCode(entity.getInstalmentPeriod())
                )
                    .build()
            )
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .postedDetails(PostedDetails.builder()
                               .postedDate(entity.getPostedDate())
                               .postedBy(entity.getPostedBy())
                               .postedByName(entity.getPostedByUsername())
                               .build())
            .build();

        return GetDefendantAccountPaymentTermsResponse.builder()
            .paymentTerms(paymentTerms)
            .paymentCardLastRequested(account.getPaymentCardRequestedDate())
            .lastEnforcement(account.getLastEnforcement())
            .build();
    }

    private static GetDefendantAccountFixedPenaltyResponse toFixedPenaltyResponse(
        DefendantAccountEntity account, FixedPenaltyOffenceEntity offence) {

        boolean isVehicle =
            offence.getVehicleRegistration() != null
                && !"NV".equalsIgnoreCase(offence.getVehicleRegistration());

        FixedPenaltyTicketDetails ticketDetails = FixedPenaltyTicketDetails.builder()
            .issuingAuthority(account.getOriginatorName())
            .ticketNumber(offence.getTicketNumber())
            .timeOfOffence(offence.getTimeOfOffence())
            .placeOfOffence(offence.getOffenceLocation())
            .build();

        VehicleFixedPenaltyDetails vehicleDetails = isVehicle
            ? VehicleFixedPenaltyDetails.builder()
            .vehicleRegistrationNumber(offence.getVehicleRegistration())
            .vehicleDriversLicense(offence.getLicenceNumber())
            .noticeNumber(offence.getNoticeNumber())
            .dateNoticeIssued(DateTimeUtils.toString(offence.getIssuedDate()))
            .build()
            : null;

        return GetDefendantAccountFixedPenaltyResponse.builder()
            .vehicleFixedPenaltyFlag(isVehicle)
            .fixedPenaltyTicketDetails(ticketDetails)
            .vehicleFixedPenaltyDetails(vehicleDetails)
            .version(String.valueOf(account.getVersion()))
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

    public DefendantAccountSummaryViewEntity getDefendantAccountSummaryViewById(long defendantAccountId) {
        return defendantAccountSummaryViewRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Summary View not found with id: " + defendantAccountId));
    }

    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.debug(":getAtAGlance (Opal): id: {}.", defendantAccountId);

        // fetch DefendantAccountAtAGlance data from the view.
        DefendantAccountSummaryViewEntity entity = getDefendantAccountSummaryViewById(defendantAccountId);

        return convertEntityToAtAGlanceResponse(entity);
    }

    static DefendantAccountAtAGlanceResponse
        convertEntityToAtAGlanceResponse(DefendantAccountSummaryViewEntity entity) {
        if (null == entity) {
            return null;
        }

        PartyDetails partyDetails = PartyDetails.builder()
            .partyId(entity.getPartyId() != null ? entity.getPartyId().toString() : null)
            .organisationFlag(entity.getOrganisation())
            // Only one of organisationDetails or individualDetails will be populated
            // if organisationFlag is true, then organisationDetails is populated
            .organisationDetails(
                entity.getOrganisation()
                    ? buildOrganisationDetails(entity)
                    : null
            )
            // if organisationFlag is false, then individualDetails is populated
            .individualDetails(
                !entity.getOrganisation()
                    ? buildIndividualDetails(entity)
                    : null
            )
            .build();

        AddressDetails addressDetails = AddressDetails.builder()
            .addressLine1(entity.getAddressLine1())
            .addressLine2(entity.getAddressLine2())
            .addressLine3(entity.getAddressLine3())
            .addressLine4(entity.getAddressLine4())
            .addressLine5(entity.getAddressLine5())
            .postcode(entity.getPostcode())
            .build();

        return DefendantAccountAtAGlanceResponse.builder()
            .defendantAccountId(entity.getDefendantAccountId().toString())
            .accountNumber(entity.getAccountNumber())
            .debtorType(entity.getDebtorType())
            .isYouth(isYouth(entity.getBirthDate(), entity.getAge()))
            .partyDetails(partyDetails)
            .addressDetails(addressDetails)
            .languagePreferences(buildLanguagePreferences(entity))
            .paymentTermsSummary(buildPaymentTerms(entity))
            .enforcementStatus(buildEnforcementStatusSummary(entity))
            .commentsAndNotes(buildCommentsAndNotes(entity))
            .version(entity.getVersion())
            .build();
    }

    private static record ParsedAlias(
        String aliasId,
        Integer sequenceNumber,
        String forenames,
        String surname,
        String organisationName
    ) {}

    /** Split a full name into forenames + surname (surname = last token). */
    private static String[] splitForenamesSurname(String fullName) {
        if (fullName == null) {
            return new String[] { null, null };
        }
        String s = fullName.trim().replaceAll("\\s+", " ");
        int idx = s.lastIndexOf(' ');
        if (idx < 0) {
            // Single token — treat as forename only (no surname)
            return new String[] { emptyToNull(s), null };
        }
        String forenames = emptyToNull(s.substring(0, idx));
        String surname   = emptyToNull(s.substring(idx + 1));
        return new String[] { forenames, surname };
    }

    // ---- public-ish helpers your builders can call ----
    static List<IndividualAlias> buildIndividualAliasesList(DefendantAccountSummaryViewEntity e) {
        // If the entity is an organisation, there should be no individual aliases to emit.
        if (Boolean.TRUE.equals(e.getOrganisation())) {
            return List.of();
        }

        return streamAliasSlots(e)
            .map(raw -> parseAliasRaw(raw, /* isOrganisation= */ false)) // Optional<ParsedAlias>
            .flatMap(Optional::stream)
            .map(pa -> IndividualAlias.builder()
                .aliasId(pa.aliasId())
                .sequenceNumber(pa.sequenceNumber())
                .forenames(pa.forenames())
                .surname(pa.surname())
                .build())
            .toList();
    }

    static List<OrganisationAlias> buildOrganisationAliasesList(DefendantAccountSummaryViewEntity e) {
        // If the entity is an individual, there should be no organisation aliases to emit.
        if (!Boolean.TRUE.equals(e.getOrganisation())) {
            return List.of();
        }

        return streamAliasSlots(e)
            .map(raw -> parseAliasRaw(raw, /* isOrganisation= */ true)) // Optional<ParsedAlias>
            .flatMap(Optional::stream)
            .map(pa -> OrganisationAlias.builder()
                .aliasId(pa.aliasId())
                .sequenceNumber(pa.sequenceNumber())
                .organisationName(pa.organisationName())
                .build())
            .toList();
    }

    // ---- core parsing (shared) ----
    private static Stream<String> streamAliasSlots(DefendantAccountSummaryViewEntity e) {
        return Stream.of(e.getAlias1(), e.getAlias2(), e.getAlias3(), e.getAlias4(), e.getAlias5())
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty());
    }

    /**
     * Parse alias in the new unified shape: id|seq|name
     * Use isOrganisation to decide whether "name" is an organisation name or a personal full name.
     */
    private static Optional<ParsedAlias> parseAliasRaw(String raw, boolean isOrganisation) {
        String[] parts = Arrays.stream(raw.split("\\|", -1))
            .map(p -> p == null ? null : p.trim())
            .toArray(String[]::new);

        try {
            if (parts.length != 3) {
                return Optional.empty(); // unexpected shape
            }

            String aliasId = emptyToNull(parts[0]);
            Integer seq    = parseIntOrNull(parts[1]);
            String name    = emptyToNull(parts[2]);

            if (isOrganisation) {
                return Optional.of(new ParsedAlias(
                    aliasId,
                    seq,
                    null,
                    null,
                    name
                ));
            } else {
                String[] split = splitForenamesSurname(name);
                return Optional.of(new ParsedAlias(
                    aliasId,
                    seq,
                    split[0],
                    split[1],
                    null
                ));
            }
        } catch (Exception ex) {
            return Optional.empty(); // malformed data
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        return Integer.valueOf(s);
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ---- unchanged ----
    private static IndividualDetails buildIndividualDetails(DefendantAccountSummaryViewEntity e) {
        return IndividualDetails.builder()
            .title(e.getTitle())
            .forenames(e.getForenames())
            .surname(e.getSurname())
            .dateOfBirth(e.getBirthDate() != null ? e.getBirthDate().toLocalDate().toString() : null)
            .age(e.getBirthDate() != null ? String.valueOf(calculateAge(e.getBirthDate().toLocalDate())) : null)
            .individualAliases(buildIndividualAliasesList(e))
            .nationalInsuranceNumber(e.getNationalInsuranceNumber())
            .build();
    }

    private static OrganisationDetails buildOrganisationDetails(DefendantAccountSummaryViewEntity e) {
        return OrganisationDetails.builder()
            .organisationName(e.getOrganisationName())
            .organisationAliases(buildOrganisationAliasesList(e))
            .build();
    }

    private static EnforcementStatusSummary buildEnforcementStatusSummary(DefendantAccountSummaryViewEntity entity) {
        LastEnforcementAction lastEnforcementAction = LastEnforcementAction.builder()
            .lastEnforcementActionId(entity.getLastEnforcement())
            .lastEnforcementActionTitle(entity.getLastEnforcementTitle())
            .build();

        EnforcementOverrideResult enforcementOverrideResult = EnforcementOverrideResult.builder()
            .enforcementOverrideId(entity.getEnforcementOverrideResultId())
            .enforcementOverrideTitle(entity.getEnforcementOverrideTitle())
            .build();

        Integer enforcerId = safeInt(entity.getEnforcerId());
        Enforcer enforcer = (enforcerId != null)
            ? Enforcer.builder()
            .enforcerId(enforcerId)
            .enforcerName(entity.getEnforcerName())
            .build()
            : null;

        LJA lja = LJA.builder()
            .ljaId(null == entity.getLjaId() ? null : Integer.parseInt(entity.getLjaId()))
            .ljaName(entity.getLjaName())
            .build();

        EnforcementOverride enforcementOverride = EnforcementOverride.builder()
            .enforcementOverrideResult(enforcementOverrideResult)
            .enforcer(enforcer)
            .lja(lja)
            .build();

        return EnforcementStatusSummary.builder()
            .lastEnforcementAction(lastEnforcementAction)
            .collectionOrderMade(entity.getCollectionOrder())
            .defaultDaysInJail(entity.getJailDays())
            .enforcementOverride(enforcementOverride)
            .lastMovementDate(entity.getLastMovementDate().toLocalDate())
            .build();
    }

    private static PaymentTermsSummary buildPaymentTerms(DefendantAccountSummaryViewEntity entity) {
        return PaymentTermsSummary.builder()
            .paymentTermsType(
                PaymentTermsType.builder()
                    .paymentTermsTypeCode(safePaymentTermsTypeCode(entity.getTermsTypeCode()))
                    .build()
            )
            .effectiveDate(null == entity.getEffectiveDate() ? null : entity.getEffectiveDate().toLocalDate())
            .instalmentPeriod(
                InstalmentPeriod.builder()
                    .instalmentPeriodCode(safeInstalmentPeriodCode(entity.getInstalmentPeriod()))
                    .build()
            )
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .build();
    }

    private static LanguagePreferences buildLanguagePreferences(DefendantAccountSummaryViewEntity entity) {
        // if both language preferences are not set, as they are optional objects.
        if ((null == entity.getDocumentLanguage()) && (null == entity.getHearingLanguage())) {
            return null;
        }

        LanguagePreference documentLanguagePref =
            null == entity.getDocumentLanguage() ? null : LanguagePreference.fromCode(entity.getDocumentLanguage());

        LanguagePreference hearingLanguagePref =
            null == entity.getHearingLanguage() ? null : LanguagePreference.fromCode(entity.getHearingLanguage());

        return LanguagePreferences.builder()
            .documentLanguagePreference(documentLanguagePref)
            .hearingLanguagePreference(hearingLanguagePref)
            .build();
    }

    private static CommentsAndNotes buildCommentsAndNotes(DefendantAccountSummaryViewEntity entity) {
        // Return null if all fields don't have values, as they are optional objects.
        if ((null == entity.getAccountComments()) && (null == entity.getAccountNote1())
            && (null == entity.getAccountNote2()) && (null == entity.getAccountNote3())) {
            return null;
        }

        return CommentsAndNotes.builder()
            .accountNotesAccountComments(entity.getAccountComments())
            .accountNotesFreeTextNote1(entity.getAccountNote1())
            .accountNotesFreeTextNote2(entity.getAccountNote2())
            .accountNotesFreeTextNote3(entity.getAccountNote3())
            .build();
    }

    @Override
    @Transactional
    public DefendantAccountResponse updateDefendantAccount(
        Long defendantAccountId,
        String businessUnitId,
        UpdateDefendantAccountRequest request,
        String ifMatch,
        String postedBy
    ) {
        log.debug(":updateDefendantAccount (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        if (request.getCommentsAndNotes() == null
            && request.getEnforcementCourt() == null
            && request.getCollectionOrder() == null
            && request.getEnforcementOverride() == null) {
            throw new IllegalArgumentException("At least one update group must be provided");
        }

        DefendantAccountEntity entity = defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));

        if (entity.getBusinessUnit() == null
            || entity.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(entity.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit "
                + businessUnitId);
        }

        VersionUtils.verifyIfMatch(entity, ifMatch, defendantAccountId, "updateDefendantAccount");

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        if (request.getCommentsAndNotes() != null) {
            applyCommentAndNotes(entity, request.getCommentsAndNotes(), postedBy);
        }
        if (request.getEnforcementCourt() != null) {
            applyEnforcementCourt(entity, request.getEnforcementCourt());
        }
        if (request.getCollectionOrder() != null) {
            applyCollectionOrder(entity, request.getCollectionOrder());
        }
        if (request.getEnforcementOverride() != null) {
            applyEnforcementOverride(entity, request.getEnforcementOverride());
        }

        defendantAccountRepository.save(entity);

        em.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush();
        Long newVersion = entity.getVersion();

        Short buId = entity.getBusinessUnit().getBusinessUnitId();
        amendmentService.auditFinaliseStoredProc(
            defendantAccountId,
            RecordType.DEFENDANT_ACCOUNTS,
            buId,
            postedBy,
            entity.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        // ---- Build response ----
        CourtReferenceDto courtDto = Optional.ofNullable(entity.getEnforcingCourt())
            .filter(c -> safeInt(c.getCourtId()) != null)
            .map(c -> CourtReferenceDto.builder()
                .courtId(safeInt(c.getCourtId()))
                .courtName(c.getName())
                .build())
            .orElse(null);

        CollectionOrderDto collectionOrderDto = CollectionOrderDto.builder()
            .collectionOrderFlag(entity.getCollectionOrder())
            .collectionOrderDate(entity.getCollectionOrderEffectiveDate() != null
                ? entity.getCollectionOrderEffectiveDate().toString()
                : null)
            .build();

        EnforcementOverride enforcementOverrideDto = null;
        if (entity.getEnforcementOverrideResultId() != null
            || entity.getEnforcementOverrideEnforcerId() != null
            || entity.getEnforcementOverrideTfoLjaId() != null) {

            enforcementOverrideDto = EnforcementOverride.builder()
                .enforcementOverrideResult(
                    Optional.ofNullable(entity.getEnforcementOverrideResultId())
                        .flatMap(enforcementOverrideResultRepository::findById) // Optional-friendly
                        .map(r -> EnforcementOverrideResult.builder()
                            .enforcementOverrideId(r.getEnforcementOverrideResultId())
                            .enforcementOverrideTitle(r.getEnforcementOverrideResultName())
                            .build())
                        .orElse(null)
                )
                .enforcer(
                    Optional.ofNullable(entity.getEnforcementOverrideEnforcerId())
                        .flatMap(enforcerRepository::findById)
                        .filter(enf -> safeInt(enf.getEnforcerId()) != null)
                        .map(enf -> Enforcer.builder()
                            .enforcerId(safeInt(enf.getEnforcerId()))
                            .enforcerName(enf.getName())
                            .build())
                        .orElse(null)
                )
                .lja(
                    Optional.ofNullable(entity.getEnforcementOverrideTfoLjaId())
                        .flatMap(localJusticeAreaRepository::findById)
                        .map(lja -> LJA.builder()
                            .ljaId(
                                lja.getLocalJusticeAreaId() != null
                                    ? lja.getLocalJusticeAreaId().intValue()
                                    : null
                            )
                            .ljaName(Optional.ofNullable(lja.getName()).orElse(lja.getLjaCode()))
                            .build())
                        .orElse(null)
                )

                .build();
        }

        CommentsAndNotes notesOut = Optional.ofNullable(request.getCommentsAndNotes())
            .map(in -> CommentsAndNotes.builder()
                .accountNotesAccountComments(orEmpty(in.getAccountNotesAccountComments()))
                .accountNotesFreeTextNote1(orEmpty(in.getAccountNotesFreeTextNote1()))
                .accountNotesFreeTextNote2(orEmpty(in.getAccountNotesFreeTextNote2()))
                .accountNotesFreeTextNote3(orEmpty(in.getAccountNotesFreeTextNote3()))
                .build())
            .orElse(null);

        return DefendantAccountResponse.builder()
            .id(entity.getDefendantAccountId())
            .commentsAndNotes(notesOut)
            .enforcementCourt(courtDto)
            .collectionOrder(collectionOrderDto)
            .enforcementOverride(enforcementOverrideDto)
            .version(newVersion)
            .build();
    }

    private static String orEmpty(String s) {
        return (s == null) ? "" : s;
    }


    /**
     * Determines if the individual is considered a youth (under 18 years old).
     *
     * <p>
     *     If the birth date is provided, it calculates the age based on the current date.
     *     If the birth date is not provided, the age parameter is used if available.
     *     If neither is available, it returns null.
     * </p>
     * @param birthDate The birth date of the individual.
     * @param age Age of the individual.
     * @return True if the individual is under 18, false otherwise.
     */
    private static Boolean isYouth(LocalDateTime birthDate, Integer age) {
        if (birthDate != null) {
            return calculateAge(birthDate.toLocalDate()) < 18;
        } else if (age != null) {
            return age < 18;
        } else {
            return Boolean.FALSE; // return FALSE if both are null
        }
    }


    private void applyCommentAndNotes(DefendantAccountEntity managed,
                                      CommentsAndNotes notes,
                                      String postedBy) {

        // Persist values on the main defendant_accounts table
        managed.setAccountComments(notes.getAccountNotesAccountComments());
        managed.setAccountNote1(notes.getAccountNotesFreeTextNote1());
        managed.setAccountNote2(notes.getAccountNotesFreeTextNote2());
        managed.setAccountNote3(notes.getAccountNotesFreeTextNote3());

        // Build a combined text block for the NOTES table (audit/history)
        final String combined = Stream.of(
                notes.getAccountNotesAccountComments(),
                notes.getAccountNotesFreeTextNote1(),
                notes.getAccountNotesFreeTextNote2(),
                notes.getAccountNotesFreeTextNote3()
            )
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining("\n"));

        if (combined.isEmpty()) {
            log.debug(":applyCommentAndNotes: nothing to add");
            return;
        }

        NoteEntity note = new NoteEntity();
        note.setNoteText(combined);
        note.setNoteType("AA");
        note.setAssociatedRecordId(String.valueOf(managed.getDefendantAccountId()));
        note.setAssociatedRecordType(RecordType.DEFENDANT_ACCOUNTS.toString());
        note.setBusinessUnitUserId(String.valueOf(managed.getBusinessUnit().getBusinessUnitId()));
        note.setPostedDate(LocalDateTime.now());
        note.setPostedByUsername(postedBy);

        noteRepository.save(note);
        log.debug(":applyCommentAndNotes: saved note for account {}", managed.getDefendantAccountId());
    }


    private void applyEnforcementCourt(DefendantAccountEntity entity, CourtReferenceDto courtRef) {
        Integer courtId = courtRef.getCourtId();
        if (courtId == null) {
            throw new IllegalArgumentException("enforcement_court.court_id is required");
        }
        CourtEntity court = courtRepository.findById(courtId.longValue())
            .orElseThrow(() -> new EntityNotFoundException("Court not found: " + courtId));
        entity.setEnforcingCourt(asLite(court));
        log.debug(":applyEnforcementCourt: accountId={}, courtId={}",
            entity.getDefendantAccountId(), court.getCourtId());
    }

    private CourtEntity.Lite asLite(CourtEntity court) {
        return CourtEntity.Lite.builder()
            .courtId(court.getCourtId())
            .businessUnitId(court.getBusinessUnitId())
            .courtCode(court.getCourtCode())
            .localJusticeAreaId(court.getLocalJusticeAreaId())
            .courtType(court.getCourtType())
            .division(court.getDivision())
            .name(court.getName())
            .build();
    }

    private void applyCollectionOrder(DefendantAccountEntity entity, CollectionOrderDto co) {
        if (co.getCollectionOrderFlag() == null || co.getCollectionOrderDate() == null) {
            throw new IllegalArgumentException("collection_order_flag and collection_order_date are required");
        }
        entity.setCollectionOrder(Boolean.TRUE.equals(co.getCollectionOrderFlag()));
        try {
            entity.setCollectionOrderEffectiveDate(LocalDate.parse(co.getCollectionOrderDate()));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("collection_order_date must be ISO date (yyyy-MM-dd)", ex);
        }
        log.debug(":applyCollectionOrder: accountId={}, flag={}, date={}",
            entity.getDefendantAccountId(), co.getCollectionOrderFlag(), co.getCollectionOrderDate());
    }

    private void applyEnforcementOverride(DefendantAccountEntity entity, EnforcementOverride override) {
        if (override.getEnforcementOverrideResult() != null) {
            entity.setEnforcementOverrideResultId(
                override.getEnforcementOverrideResult().getEnforcementOverrideId());
        }
        if (override.getEnforcer() != null && override.getEnforcer().getEnforcerId() != null) {
            entity.setEnforcementOverrideEnforcerId(override.getEnforcer().getEnforcerId().longValue());
        }
        if (override.getLja() != null && override.getLja().getLjaId() != null) {
            entity.setEnforcementOverrideTfoLjaId(override.getLja().getLjaId().shortValue());
        }
        log.debug(":applyEnforcementOverride: accountId={}, resultId={}, enforcerId={}, ljaId={}",
            entity.getDefendantAccountId(),
            override.getEnforcementOverrideResult() != null
                ? override.getEnforcementOverrideResult().getEnforcementOverrideId() : null,
            override.getEnforcer() != null ? override.getEnforcer().getEnforcerId() : null,
            override.getLja() != null ? override.getLja().getLjaId() : null);
    }

    private static String normaliseAccountType(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.trim()) {
            case "Fines", "Fine" -> "Fine";
            case "Conditional Caution" -> "Conditional Caution";
            case "Confiscation" -> "Confiscation";
            case "Fixed Penalty", "Fixed Penalty Registration" -> "Fixed Penalty";
            default -> raw;
        };
    }

}
