package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CommentAndNotesDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.EnforcementOverride;
import uk.gov.hmcts.opal.dto.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.EnforcerReference;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.LjaReference;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PaymentTermsType;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
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
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private final DefendantAccountHeaderViewRepository repository;

    private final DefendantAccountRepository defendantAccountRepository;

    private final DefendantAccountSpecs defendantAccountSpecs;
    private final DefendantAccountPaymentTermsRepository defendantAccountPaymentTermsRepository;

    private final CourtRepository courtRepository;

    private final AmendmentService amendmentService;

    private final EntityManager em;

    private final NoteRepository noteRepository;


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
            .defendantPartyId(Optional.ofNullable(e.getPartyId()).map(Object::toString).orElse(null))
            .parentGuardianPartyId(Optional.ofNullable(e.getParentGuardianAccountPartyId())
                                       .map(Object::toString).orElse(null))
            .accountNumber(e.getAccountNumber())
            .accountType(e.getAccountType())
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

        LanguagePreferences.LanguagePreference documentLanguagePref =
            LanguagePreferences.LanguagePreference.builder()
                .languageCode(debtorDetail != null ? debtorDetail.getDocumentLanguage() : null)
                .languageDisplayName(null)
                .build();

        LanguagePreferences.LanguagePreference hearingLanguagePref =
            LanguagePreferences.LanguagePreference.builder()
                .languageCode(debtorDetail != null ? debtorDetail.getHearingLanguage() : null)
                .languageDisplayName(null)
                .build();

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

        // Require at least one update group
        if (request.getCommentAndNotes() == null
            && request.getEnforcementCourt() == null
            && request.getCollectionOrder() == null
            && request.getEnforcementOverrides() == null) {
            throw new IllegalArgumentException("At least one update group must be provided");
        }

        // Load & guard BU
        DefendantAccountEntity entity = defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));
        if (entity.getBusinessUnit() == null
            || entity.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(entity.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }

        // ---- ETag / If-Match check against entity @Version --------------------------------------------
        VersionUtils.verifyIfMatch(entity, ifMatch, defendantAccountId, "updateDefendantAccount");
        // -----------------------------------------------------------------------------------------------

        // ---- Audit: initialise (same TX) --------------------------------------------------------------
        amendmentService.auditInitialiseStoredProc(defendantAccountId,
            uk.gov.hmcts.opal.entity.amendment.RecordType.DEFENDANT_ACCOUNTS);
        // -----------------------------------------------------------------------------------------------

        // Apply requested changes
        if (request.getCommentAndNotes() != null) {
            applyCommentAndNotes(entity, request.getCommentAndNotes(), postedBy);
        }
        if (request.getEnforcementCourt() != null) {
            applyEnforcementCourt(entity, request.getEnforcementCourt());
        }
        if (request.getCollectionOrder() != null) {
            applyCollectionOrder(entity, request.getCollectionOrder());
        }
        if (request.getEnforcementOverrides() != null) {
            applyEnforcementOverrides(entity, request.getEnforcementOverrides());
        }

        defendantAccountRepository.save(entity);

        // Force optimistic version increment and get the new version
        em.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush(); // ensure increment happens within this tx
        Long newVersion = entity.getVersion();

        // ---- Audit: finalise (same TX) ----------------------------------------------------------------
        Short buId = entity.getBusinessUnit().getBusinessUnitId();
        amendmentService.auditFinaliseStoredProc(
            defendantAccountId,
            uk.gov.hmcts.opal.entity.amendment.RecordType.DEFENDANT_ACCOUNTS,
            buId,
            postedBy,
            entity.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );
        // -----------------------------------------------------------------------------------------------

        // Build response
        var court = entity.getEnforcingCourt();
        var notes = request.getCommentAndNotes();

        CourtReferenceDto courtDto = (court != null)
            ? CourtReferenceDto.builder()
            .courtId(court.getCourtId() != null ? court.getCourtId().intValue() : null)
            .courtName(court.getName())
            .build()
            : null;

        CollectionOrderDto collectionOrderDto = CollectionOrderDto.builder()
            .collectionOrderFlag(entity.isCollectionOrder())
            .collectionOrderDate(
                entity.getCollectionOrderEffectiveDate() != null
                    ? entity.getCollectionOrderEffectiveDate().toString()
                    : null)
            .build();

        EnforcementOverride enforcementOverridesDto = EnforcementOverride.builder()
            .enforcementOverrideResult(
                entity.getEnforcementOverrideResultId() != null
                    ? EnforcementOverrideResultReference.builder()
                    .enforcementOverrideResultId(entity.getEnforcementOverrideResultId())
                    .enforcementOverrideResultTitle(null)
                    .build()
                    : null)
            .enforcer(
                entity.getEnforcementOverrideEnforcerId() != null
                    ? EnforcerReference.builder()
                    .enforcerId(entity.getEnforcementOverrideEnforcerId().intValue())
                    .enforcerName(null)
                    .build()
                    : null)
            .lja(
                entity.getEnforcementOverrideTfoLjaId() != null
                    ? LjaReference.builder()
                    .ljaId(Integer.valueOf(entity.getEnforcementOverrideTfoLjaId()))
                    .ljaName(null)
                    .build()
                    : null)
            .build();

        return DefendantAccountResponse.builder()
            .id(entity.getDefendantAccountId())
            .commentAndNotes(notes)
            .enforcementCourt(courtDto)
            .collectionOrder(collectionOrderDto)
            .enforcementOverrides(enforcementOverridesDto)
            .version(newVersion)
            .build();
    }


    private void applyCommentAndNotes(DefendantAccountEntity managed,
                                      CommentAndNotesDto notes,
                                      String postedBy) {

        String combined = Stream.of(
                notes.getAccountComment(),
                notes.getFreeTextNote1(),
                notes.getFreeTextNote2(),
                notes.getFreeTextNote3()
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
        em.lock(managed, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        log.debug(":applyCommentAndNotes: saved note for account {}", managed.getDefendantAccountId());
    }



    private void applyEnforcementCourt(DefendantAccountEntity entity, CourtReferenceDto courtRef) {
        Integer courtId = courtRef.getCourtId();
        if (courtId == null) {
            throw new IllegalArgumentException("enforcement_court.court_id is required");
        }
        CourtEntity court = courtRepository.findById(courtId.longValue())
            .orElseThrow(() -> new EntityNotFoundException("Court not found: " + courtId));
        entity.setEnforcingCourt(court);
        log.debug(":applyEnforcementCourt: accountId={}, courtId={}",
            entity.getDefendantAccountId(), court.getCourtId());
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

    private void applyEnforcementOverrides(DefendantAccountEntity entity, EnforcementOverride override) {
        if (override.getEnforcementOverrideResult() != null) {
            entity.setEnforcementOverrideResultId(
                override.getEnforcementOverrideResult().getEnforcementOverrideResultId());
        }
        if (override.getEnforcer() != null && override.getEnforcer().getEnforcerId() != null) {
            entity.setEnforcementOverrideEnforcerId(override.getEnforcer().getEnforcerId().longValue());
        }
        if (override.getLja() != null && override.getLja().getLjaId() != null) {
            entity.setEnforcementOverrideTfoLjaId(override.getLja().getLjaId().shortValue());
        }
        log.debug(":applyEnforcementOverrides: accountId={}, resultId={}, enforcerId={}, ljaId={}",
            entity.getDefendantAccountId(),
            override.getEnforcementOverrideResult() != null
                ? override.getEnforcementOverrideResult().getEnforcementOverrideResultId() : null,
            override.getEnforcer() != null ? override.getEnforcer().getEnforcerId() : null,
            override.getLja() != null ? override.getLja().getLjaId() : null);
    }
}

