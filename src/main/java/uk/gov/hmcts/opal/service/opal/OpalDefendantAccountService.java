package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCollectionOrder;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCommentsAndNotes;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildContactDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCourtReference;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEmployerDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementAction;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResult;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementStatus;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildLanguagePreferences;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildPartyAddressDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildPartyDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildVehicleDetails;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.filterDefendantParty;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.mapToDto;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.common.user.authentication.service.AccessTokenService;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.EmployerDetails;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.FixedPenaltyTicketDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.LanguagePreferences;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.VehicleDetails;
import uk.gov.hmcts.opal.dto.common.VehicleFixedPenaltyDetails;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.EnforcementOverrideResultEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.SearchDefendantAccountEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountSummaryViewRepository;
import uk.gov.hmcts.opal.repository.EnforcementOverrideResultRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentCardRequestRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantAccountRepository;
import uk.gov.hmcts.opal.repository.jpa.AliasSpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchDefendantAccountSpecs;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.persistence.PartyRepositoryService;
import uk.gov.hmcts.opal.service.iface.ReportEntryServiceInterface;
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

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    private final EnforcementRepository enforcementRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final AliasRepository aliasRepository;

    private final PaymentCardRequestRepository paymentCardRequestRepository;

    private final AccessTokenService accessTokenService;

    private final UserStateService userStateService;

    private final PartyRepositoryService partyRepositoryService;

    private final ResultRepository resultRepository;

    // Services
    private final DocumentService documentService;

    private final PaymentTermsService paymentTermsService;

    private final ResultService resultService;

    private final ReportEntryServiceInterface reportEntryService;

    // Mappers
    private final PaymentTermsMapper paymentTermsMapper;

    //TODO - Remove once repository service is in use
    @Transactional(readOnly = true)
    public DefendantAccountEntity getDefendantAccountById(long defendantAccountId) {
        return defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));
    }

    //TODO - Remove once repository service is in use
    @Transactional
    public DefendantAccountEntity getDefendantAccountByIdForUpdate(long defendantAccountId) {
        return defendantAccountRepository.findByDefendantAccountIdForUpdate(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account not found with id: " + defendantAccountId));
    }

    //TODO - Remove once repository service is in use
    @Override
    @Transactional(readOnly = true)
    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountHeaderViewEntity entity = repository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Defendant Account not found with id: "
                + defendantAccountId));

        return mapToDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts (Opal): criteria: {}", accountSearchDto);

        Specification<SearchDefendantAccountEntity> spec = SearchDefendantAccountSpecs.findBySearch(accountSearchDto);

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

    //Deprecated - use OpalDefendantAccountPaymentTermsService
    //TODO - Remove once OpalDefendantAccountPaymentTermsService is in use
    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId) {
        log.debug(":getPaymentTerms (Opal): criteria: {}", defendantAccountId);

        PaymentTermsEntity entity = defendantAccountPaymentTermsRepository
            .findTopByDefendantAccount_DefendantAccountIdOrderByPostedDateDescPaymentTermsIdDesc(
                defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException("Payment Terms not found for Defendant Account Id: "
                + defendantAccountId));

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(entity);
    }

    //Deprecated - use OpalDefendantAccountFixedPenaltyService
    //TODO - Remove once OpalDefendantAccountFixedPenaltyService is in use
    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = getDefendantAccountById(defendantAccountId);

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Fixed Penalty Offence not found for account: " + defendantAccountId));

        return toFixedPenaltyResponse(account, offence);
    }


    private DefendantAccountSummaryDto toSummaryDto(SearchDefendantAccountEntity e) {
        boolean isOrganisation = Boolean.TRUE.equals(e.getOrganisation());

        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(e.getDefendantAccountId()))
            .accountNumber(e.getAccountNumber())
            .organisation(isOrganisation)
            .organisationName(isOrganisation ? e.getOrganisationName() : null)
            .defendantTitle(isOrganisation ? null : e.getTitle())
            .defendantFirstnames(isOrganisation ? null : e.getForenames())
            .defendantSurname(isOrganisation ? null : e.getSurname())
            .addressLine1(OpalDefendantAccountBuilders.orEmpty(e.getAddressLine1()))
            .postcode(e.getPostcode())
            .businessUnitName(e.getBusinessUnitName())
            .businessUnitId(String.valueOf(e.getBusinessUnitId()))
            .prosecutorCaseReference(e.getProsecutorCaseReference())
            .lastEnforcementAction(e.getLastEnforcement())
            .accountBalance(e.getDefendantAccountBalance())
            .birthDate(e.getBirthDate() != null ? e.getBirthDate().toString() : null)
            .aliases(OpalDefendantAccountBuilders.buildSearchAliases(e))
            .build();
    }

    //Deprecated - use OpalDefendantAccountPartyService
    //TODO - Remove once OpalDefendantAccountPartyService is in use
    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId,
        Long defendantAccountPartyId) {
        log.debug(":getDefendantAccountParty: Opal mode: accountId={}, partyId={}", defendantAccountId,
            defendantAccountPartyId);

        // Find the DefendantAccountEntity by ID
        DefendantAccountEntity account = getDefendantAccountById(defendantAccountId);

        // Find the DefendantAccountPartiesEntity by Party ID
        DefendantAccountPartiesEntity party = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(defendantAccountPartyId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Party not found for accountId=" + defendantAccountId
                    + ", partyId=" + defendantAccountPartyId));

        List<AliasEntity> aliasEntity = aliasRepository.findByParty_PartyId(party.getParty().getPartyId());

        // Map entity to PartyDetails DTO
        DefendantAccountParty defendantAccountParty = mapDefendantAccountParty(party, aliasEntity);

        return GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(defendantAccountParty)
            .version(account.getVersion())
            .build();

    }

    //Deprecated - use OpalDefendantAccountPartyService
    // TODO - Remove once OpalDefendantAccountPartyService is in use
    @Deprecated
    private DefendantAccountParty mapDefendantAccountParty(
        DefendantAccountPartiesEntity partyEntity, List<AliasEntity> aliases) {

        PartyEntity party = partyEntity.getParty();
        Optional<DebtorDetailEntity> debtorDetail = debtorDetailRepository.findByPartyId(party.getPartyId());

        return DefendantAccountParty.builder()
            .defendantAccountPartyType(partyEntity.getAssociationType())
            .isDebtor(partyEntity.getDebtor())
            .partyDetails(buildPartyDetails(party, aliases))
            .address(buildPartyAddressDetails(party))
            .contactDetails(buildContactDetails(party))
            .vehicleDetails(buildVehicleDetails(debtorDetail))
            .employerDetails(buildEmployerDetails(debtorDetail))
            .languagePreferences(buildLanguagePreferences(debtorDetail))
            .build();
    }

    //Deprecated - use OpalDefendantAccountFixedPenaltyService
    //TODO - Remove once OpalDefendantAccountFixedPenaltyService is in use
    private static GetDefendantAccountFixedPenaltyResponse toFixedPenaltyResponse(
        DefendantAccountEntity account, FixedPenaltyOffenceEntity offence) {

        boolean isVehicle =
            offence.getVehicleRegistration() != null
                && !"NV".equalsIgnoreCase(offence.getVehicleRegistration());

        FixedPenaltyTicketDetails ticketDetails = FixedPenaltyTicketDetails.builder()
            .issuingAuthority(account.getOriginatorName())
            .ticketNumber(offence.getTicketNumber())
            .timeOfOffence(
                offence.getTimeOfOffence() != null
                    ? offence.getTimeOfOffence().toString()
                    : null
            )
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
            .version(account.getVersion())
            .build();
    }

    //TODO - Remove this once repository service is in use
    @Transactional(readOnly = true)
    public DefendantAccountSummaryViewEntity getDefendantAccountSummaryViewById(long defendantAccountId) {
        return defendantAccountSummaryViewRepository.findById(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Summary View not found with id: " + defendantAccountId));
    }

    @Transactional(readOnly = true)
    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.debug(":getAtAGlance (Opal): id: {}.", defendantAccountId);
        return OpalDefendantAccountBuilders
            .buildAtAGlanceResponse(getDefendantAccountSummaryViewById(defendantAccountId));
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

        DefendantAccountEntity entity = getDefendantAccountById(defendantAccountId);

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
            OpalDefendantAccountBuilders.applyCollectionOrder(entity, request.getCollectionOrder());
        }
        if (request.getEnforcementOverride() != null) {
            OpalDefendantAccountBuilders.applyEnforcementOverride(entity, request.getEnforcementOverride());
        }

        defendantAccountRepository.save(entity);

        em.lock(entity, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush();
        BigInteger newVersion = entity.getVersion();

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
        return DefendantAccountResponse.builder()
            .id(entity.getDefendantAccountId())
            .commentsAndNotes(buildCommentsAndNotes(entity))
            .enforcementCourt(buildCourtReference(entity.getEnforcingCourt()))
            .collectionOrder(buildCollectionOrder(entity))
            .enforcementOverride(buildEnforcementOverride(entity))
            .version(newVersion)
            .build();
    }

    /**
     * This method adds a payment card request to a defendant account.
     *
     * <p>
     * This method is separated from the public interface to allow for
     * addition of a payment card in a chained operation.
     * </p>
     */
    private AddPaymentCardRequestResponse addPaymentCard(
        Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader) {

        log.debug(":addPaymentCard (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCard");

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, authHeader);

        // Minimal response
        return new AddPaymentCardRequestResponse(defendantAccountId);
    }

    //Deprecated - use OpalDefendantAccountPartyService
    //TODO - Remove once OpalDefendantAccountPartyService is in use
    // TODO - Created PO-2452 to fix bumping the version with a more atomically correct method
    private DefendantAccountEntity bumpVersion(Long accountId) {
        DefendantAccountEntity entity = getDefendantAccountById(accountId);
        entity.setVersionNumber(entity.getVersion().add(BigInteger.ONE).longValueExact());
        return defendantAccountRepository.saveAndFlush(entity);
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Override
    public AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch, String authHeader, AddDefendantAccountEnforcementRequest request) {
        return null;
    }

    //Deprecated - use OpalDefendantAccountPartyService
    // TODO - Remove once OpalDefendantAccountPartyService is in use
    @Override
    @Transactional
    public GetDefendantAccountPartyResponse replaceDefendantAccountParty(
        Long accountId,
        Long dapId,
        DefendantAccountParty request,
        String ifMatch,
        String businessUnitId,
        String postedBy,
        String businessUserId) {

        DefendantAccountEntity account = getDefendantAccountById(accountId);

        if (account.getBusinessUnit() == null
            || account.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(account.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }

        VersionUtils.verifyIfMatch(account, ifMatch, accountId, "replaceDefendantAccountParty");
        amendmentService.auditInitialiseStoredProc(accountId, RecordType.DEFENDANT_ACCOUNTS);

        DefendantAccountPartiesEntity dap = account.getParties().stream()
            .filter(p -> p.getDefendantAccountPartyId().equals(dapId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException(
                "Defendant Account Party not found for accountId=" + accountId + ", partyId=" + dapId));

        PartyEntity party = dap.getParty();

        Long requestedPartyId = OpalDefendantAccountBuilders.safeParseLong(
            request != null && request.getPartyDetails() != null ? request.getPartyDetails().getPartyId() : null);

        if (party == null) {
            if (requestedPartyId == null) {
                throw new IllegalArgumentException("party_id is required");
            }
            party = partyRepositoryService.findById(requestedPartyId);   // loads & manages the entity
            dap.setParty(party);
        } else {
            if (requestedPartyId != null && !Objects.equals(party.getPartyId(), requestedPartyId)) {
                throw new IllegalArgumentException("Switching party is not allowed");
            }

            party = partyRepositoryService.findById(party.getPartyId());
            dap.setParty(party);
        }

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        dap.setAssociationType(request.getDefendantAccountPartyType());
        dap.setDebtor(request.getIsDebtor());

        OpalDefendantAccountBuilders.applyPartyCoreReplace(party, request.getPartyDetails());
        OpalDefendantAccountBuilders.applyPartyAddressReplace(party, request.getAddress());
        OpalDefendantAccountBuilders.applyPartyContactReplace(party, request.getContactDetails());

        boolean isDebtor = Boolean.TRUE.equals(request.getIsDebtor());
        replaceDebtorDetail(
            party.getPartyId(),
            request.getVehicleDetails(),
            request.getEmployerDetails(),
            request.getLanguagePreferences(),
            isDebtor
        );

        replaceAliasesForParty(party.getPartyId(), request.getPartyDetails());

        amendmentService.auditFinaliseStoredProc(
            account.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            postedBy,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        List<AliasEntity> aliasEntity = party.getPartyId() == null
            ? java.util.Collections.emptyList()
            : aliasRepository.findByParty_PartyId(party.getPartyId());

        return GetDefendantAccountPartyResponse.builder()
            .defendantAccountParty(mapDefendantAccountParty(dap, aliasEntity))
            .version(bumpVersion(accountId).getVersion())
            .build();
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    EnforcementOverride buildEnforcementOverride(DefendantAccountEntity entity) {
        if (entity.getEnforcementOverrideResultId() == null
            && entity.getEnforcementOverrideEnforcerId() == null
            && entity.getEnforcementOverrideTfoLjaId() == null) {
            return null;
        } else {
            return EnforcementOverride.builder()
                .enforcementOverrideResult(buildEnforcementOverrideResult(dbResultEntity(
                    entity.getEnforcementOverrideResultId())))
                .enforcer(OpalDefendantAccountBuilders.buildEnforcer(dbEnforcerEntity(entity)))
                .lja(OpalDefendantAccountBuilders.buildLja(dbLja(entity)))
                .build();
        }
    }

    // These 'DB' methods are focused purely on fetching relevant entities from the DB without any mapping.

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<ResultEntity.Lite> dbResultEntity(String resultId) {
        return Optional.ofNullable(resultId).flatMap(resultRepository::findById);
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<EnforcementOverrideResultEntity> dbEnforcementOverrideResult(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideResultId())
            .flatMap(enforcementOverrideResultRepository::findById);
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<EnforcerEntity> dbEnforcerEntity(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideEnforcerId())
            .flatMap(enforcerRepository::findById)
            .filter(enf -> Objects.nonNull(enf.getEnforcerId()));
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<LocalJusticeAreaEntity> dbLja(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideTfoLjaId())
            .flatMap(localJusticeAreaRepository::findById);
    }

    @Transactional(readOnly = true)
    List<AliasEntity> dbAliasesForDefendantAccount(Long defendantAccountId) {
        return aliasRepository.findAll(AliasSpecs.byDefendantAccountId(defendantAccountId));
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<DebtorDetailEntity> dbDebtorDetails(PartyEntity party) {
        return debtorDetailRepository.findByPartyId(party.getPartyId());
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    List<EnforcementEntity.Lite> dbEnforcements(DefendantAccountEntity entity) {
        return enforcementRepository.findAllByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            entity.getDefendantAccountId(), entity.getLastEnforcement());
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Transactional(readOnly = true)
    Optional<EnforcementEntity.Lite> dbEnforcementMostRecent(DefendantAccountEntity entity) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            entity.getDefendantAccountId(), entity.getLastEnforcement());
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Override
    @Transactional(readOnly = true)
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus: def acc: {}",  defendantAccountId);

        DefendantAccountEntity defendantEntity = getDefendantAccountById(defendantAccountId);
        DefendantAccountPartiesEntity defendantParty = filterDefendantParty(defendantEntity);
        Optional<EnforcementEntity.Lite> recentEnforcement = dbEnforcementMostRecent(defendantEntity);

        return buildEnforcementStatus(
            defendantEntity,
            defendantParty,
            dbDebtorDetails(defendantParty.getParty()),
            recentEnforcement.map(EnforcementEntity::getResult),
            buildEnforcementOverride(defendantEntity),
            buildEnforcementAction(recentEnforcement,
                recentEnforcement
                    .map(EnforcementEntity::getEnforcerId)
                    .map(enforcerRepository::findByEnforcerId)));
    }

    private void applyCommentAndNotes(DefendantAccountEntity managed, CommentsAndNotes notes, String postedBy) {

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

        // Persist values on the main defendant_accounts table
        managed.setAccountComments(notes.getAccountNotesAccountComments());
        managed.setAccountNote1(notes.getAccountNotesFreeTextNote1());
        managed.setAccountNote2(notes.getAccountNotesFreeTextNote2());
        managed.setAccountNote3(notes.getAccountNotesFreeTextNote3());

        noteRepository.save(OpalDefendantAccountBuilders.buildNoteEntity(managed, combined, postedBy));
        log.debug(":applyCommentAndNotes: saved note for account {}", managed.getDefendantAccountId());
    }

    private void applyEnforcementCourt(DefendantAccountEntity entity, CourtReferenceDto courtRef) {
        Integer courtId = courtRef.getCourtId();
        if (courtId == null) {
            throw new IllegalArgumentException("enforcement_court.court_id is required");
        }
        CourtEntity court = courtRepository.findById(courtId.longValue())
            .orElseThrow(() -> new EntityNotFoundException("Court not found: " + courtId));
        entity.setEnforcingCourt(OpalDefendantAccountBuilders.asLite(court));
        log.debug(":applyEnforcementCourt: accountId={}, courtId={}",
            entity.getDefendantAccountId(), court.getCourtId());
    }

    //Deprecated - use OpalDefendantAccountPartyService
    //TODO: Remove this method once OpalDefendantAccountPartyService is in use
    private void replaceAliasesForParty(Long partyId, PartyDetails pd) {
        if (partyId == null || pd == null || pd.getOrganisationFlag() == null) {
            return;
        }

        PartyEntity party = partyRepositoryService.findById(partyId);

        List<AliasEntity> existing = aliasRepository.findByParty_PartyId(partyId);

        Map<Long, AliasEntity> byId = new HashMap<>();
        for (AliasEntity e : existing) {
            if (e.getAliasId() != null) {
                byId.put(e.getAliasId(), e);
            }
        }

        List<AliasEntity> toPersist = new ArrayList<>();
        Set<Long> keepIds = new HashSet<>();

        if (Boolean.TRUE.equals(pd.getOrganisationFlag())) {
            List<OrganisationAlias> orgAliases = Optional.ofNullable(pd.getOrganisationDetails())
                .map(OrganisationDetails::getOrganisationAliases)
                .orElse(Collections.emptyList());

            for (OrganisationAlias a : orgAliases) {
                if (a == null) {
                    continue;
                }

                String idStr = a.getAliasId();
                Long id = (idStr == null || idStr.trim().isEmpty()) ? null : Long.valueOf(idStr.trim());

                AliasEntity row = upsertAlias(
                    byId, party,
                    id, a.getSequenceNumber(),
                    a.getOrganisationName(),
                    null, null,
                    true
                );
                toPersist.add(row);
                if (row.getAliasId() != null) {
                    keepIds.add(row.getAliasId());
                }
            }

        } else {
            List<IndividualAlias> indAliases = Optional.ofNullable(pd.getIndividualDetails())
                .map(IndividualDetails::getIndividualAliases)
                .orElse(Collections.emptyList());

            for (IndividualAlias a : indAliases) {
                if (a == null) {
                    continue;
                }

                String idStr = a.getAliasId();
                Long id = (idStr == null || idStr.trim().isEmpty()) ? null : Long.valueOf(idStr.trim());

                AliasEntity row = upsertAlias(
                    byId, party,
                    id, a.getSequenceNumber(),
                    null,
                    a.getForenames(), a.getSurname(),
                    false
                );
                toPersist.add(row);
                if (row.getAliasId() != null) {
                    keepIds.add(row.getAliasId());
                }
            }
        }

        if (!toPersist.isEmpty()) {
            List<AliasEntity> persisted = aliasRepository.saveAll(toPersist);
            for (AliasEntity p : persisted) {
                if (p.getAliasId() != null) {
                    keepIds.add(p.getAliasId());
                }
            }
        }

        deletePartyAliasesNotIn(partyId, keepIds);
        aliasRepository.flush();
    }

    /**
     * Upsert a single alias: - if aliasId present, updates the existing row (must belong to this party) - if aliasId
     * null, creates a new row (insert) Also normalizes org/individual fields.
     */
    //Deprecated - use OpalDefendantAccountPartyService
    //TODO: Remove this method once OpalDefendantAccountPartyService is in use
    private AliasEntity upsertAlias(
        Map<Long, AliasEntity> byId,
        PartyEntity party,
        Long aliasId,
        Integer sequenceNumber,
        String orgName,
        String forenames,
        String surname,
        boolean isOrg
    ) {

        AliasEntity row;
        if (aliasId != null) {
            row = byId.get(aliasId);
            if (row == null) {
                throw new EntityNotFoundException(
                    "Alias not found for partyId=" + party.getPartyId() + ", aliasId=" + aliasId);
            }
        } else {
            row = new AliasEntity();
        }

        row.setParty(party);
        row.setSequenceNumber(sequenceNumber);

        if (isOrg) {
            row.setOrganisationName(orgName);
            row.setForenames(null);
            row.setSurname(null);
        } else {
            row.setOrganisationName(null);
            row.setForenames(forenames);
            row.setSurname(surname);
        }
        return row;
    }

    //Deprecated - use OpalDefendantAccountPartyService
    //TODO: Remove this method once OpalDefendantAccountPartyService is in use
    private void deletePartyAliasesNotIn(Long partyId, Set<Long> keepIds) {
        if (keepIds == null || keepIds.isEmpty()) {
            aliasRepository.deleteByParty_PartyId(partyId);
        } else {
            aliasRepository.deleteByParty_PartyIdAndAliasIdNotIn(partyId, keepIds);
        }
    }

    //Deprecated - use OpalDefendantAccountPartyService
    //TODO: Remove this method once OpalDefendantAccountPartyService is in use
    private void replaceDebtorDetail(Long partyId,
        VehicleDetails vehicle,
        EmployerDetails employer,
        LanguagePreferences language,
        boolean isDebtor) {

        if (partyId == null) {
            return;
        }

        if (!isDebtor) {
            return;
        }

        DebtorDetailEntity debtor = debtorDetailRepository.findById(partyId)
            .orElseThrow(() -> new EntityNotFoundException("debtor_detail not found with id: " + partyId));

        if (debtor == null) {
            debtor = new DebtorDetailEntity();
            debtor.setPartyId(partyId);
        }

        debtor.setVehicleMake(vehicle != null ? vehicle.getVehicleMakeAndModel() : null);
        debtor.setVehicleRegistration(vehicle != null ? vehicle.getVehicleRegistration() : null);

        if (employer != null) {
            debtor.setEmployerName(employer.getEmployerName());
            debtor.setEmployeeReference(employer.getEmployerReference());
            debtor.setEmployerEmail(employer.getEmployerEmailAddress());
            debtor.setEmployerTelephone(employer.getEmployerTelephoneNumber());

            AddressDetails ea = employer.getEmployerAddress();
            if (ea != null) {
                debtor.setEmployerAddressLine1(ea.getAddressLine1());
                debtor.setEmployerAddressLine2(ea.getAddressLine2());
                debtor.setEmployerAddressLine3(ea.getAddressLine3());
                debtor.setEmployerAddressLine4(ea.getAddressLine4());
                debtor.setEmployerAddressLine5(ea.getAddressLine5());
                debtor.setEmployerPostcode(ea.getPostcode());
            } else {
                debtor.setEmployerAddressLine1(null);
                debtor.setEmployerAddressLine2(null);
                debtor.setEmployerAddressLine3(null);
                debtor.setEmployerAddressLine4(null);
                debtor.setEmployerAddressLine5(null);
                debtor.setEmployerPostcode(null);
            }
        } else {
            debtor.setEmployerName(null);
            debtor.setEmployeeReference(null);
            debtor.setEmployerEmail(null);
            debtor.setEmployerTelephone(null);
            debtor.setEmployerAddressLine1(null);
            debtor.setEmployerAddressLine2(null);
            debtor.setEmployerAddressLine3(null);
            debtor.setEmployerAddressLine4(null);
            debtor.setEmployerAddressLine5(null);
            debtor.setEmployerPostcode(null);
        }

        if (language != null) {
            debtor.setDocumentLanguage(language.getDocumentLanguagePreference() != null
                ? language.getDocumentLanguagePreference().getLanguageCode() : null);
            debtor.setHearingLanguage(language.getHearingLanguagePreference() != null
                ? language.getHearingLanguagePreference().getLanguageCode() : null);
            debtor.setDocumentLanguageDate(LocalDate.now());
            debtor.setHearingLanguageDate(LocalDate.now());
        } else {
            debtor.setDocumentLanguage(null);
            debtor.setHearingLanguage(null);
            debtor.setDocumentLanguageDate(null);
            debtor.setHearingLanguageDate(null);
        }

        debtorDetailRepository.save(debtor);
    }

    private static void applyEnforcementOverride(DefendantAccountEntity entity, EnforcementOverride override) {
        if (override.getEnforcementOverrideResult() != null) {
            entity.setEnforcementOverrideResultId(
                override.getEnforcementOverrideResult().getEnforcementOverrideId());
        }
        if (override.getEnforcer() != null && override.getEnforcer().getEnforcerId() != null) {
            entity.setEnforcementOverrideEnforcerId(override.getEnforcer().getEnforcerId());
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

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    @Override
    @Transactional
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader) {

        log.debug(":addPaymentCardRequest (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = getDefendantAccountById(defendantAccountId);

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        AddPaymentCardRequestResponse paymentCardResponse = addPaymentCard(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            authHeader
        );

        auditComplete(defendantAccountId, account, businessUnitUserId);

        return paymentCardResponse;
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = getDefendantAccountById(accountId);
        validateBusinessUnitPresent(account, buId);
        return account;
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private void validateBusinessUnitPresent(DefendantAccountEntity account, String buId) {
        if (account.getBusinessUnit() == null
            || account.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(account.getBusinessUnit().getBusinessUnitId()).equals(buId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + buId);
        }
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    private void ensureNoExistingPaymentCardRequest(Long accountId) {
        if (paymentCardRequestRepository.existsByDefendantAccountId(accountId)) {
            throw new ResourceConflictException(
                "DefendantAccountEntity",
                String.valueOf(accountId),
                "A payment card request already exists for this account.",
                null
            );
        }
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private void createPaymentCardRequest(Long accountId) {
        PaymentCardRequestEntity pcr = PaymentCardRequestEntity.builder()
            .defendantAccountId(accountId)
            .build();
        paymentCardRequestRepository.save(pcr);
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private void updateDefendantAccountWithPcr(DefendantAccountEntity account,
        String businessUnitUserId,
        String authHeader) {

        String displayName = accessTokenService.extractName(authHeader);

        account.setPaymentCardRequested(true);
        account.setPaymentCardRequestedDate(LocalDate.now());
        account.setPaymentCardRequestedBy(businessUnitUserId);
        account.setPaymentCardRequestedByName(displayName);

        defendantAccountRepository.save(account);
    }

    private void auditComplete(Long accountId,
        DefendantAccountEntity account,
        String businessUnitUserId) {

        Short buId = account.getBusinessUnit().getBusinessUnitId();

        amendmentService.auditFinaliseStoredProc(
            accountId,
            RecordType.DEFENDANT_ACCOUNTS,
            buId,
            businessUnitUserId,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );
    }

    private static record ParsedAlias(
        String aliasId,
        Integer sequenceNumber,
        String forenames,
        String surname,
        String organisationName) { }

    @Override
    @Transactional
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTerms (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        // Look up the defendant account
        DefendantAccountEntity defAccount = getDefendantAccountByIdForUpdate(defendantAccountId);

        // Validate BU
        if (defAccount.getBusinessUnit() == null
            || defAccount.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(defAccount.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }

        VersionUtils.verifyIfMatch(defAccount, ifMatch, defendantAccountId, "addPaymentTerms");

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        // Toggle any existing active payment term(s) for the defendant account to inactive
        paymentTermsService.deactivateExistingActivePaymentTerms(defAccount.getDefendantAccountId());

        // Map request -> Payment Terms Entity using MapStruct
        PaymentTermsEntity paymentTermsEntity
            = paymentTermsMapper.toEntity(addPaymentTermsRequest.getPaymentTerms());
        paymentTermsEntity.setDefendantAccount(defAccount);
        // Persist the new (active) PaymentTermsEntity
        PaymentTermsEntity savedPaymentTerms = paymentTermsService.addPaymentTerm(paymentTermsEntity);

        // Update defendant account with any payment term related attributes
        addPaymentTerm(defAccount, addPaymentTermsRequest);

        // Clear last_enforcement on the defendant account, if applicable
        clearLastEnforcementAction(defAccount,
            savedPaymentTerms.getPaymentTermsId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        defendantAccountRepository.save(defAccount);

        // If requestPaymentCardFlag is true: create a PaymentCardRequest row (if not already present)
        //  and update the defendant account PCR-related attributes (requested flag/date/by/byName).
        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(":addPaymentTerms: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, authHeader);
        }

        // if generate_payment_terms_change_letter is true
        //   Create a Document Instances record for a Payment Terms Change Letter.
        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(":addPaymentTerms: Generate Payment Terms Change Letter flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        // Create report entry for the Extension of Time to Pay report
        reportEntryService.createExtendTtpReportEntry(savedPaymentTerms.getPaymentTermsId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(":addPaymentTerms: saved payment terms id={} for account {}",
            savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

        amendmentService.auditFinaliseStoredProc(
            defAccount.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            businessUnitUserId,
            defAccount.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    /**
     * Along with adding a new latest payment_terms we will also clear the last enforcement action on the account.
     * The only time it doesn't is when the last enforcement has result.extend_ttp_preserve_last_enf = TRUE
     */
    private void clearLastEnforcementAction(DefendantAccountEntity defAccount, Long savedPaymentTermsId,
        Short businessUnitId) {
        // Retrieve most recent enforcement action for this account using the result
        Optional<EnforcementEntity.Lite> mostRecentEnforcementOpt = dbEnforcementMostRecent(defAccount);

        if (mostRecentEnforcementOpt.isPresent()) {
            EnforcementEntity.Lite mostRecentEnforcement = mostRecentEnforcementOpt.get();
            ResultEntity.Lite resultEntityLite = resultService.getResultById(mostRecentEnforcement.getResultId());

            // If resultEntity exists and extend_ttp_preserve_last_enf is not TRUE, clear last_enforcement
            if (!resultEntityLite.isExtendTtpPreserveLastEnf()) {
                log.debug(":clearLastEnforcementAction: Clearing last_enforcement={} for account {}",
                    defAccount.getLastEnforcement(), defAccount.getDefendantAccountId());
                defAccount.setLastEnforcement(null);

            } else {
                log.debug(":clearLastEnforcementAction: Preserving last_enforcement={} for account {} as "
                        + "extend_ttp_preserve_last_enf=TRUE",
                    defAccount.getLastEnforcement(), defAccount.getDefendantAccountId());
            }
        }
    }

    /**
     * Add payment term related attributes to the defendant account.
     */
    private void addPaymentTerm(DefendantAccountEntity defAccount,
        AddDefendantAccountPaymentTermsRequest paymentTermsRequest) {

        defAccount.setSuspendedCommittalDate(paymentTermsRequest.getPaymentTerms().getDateDaysInDefaultImposed());
    }
}
