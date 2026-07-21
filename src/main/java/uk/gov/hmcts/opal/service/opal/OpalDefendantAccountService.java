package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCollectionOrderCommon;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCommentsAndNotes;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCourtReference;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementAction;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResult;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResultDefendantAccount;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementStatus;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.filterDefendantParty;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.Checks;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.DefendantAccountSummaryDtoBuilder;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.WarnError;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.GetDefendantAccountConsolidatedAccountsResult;
import uk.gov.hmcts.opal.dto.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount;
import uk.gov.hmcts.opal.exception.DefendantAccountNotFoundException;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementCourtDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountResponsePayload;
import uk.gov.hmcts.opal.mapper.ConsolidatedAccountMapper;
import uk.gov.hmcts.opal.mapper.DefendantAccountHeaderSummaryMapper;
import uk.gov.hmcts.opal.mapper.common.EnforcerDefendantAccountMapper;
import uk.gov.hmcts.opal.repository.ConsolidatedAccountRepository;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.FixedPenaltyOffenceRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantBasicRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantConsolidatedRepository;
import uk.gov.hmcts.opal.repository.jpa.SearchBasicEntitySpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchConsolidatedEntitySpecs;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountHeaderViewRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountSummaryViewRepositoryService;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private static final int TOO_MANY_SEARCH_RESULTS = 100;

    private final DefendantAccountRepository defendantAccountRepository;

    private final ConsolidatedAccountRepository consolidatedAccountRepository;

    private final SearchDefendantBasicRepository searchDefendantBasicRepository;
    private final SearchDefendantConsolidatedRepository searchConsolidatedRepository;

    private final SearchBasicEntitySpecs searchBasicEntitySpecs;
    private final SearchConsolidatedEntitySpecs searchConsolidatedEntitySpecs;

    private final DefendantAccountSummaryViewRepositoryService defendantAccountSummaryViewRepositoryService;
    private final DefendantAccountHeaderViewRepositoryService defendantAccountHeaderViewRepositoryService;

    private final CourtLiteRepository courtLiteRepository;
    private final AmendmentService amendmentService;

    private final EntityManager em;

    private final NoteRepository noteRepository;

    private final LocalJusticeAreaRepository localJusticeAreaRepository;

    private final EnforcerRepository enforcerRepository;

    private final FixedPenaltyOffenceRepository fixedPenaltyOffenceRepository;

    private final EnforcementRepository enforcementRepository;

    private final DebtorDetailRepository debtorDetailRepository;

    private final ResultRepository resultRepository;

    // Services
    private final DefendantAccountHistoryService defendantAccountHistoryService;

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;

    // Mappers
    private final ConsolidatedAccountMapper consolidatedAccountMapper;
    private final DefendantAccountHeaderSummaryMapper defendantAccountHeaderSummaryMapper;
    private final EnforcerDefendantAccountMapper enforcerDefendantAccountMapper;

    private final Clock clock;

    private final DefendantAccountControlValidator defendantAccountControlValidator;

    @Override
    @Transactional(readOnly = true)
    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountHeaderViewEntity entity = defendantAccountHeaderViewRepositoryService
            .getHeaderViewById(defendantAccountId);

        return defendantAccountHeaderSummaryMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountConsolidatedAccountsResult getConsolidatedAccounts(Long defendantAccountId) {
        log.debug(":getConsolidatedAccounts: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountEntity masterAccount = defendantAccountRepository.findById(defendantAccountId)
            .orElseThrow(() -> new DefendantAccountNotFoundException(defendantAccountId));

        return GetDefendantAccountConsolidatedAccountsResult.builder()
            .payload(consolidatedAccountMapper.toResponse(
                consolidatedAccountRepository.findByMasterAccountIdOrderByChildAccountIdAsc(defendantAccountId)))
            .version(masterAccount.getVersion())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        return defendantAccountHistoryService.getHistory(defendantAccountId, filter);
    }

    @Override
    @Transactional(readOnly = true)
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts (Opal): criteria: {}", accountSearchDto);

        boolean consolidatedSearch = accountSearchDto.isConsolidationSearch();
        log.debug(":searchDefendantAccounts: Using {} search", consolidatedSearch ? "consolidated" : "basic");

        List<DefendantAccountSummaryDto> summaries = consolidatedSearch
            ? consolidatedSearch(accountSearchDto)
            : basicSearch(accountSearchDto);

        return DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(summaries)
            .build();
    }

    private List<DefendantAccountSummaryDto> consolidatedSearch(AccountSearchDto accountSearchDto) {
        List<DefendantAccountSummaryDto> results = searchConsolidatedRepository
            .findAll(searchConsolidatedEntitySpecs.findBySearch(accountSearchDto))
            .stream()
            .map(this::toSummaryDto)
            .filter(this::hasNonZeroBalance)
            .toList();

        if (results.size() > TOO_MANY_SEARCH_RESULTS) {
            log.warn("Consolidated search returned {} results, limiting to 100", results.size());
            throw new UnprocessableException("Search generated more than " + TOO_MANY_SEARCH_RESULTS
                + " results. Please refine your search and try again.");
        }
        return results;
    }

    private List<DefendantAccountSummaryDto> basicSearch(AccountSearchDto accountSearchDto) {
        return searchDefendantBasicRepository
            .findAll(searchBasicEntitySpecs.findBySearch(accountSearchDto))
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    private DefendantAccountSummaryDto toSummaryDto(SearchDefendantAccount account) {
        return toSummaryBuilder(account).build();
    }

    private DefendantAccountSummaryDto toSummaryDto(SearchConsolidatedEntity account) {
        return toSummaryBuilder(account)
            .hasCollectionOrder(account.getHasCollectionOrder())
            .accountVersion(account.getVersion())
            .checks(Checks.builder()
                .errors(convertWarnErrors(account.getErrors()))
                .warnings(convertWarnErrors(account.getWarnings()))
                .build())
            .build();
    }

    private List<WarnError> convertWarnErrors(List<String> fromDb) {
        if (fromDb == null || fromDb.isEmpty()) {
            return Collections.emptyList();
        }
        return fromDb.stream()
            .filter(Objects::nonNull)
            .filter(this::isNotBlank)
            .map(WarnError::new)
            .toList();
    }

    private boolean isNotBlank(String s) {
        return !s.isBlank();
    }

    private DefendantAccountSummaryDtoBuilder toSummaryBuilder(SearchDefendantAccount account) {
        boolean isOrganisation = Boolean.TRUE.equals(account.getOrganisation());

        return DefendantAccountSummaryDto.builder()
            .defendantAccountId(String.valueOf(account.getDefendantAccountId()))
            .accountNumber(account.getAccountNumber())
            .organisation(isOrganisation)
            .organisationName(isOrganisation ? account.getOrganisationName() : null)
            .defendantTitle(isOrganisation ? null : account.getTitle())
            .defendantFirstnames(isOrganisation ? null : account.getForenames())
            .defendantSurname(isOrganisation ? null : account.getSurname())
            .addressLine1(OpalDefendantAccountBuilders.orEmpty(account.getAddressLine1()))
            .postcode(account.getPostcode())
            .businessUnitName(account.getBusinessUnitName())
            .businessUnitId(String.valueOf(account.getBusinessUnitId()))
            .prosecutorCaseReference(account.getProsecutorCaseReference())
            .lastEnforcementAction(account.getLastEnforcement())
            .accountBalance(account.getDefendantAccountBalance())
            .birthDate(account.getBirthDate() != null ? account.getBirthDate().toString() : null)
            .aliases(OpalDefendantAccountBuilders.buildSearchAliases(account));
    }

    //Deprecated - use OpalDefendantAccountFixedPenaltyService
    //TODO - Remove once OpalDefendantAccountFixedPenaltyService is in use
    @Override
    @Transactional(readOnly = true)
    public GetDefendantAccountFixedPenaltyResponse getDefendantAccountFixedPenalty(Long defendantAccountId) {
        log.debug(":getDefendantAccountFixedPenalty (Opal): id={}", defendantAccountId);

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        FixedPenaltyOffenceEntity offence = fixedPenaltyOffenceRepository.findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Fixed Penalty Offence not found for account: " + defendantAccountId));

        return OpalDefendantAccountBuilders.toFixedPenaltyResponse(account, offence);
    }

    @Transactional(readOnly = true)
    public DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId) {
        log.debug(":getAtAGlance (Opal): id: {}.", defendantAccountId);
        return OpalDefendantAccountBuilders
            .buildAtAGlanceResponse(
                defendantAccountSummaryViewRepositoryService.getSummaryViewById(defendantAccountId));
    }

    @Override
    @Transactional
    public UpdateDefendantAccountResponse updateDefendantAccount(
        Long defendantAccountId,
        String businessUnitId,
        UpdateDefendantAccountRequest request,
        String postedBy,
        String postedByName
    ) {
        log.debug(":updateDefendantAccount (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity entity = defendantAccountRepositoryService.findById(defendantAccountId);

        if (entity.getBusinessUnit() == null
            || entity.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(entity.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit "
                + businessUnitId);
        }

        VersionUtils.verifyIfMatch(entity, request.getVersion(), defendantAccountId, "updateDefendantAccount");

        if (defendantAccountControlValidator.isProtectedUpdate(request, entity)) {
            defendantAccountControlValidator.validateCanUpdateProtectedFields(entity);
        }

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        if (request.getPayload().getCommentAndNotes() != null) {
            applyCommentAndNotes(entity,
                request.getPayload().getCommentAndNotes(), postedBy);
        }
        if (request.getPayload().getEnforcementCourt() != null) {
            applyEnforcementCourt(entity,
                request.getPayload().getEnforcementCourt());
        }
        if (request.getPayload().getCollectionOrder() != null) {
            OpalDefendantAccountBuilders.applyCollectionOrder(entity,
                request.getPayload().getCollectionOrder());
        }
        if (request.getPayload().getEnforcementOverride() != null) {
            OpalDefendantAccountBuilders.applyEnforcementOverride(entity,
                request.getPayload().getEnforcementOverride());
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
            postedByName,
            entity.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        // ---- Build response ----
        return UpdateDefendantAccountResponse.builder()
            .payload(
                UpdateDefendantAccountResponsePayload.builder()
                .id(entity.getDefendantAccountId())
                .commentAndNotes(buildCommentsAndNotes(entity))
                .enforcementCourt(buildCourtReference(entity.getEnforcingCourt()))
                .collectionOrder(buildCollectionOrderCommon(entity))
                .enforcementOverride(buildEnforcementOverrideDefendantAccount(entity))
                .build()
            )
            .version(newVersion)
            .build();
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Override
    public AddEnforcementResponse addEnforcement(Long defendantAccountId, String businessUnitId,
        String businessUnitUserId, String ifMatch, AddDefendantAccountEnforcementRequest request) {
        return null;
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
                .enforcementOverrideResult(buildEnforcementOverrideResult(fetchResultEntity(
                    entity.getEnforcementOverrideResultId())))
                .enforcer(OpalDefendantAccountBuilders.buildEnforcer(fetchEnforcerEntity(entity)))
                .lja(OpalDefendantAccountBuilders.buildLja(fetchLja(entity)))
                .build();
        }
    }

    EnforcementOverrideDefendantAccount buildEnforcementOverrideDefendantAccount(DefendantAccountEntity entity) {
        if (entity.getEnforcementOverrideResultId() == null
            && entity.getEnforcementOverrideEnforcerId() == null
            && entity.getEnforcementOverrideTfoLjaId() == null) {
            return null;
        } else {
            return EnforcementOverrideDefendantAccount.builder()
                .enforcementOverrideResult(buildEnforcementOverrideResultDefendantAccount(fetchResultEntity(
                    entity.getEnforcementOverrideResultId())))
                .enforcer(enforcerDefendantAccountMapper.toDto(fetchEnforcerEntity(entity)))
                .lja(OpalDefendantAccountBuilders.buildLjaDefendantAccount(fetchLja(entity)))
                .build();
        }
    }

    // These 'DB' methods are focused purely on fetching relevant entities from the DB without any mapping.

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    private DebtorDetailEntity fetchDebtorDetails(PartyEntity party) {
        return debtorDetailRepository.findByPartyId(party.getPartyId()).orElse(null);
    }

    private EnforcementEntity fetchEnforcementMostRecent(DefendantAccountEntity entity) {
        return enforcementRepository.findFirstByDefendantAccountIdAndResultIdOrderByPostedDateDesc(
            entity.getDefendantAccountId(), entity.getLastEnforcement()).orElse(null);
    }

    //Deprecated - use OpalDefendantAccountEnforcementService
    //TODO - Remove once OpalDefendantAccountEnforcementService is in use
    @Override
    @Transactional(readOnly = true)
    public EnforcementStatus getEnforcementStatus(Long defendantAccountId) {

        log.debug(":getEnforcementStatus: def acc: {}",  defendantAccountId);

        DefendantAccountEntity defendantEntity = defendantAccountRepositoryService.findById(defendantAccountId);
        DefendantAccountPartiesEntity defendantParty = filterDefendantParty(defendantEntity);
        EnforcementEntity recentEnforcement = fetchEnforcementMostRecent(defendantEntity);

        return buildEnforcementStatus(
            defendantEntity,
            defendantParty,
            fetchDebtorDetails(defendantParty.getParty()),
            recentEnforcement != null ? recentEnforcement.getResult() : null,
            buildEnforcementOverride(defendantEntity),
            buildEnforcementAction(recentEnforcement,
                recentEnforcement != null
                    ? enforcerRepository.findByEnforcerId(recentEnforcement.getEnforcerId())
                    : null));
    }

    private void applyCommentAndNotes(DefendantAccountEntity managed, CommentsAndNotesCommon notes, String postedBy) {

        // Build a combined text block for the NOTES table (audit/history)
        final String combined = Stream.of(
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

        // Persist values on the main defendant_accounts table
        managed.setAccountComments(notes.getAccountComment());
        managed.setAccountNote1(notes.getFreeTextNote1());
        managed.setAccountNote2(notes.getFreeTextNote2());
        managed.setAccountNote3(notes.getFreeTextNote3());

        noteRepository.save(
            OpalDefendantAccountBuilders.buildNoteEntity(managed, combined, postedBy, LocalDateTime.now(clock)));
        log.debug(":applyCommentAndNotes: saved note for account {}", managed.getDefendantAccountId());
    }

    private void applyEnforcementCourt(DefendantAccountEntity entity, EnforcementCourtDefendantAccount courtRef) {
        Long courtId = courtRef.getCourtId();
        if (courtId == null) {
            throw new IllegalArgumentException("enforcement_court.court_id is required");
        }
        CourtEntity court = courtLiteRepository.findById(courtId)
            .orElseThrow(() -> new EntityNotFoundException("Court not found: " + courtId));
        entity.setEnforcingCourt(court);
        log.debug(":applyEnforcementCourt: accountId={}, courtId={}",
            entity.getDefendantAccountId(), court.getCourtId());
    }

    private boolean hasNonZeroBalance(DefendantAccountSummaryDto dto) {
        return dto.getAccountBalance() != null && dto.getAccountBalance().compareTo(BigDecimal.ZERO) != 0;
    }

    private ResultEntity fetchResultEntity(String resultId) {
        return Optional.ofNullable(resultId).flatMap(resultRepository::findById).orElse(null);
    }

    private EnforcerEntity fetchEnforcerEntity(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideEnforcerId())
            .flatMap(enforcerRepository::findById)
            .filter(enforcer -> Objects.nonNull(enforcer.getEnforcerId()))
            .orElse(null);
    }

    private LocalJusticeAreaEntity fetchLja(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideTfoLjaId())
            .flatMap(localJusticeAreaRepository::findById)
            .orElse(null);
    }
}
