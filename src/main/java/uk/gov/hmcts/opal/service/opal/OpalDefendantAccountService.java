package uk.gov.hmcts.opal.service.opal;

import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCollectionOrderCommon;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCommentsAndNotes;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildCourtReference;
import static uk.gov.hmcts.opal.service.opal.OpalDefendantAccountBuilders.buildEnforcementOverrideResultDefendantAccount;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
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
import uk.gov.hmcts.opal.controllers.advice.GlobalExceptionHandler.PaymentCardRequestAlreadyExistsException;
import uk.gov.hmcts.opal.dto.AddPaymentCardRequestResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.Checks;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.DefendantAccountSummaryDtoBuilder;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto.WarnError;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PaymentCardRequestEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountSummaryViewEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.entity.search.SearchConsolidatedEntity;
import uk.gov.hmcts.opal.entity.search.SearchDefendantAccount;
import uk.gov.hmcts.opal.exception.UnprocessableException;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementCourtDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountResponsePayload;
import uk.gov.hmcts.opal.mapper.DefendantAccountHeaderSummaryMapper;
import uk.gov.hmcts.opal.mapper.common.EnforcerDefendantAccountMapper;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
import uk.gov.hmcts.opal.repository.SearchDefendantBasicRepository;
import uk.gov.hmcts.opal.repository.SearchDefendantConsolidatedRepository;
import uk.gov.hmcts.opal.repository.jpa.SearchBasicEntitySpecs;
import uk.gov.hmcts.opal.repository.jpa.SearchConsolidatedEntitySpecs;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountHeaderViewRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountSummaryViewRepositoryService;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryService;
import uk.gov.hmcts.opal.service.iface.ReportEntryServiceInterface;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.LocalJusticeAreaRepositoryService;
import uk.gov.hmcts.opal.service.persistence.NoteRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentCardRequestRepositoryService;
import uk.gov.hmcts.opal.service.persistence.PaymentTermsRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    private static final int TOO_MANY_SEARCH_RESULTS = 100;

    private final SearchDefendantBasicRepository searchDefendantBasicRepository;
    private final SearchDefendantConsolidatedRepository searchConsolidatedRepository;

    private final SearchBasicEntitySpecs searchBasicEntitySpecs;
    private final SearchConsolidatedEntitySpecs searchConsolidatedEntitySpecs;

    private final AmendmentService amendmentService;

    private final EntityManager em;

    private final UserStateService userStateService;

    // Services
    private final DocumentService documentService;

    private final PaymentTermsService paymentTermsService;

    private final ReportEntryServiceInterface reportEntryService;

    private final DefendantAccountHistoryService defendantAccountHistoryService;

    // Mappers
    private final DefendantAccountHeaderSummaryMapper defendantAccountHeaderSummaryMapper;
    private final EnforcerDefendantAccountMapper enforcerDefendantAccountMapper;
    private final PaymentTermsMapper paymentTermsMapper;

    private final Clock clock;

    private final DefendantAccountRepositoryService defendantAccountRepositoryService;
    private final DefendantAccountHeaderViewRepositoryService defendantAccountHeaderViewRepositoryService;
    private final DefendantAccountSummaryViewRepositoryService defendantAccountSummaryViewRepositoryService;
    private final PaymentTermsRepositoryService paymentTermsRepositoryService;
    private final PaymentCardRequestRepositoryService paymentCardRequestRepositoryService;
    private final ResultRepositoryService resultRepositoryService;
    private final EnforcerRepositoryService enforcerRepositoryService;
    private final LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;
    private final NoteRepositoryService noteRepositoryService;
    private final CourtService courtService;
    private final EnforcementRepositoryService enforcementRepositoryService;

    //TODO - Remove once repository service is in use
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

    //TODO - Remove this once repository service is in use
    public DefendantAccountSummaryViewEntity getDefendantAccountSummaryViewById(long defendantAccountId) {
        return defendantAccountRepositoryService.findSummaryViewById(defendantAccountId);
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

        defendantAccountRepositoryService.save(entity);

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
        String displayName) {

        log.debug(":addPaymentCard (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = loadAndValidateAccount(defendantAccountId, businessUnitId);
        VersionUtils.verifyIfMatch(account, ifMatch, account.getDefendantAccountId(), "addPaymentCard");

        ensureNoExistingPaymentCardRequest(defendantAccountId);

        createPaymentCardRequest(defendantAccountId);

        updateDefendantAccountWithPcr(account, businessUnitUserId, displayName);

        // Minimal response
        return new AddPaymentCardRequestResponse(defendantAccountId);
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

    private EnforcementEntity fetchEnforcementMostRecent(DefendantAccountEntity entity) {
        return enforcementRepositoryService.getEnforcementMostRecent(
            entity.getDefendantAccountId(), entity.getLastEnforcement()).orElse(null);
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

        noteRepositoryService.save(
            OpalDefendantAccountBuilders.buildNoteEntity(managed, combined, postedBy, LocalDateTime.now(clock)));
        log.debug(":applyCommentAndNotes: saved note for account {}", managed.getDefendantAccountId());
    }

    private void applyEnforcementCourt(DefendantAccountEntity entity, EnforcementCourtDefendantAccount courtRef) {
        Long courtId = courtRef.getCourtId();
        if (courtId == null) {
            throw new IllegalArgumentException("enforcement_court.court_id is required");
        }
        CourtEntity court = courtService.getCourtById(courtId);
        entity.setEnforcingCourt(court);
        log.debug(":applyEnforcementCourt: accountId={}, courtId={}",
            entity.getDefendantAccountId(), court.getCourtId());
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    @Override
    @Transactional
    public AddPaymentCardRequestResponse addPaymentCardRequest(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch) {

        log.debug(":addPaymentCardRequest (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        DefendantAccountEntity account = defendantAccountRepositoryService.findById(defendantAccountId);

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        AddPaymentCardRequestResponse paymentCardResponse = addPaymentCard(
            defendantAccountId,
            businessUnitId,
            businessUnitUserId,
            ifMatch,
            userStateService.getUserStateV1FromSecurityContext().getDisplayName()
        );

        auditComplete(defendantAccountId, account, businessUnitUserId,
            userStateService.getUserStateV1FromSecurityContext().getDisplayName());

        return paymentCardResponse;
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private DefendantAccountEntity loadAndValidateAccount(Long accountId, String buId) {
        DefendantAccountEntity account = defendantAccountRepositoryService.findById(accountId);
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
        if (paymentCardRequestRepositoryService.existsByDefendantAccountId(accountId)) {
            throw new PaymentCardRequestAlreadyExistsException(
                "DefendantAccountEntity",
                String.valueOf(accountId)
            );
        }
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private void createPaymentCardRequest(Long accountId) {
        PaymentCardRequestEntity pcr = PaymentCardRequestEntity.builder()
            .defendantAccountId(accountId)
            .build();
        paymentCardRequestRepositoryService.save(pcr);
    }

    //Deprecated - use DefendantAccountPaymentTermsService
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    private void updateDefendantAccountWithPcr(DefendantAccountEntity account,
        String businessUnitUserId,
        String displayName) {

        account.setPaymentCardRequested(true);
        account.setPaymentCardRequestedDate(LocalDate.now());
        account.setPaymentCardRequestedBy(businessUnitUserId);
        account.setPaymentCardRequestedByName(displayName);

        defendantAccountRepositoryService.save(account);
    }

    private void auditComplete(Long accountId,
        DefendantAccountEntity account,
        String businessUnitUserId,
        String postedByName) {

        Short buId = account.getBusinessUnit().getBusinessUnitId();

        amendmentService.auditFinaliseStoredProc(
            accountId,
            RecordType.DEFENDANT_ACCOUNTS,
            buId,
            businessUnitUserId,
            postedByName,
            account.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );
    }

    @Override
    @Transactional
    //TODO: Remove this method once OpalDefendantAccountPaymentTermsService is in use
    public GetDefendantAccountPaymentTermsResponse addPaymentTerms(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {
        log.debug(":addPaymentTerms (Opal): accountId={}, bu={}", defendantAccountId, businessUnitId);

        // Look up the defendant account
        DefendantAccountEntity defAccount = defendantAccountRepositoryService.findByIdForUpdate(defendantAccountId);

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
        // If not included in the request, infer these from the token/user context
        if (paymentTermsEntity.getPostedByUsername() == null) {
            paymentTermsEntity.setPostedByUsername(businessUnitUserId);
        }
        if (paymentTermsEntity.getPostedBy() == null) {
            paymentTermsEntity.setPostedBy(businessUnitUserId);
        }
        // Persist the new (active) PaymentTermsEntity
        final PaymentTermsEntity savedPaymentTerms = paymentTermsService.addPaymentTerm(paymentTermsEntity);

        // Update defendant account with any payment term related attributes
        addPaymentTerm(defAccount, addPaymentTermsRequest);

        clearLastEnforcementAction(defAccount);

        defendantAccountRepositoryService.save(defAccount);

        // If requestPaymentCardFlag is true: create a PaymentCardRequest row (if not already present)
        //  and update the defendant account PCR-related attributes (requested flag/date/by/byName).
        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(":addPaymentTerms: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch,
                userStateService.getUserStateV1FromSecurityContext().getDisplayName());
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
            savedPaymentTerms.getPostedByUsername(),
            defAccount.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    @Transactional
    public GetDefendantAccountPaymentTermsResponse addPaymentTermsPreservingLastEnforcement(Long defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String ifMatch,
        String authHeader,
        AddDefendantAccountPaymentTermsRequest addPaymentTermsRequest) {

        log.debug(":addPaymentTermsPreservingLastEnforcement (Opal): accountId={}, bu={}",
            defendantAccountId, businessUnitId);

        DefendantAccountEntity defAccount = defendantAccountRepositoryService.findByIdForUpdate(defendantAccountId);

        if (defAccount.getBusinessUnit() == null
            || defAccount.getBusinessUnit().getBusinessUnitId() == null
            || !String.valueOf(defAccount.getBusinessUnit().getBusinessUnitId()).equals(businessUnitId)) {
            throw new EntityNotFoundException("Defendant Account not found in business unit " + businessUnitId);
        }

        VersionUtils.verifyIfMatch(defAccount, ifMatch, defendantAccountId, "addPaymentTerms");

        amendmentService.auditInitialiseStoredProc(defendantAccountId, RecordType.DEFENDANT_ACCOUNTS);

        paymentTermsService.deactivateExistingActivePaymentTerms(defAccount.getDefendantAccountId());

        PaymentTermsEntity paymentTermsEntity = paymentTermsMapper.toEntity(addPaymentTermsRequest.getPaymentTerms());
        paymentTermsEntity.setDefendantAccount(defAccount);
        if (paymentTermsEntity.getPostedByUsername() == null) {
            paymentTermsEntity.setPostedByUsername(businessUnitUserId);
        }
        if (paymentTermsEntity.getPostedBy() == null) {
            paymentTermsEntity.setPostedBy(businessUnitUserId);
        }

        final PaymentTermsEntity savedPaymentTerms = paymentTermsService.addPaymentTerm(paymentTermsEntity);

        addPaymentTerm(defAccount, addPaymentTermsRequest);
        defendantAccountRepositoryService.save(defAccount);

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getRequestPaymentCard())) {
            log.debug(":addPaymentTermsPreservingLastEnforcement: Request Payment Card flag is TRUE for account {}",
                defAccount.getDefendantAccountId());
            addPaymentCard(defendantAccountId, businessUnitId, businessUnitUserId, ifMatch, authHeader);
        }

        if (Boolean.TRUE.equals(addPaymentTermsRequest.getGeneratePaymentTermsChangeLetter())) {
            log.debug(":addPaymentTermsPreservingLastEnforcement: Generate Payment Terms Change Letter flag is TRUE "
                    + "for account {}",
                defAccount.getDefendantAccountId());
            documentService.createDocumentInstance(defendantAccountId,
                defAccount.getBusinessUnit().getBusinessUnitId());
        }

        reportEntryService.createExtendTtpReportEntry(savedPaymentTerms.getPaymentTermsId(),
            defAccount.getBusinessUnit().getBusinessUnitId());

        log.debug(":addPaymentTermsPreservingLastEnforcement: saved payment terms id={} for account {}",
            savedPaymentTerms.getPaymentTermsId(), defAccount.getDefendantAccountId());

        amendmentService.auditFinaliseStoredProc(
            defAccount.getDefendantAccountId(),
            RecordType.DEFENDANT_ACCOUNTS,
            Short.parseShort(businessUnitId),
            businessUnitUserId,
            savedPaymentTerms.getPostedByUsername(),
            defAccount.getProsecutorCaseReference(),
            "ACCOUNT_ENQUIRY"
        );

        return OpalDefendantAccountBuilders.buildPaymentTermsResponse(savedPaymentTerms);
    }

    /**
     * Along with adding a new latest payment_terms we will also clear the last enforcement action on the account.
     * The only time it doesn't is when the last enforcement has result.extend_ttp_preserve_last_enf = TRUE
     */
    private void clearLastEnforcementAction(DefendantAccountEntity defAccount) {
        // Retrieve most recent enforcement action for this account using the result
        EnforcementEntity mostRecentEnforcement = fetchEnforcementMostRecent(defAccount);

        if (mostRecentEnforcement != null) {
            ResultEntity resultEntityLite = resultRepositoryService.getResultById(mostRecentEnforcement.getResultId())
                .orElseThrow(() -> new EntityNotFoundException("'Result' not found with id: "
                    + mostRecentEnforcement.getResultId()));

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

    private boolean hasNonZeroBalance(DefendantAccountSummaryDto dto) {
        return dto.getAccountBalance() != null && dto.getAccountBalance().compareTo(BigDecimal.ZERO) != 0;
    }

    private ResultEntity fetchResultEntity(String resultId) {
        return resultRepositoryService.getResultById(resultId).orElse(null);
    }

    private EnforcerEntity fetchEnforcerEntity(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideEnforcerId())
            .flatMap(enforcerRepositoryService::findById)
            .orElse(null);
    }

    private LocalJusticeAreaEntity fetchLja(DefendantAccountEntity entity) {
        return Optional.ofNullable(entity.getEnforcementOverrideTfoLjaId())
            .flatMap(id -> localJusticeAreaRepositoryService.getLjaById(id))
            .orElse(null);
    }
}
