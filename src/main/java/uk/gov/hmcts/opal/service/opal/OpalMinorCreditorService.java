package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.CreditorAccountDto;
import uk.gov.hmcts.opal.dto.DefendantDto;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.AmendmentTypeCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionDetailsHistory;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionStatusReferenceCommon;
import uk.gov.hmcts.opal.generated.model.CreditorTransactionTypeReferenceCommon;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MinorCreditorHistoryItemHistory;
import uk.gov.hmcts.opal.generated.model.NoteDetailsHistory;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PostedDetailsCommon;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountUpdateMapper;
import uk.gov.hmcts.opal.mapper.response.GetMinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.MinorCreditorSpecs;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorAmendmentHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorTransactionHistoryProjection;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface {

    private static final LocalDateTime MIN_HISTORY_POSTED_DATE = LocalDateTime.of(1, 1, 1, 0, 0);
    private static final LocalDateTime MAX_HISTORY_POSTED_DATE = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    private final MinorCreditorRepository minorCreditorRepository;
    private final MinorCreditorAccountHeaderRepository minorCreditorAccountHeaderRepository;
    private final MinorCreditorAccountAtAGlanceRepository minorCreditorAccountAtAGlanceRepository;
    private final CreditorAccountRepository creditorAccountRepository;
    private final PartyRepository partyRepository;
    private final AmendmentRepository amendmentRepository;
    private final NoteRepository noteRepository;
    private final CreditorTransactionRepository creditorTransactionRepository;
    private final AmendmentService amendmentService;
    private final MinorCreditorAccountHeaderEntityMapper headerSummaryMapper;
    private final MinorCreditorAccountUpdateMapper updateMapper;
    private final MinorCreditorAccountResponseMapper responseMapper;
    private final GetMinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;
    private final EntityManager em;
    private final MinorCreditorSpecs specs = new MinorCreditorSpecs();

    @Override
    public PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch criteria) {
        Specification<MinorCreditorEntity> spec = specs.findBySearchCriteria(criteria);
        List<MinorCreditorEntity> results = minorCreditorRepository.findAll(spec);
        return toResponse(results);
    }

    @Override
    @Transactional(readOnly = true)
    public MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId) {
        log.debug(":getMinorCreditorAccount (Opal): minorCreditorAccountId={}", minorCreditorAccountId);

        CreditorAccountEntity creditorAccount = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId
            ));

        if (creditorAccount.getCreditorAccountType() == null || !creditorAccount.getCreditorAccountType()
            .isMinorCreditor()) {
            throw new EntityNotFoundException("Account is not a minor creditor account: " + minorCreditorAccountId);
        }

        PartyEntity party = partyRepository.findById(creditorAccount.getMinorCreditorPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found for minor creditor account: " + creditorAccount.getCreditorAccountId()));

        MinorCreditorAccountResponse response = responseMapper.toMinorCreditorAccountResponse(creditorAccount, party);
        response.setVersion(creditorAccount.getVersion());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GetMinorCreditorHistoryResponse getMinorCreditorHistory(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {
        log.debug(":getMinorCreditorHistory (Opal): minorCreditorAccountId={}", minorCreditorAccountId);

        CreditorAccountEntity creditorAccount = creditorAccountRepository.findById(minorCreditorAccountId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId
            ));

        if (creditorAccount.getCreditorAccountType() == null || !creditorAccount.getCreditorAccountType()
            .isMinorCreditor()) {
            throw new EntityNotFoundException("Account is not a minor creditor account: " + minorCreditorAccountId);
        }

        List<MappedMinorCreditorHistoryItem> historyItems = getMinorCreditorHistoryItems(minorCreditorAccountId,
                                                                                        filters);

        return GetMinorCreditorHistoryResponse.builder()
            .payload(new GetMinorCreditorHistory200Response().historyItems(historyItems.stream()
                .sorted(MappedMinorCreditorHistoryItem.ORDERING)
                .map(MappedMinorCreditorHistoryItem::historyItem)
                .toList()))
            .version(creditorAccount.getVersion())
            .build();
    }

    private List<MappedMinorCreditorHistoryItem> getMinorCreditorHistoryItems(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {

        List<MappedMinorCreditorHistoryItem> historyItems = new ArrayList<>();
        LocalDateTime postedFromInclusive = postedFromInclusive(filters);
        LocalDateTime postedToExclusive = postedToExclusive(filters);
        if (filters.includes(MinorCreditorHistoryItemType.AMENDMENT)) {
            amendmentRepository.findMinorCreditorHistory(
                String.valueOf(minorCreditorAccountId),
                postedFromInclusive,
                postedToExclusive
            ).stream().map(this::toAmendmentHistoryItem).forEach(historyItems::add);
        }
        if (filters.includes(MinorCreditorHistoryItemType.NOTE)) {
            noteRepository.findMinorCreditorHistory(
                String.valueOf(minorCreditorAccountId),
                postedFromInclusive,
                postedToExclusive
            ).stream().map(this::toNoteHistoryItem).forEach(historyItems::add);
        }
        if (filters.includes(MinorCreditorHistoryItemType.FINANCIAL)) {
            creditorTransactionRepository.findMinorCreditorHistory(
                minorCreditorAccountId,
                postedFromInclusive,
                postedToExclusive
            ).stream().map(this::toFinancialHistoryItem).forEach(historyItems::add);
        }
        return historyItems;
    }

    private LocalDateTime postedFromInclusive(MinorCreditorHistoryFilters filters) {
        return filters.postedFromInclusive() == null ? MIN_HISTORY_POSTED_DATE : filters.postedFromInclusive();
    }

    private LocalDateTime postedToExclusive(MinorCreditorHistoryFilters filters) {
        return filters.postedToExclusive() == null ? MAX_HISTORY_POSTED_DATE : filters.postedToExclusive();
    }

    private MappedMinorCreditorHistoryItem toAmendmentHistoryItem(
        MinorCreditorAmendmentHistoryProjection projection) {
        return new MappedMinorCreditorHistoryItem(
            MinorCreditorHistoryItemType.AMENDMENT,
            projection.getAmendmentId(),
            projection.getPostedDate(),
            new MinorCreditorHistoryItemHistory()
                .postedDetails(postedDetails(projection.getPostedDate(), projection.getPostedBy(),
                                             projection.getPostedByName()))
                .type(MinorCreditorHistoryItemHistory.TypeEnum.AMENDMENT)
                .details(new AmendmentTypeCommon()
                             .attributeName(projection.getAttributeName())
                             .oldValue(projection.getOldValue())
                             .newValue(projection.getNewValue()))
                .amount(null)
        );
    }

    private MappedMinorCreditorHistoryItem toNoteHistoryItem(MinorCreditorNoteHistoryProjection projection) {
        return new MappedMinorCreditorHistoryItem(
            MinorCreditorHistoryItemType.NOTE,
            projection.getNoteId(),
            projection.getPostedDate(),
            new MinorCreditorHistoryItemHistory()
                .postedDetails(postedDetails(projection.getPostedDate(), projection.getPostedBy(),
                                             projection.getPostedByName()))
                .type(MinorCreditorHistoryItemHistory.TypeEnum.NOTE)
                .details(new NoteDetailsHistory().noteText(projection.getNoteText()))
                .amount(null)
        );
    }

    private MappedMinorCreditorHistoryItem toFinancialHistoryItem(
        MinorCreditorTransactionHistoryProjection projection) {
        return new MappedMinorCreditorHistoryItem(
            MinorCreditorHistoryItemType.FINANCIAL,
            projection.getCreditorTransactionId(),
            projection.getPostedDate(),
            new MinorCreditorHistoryItemHistory()
                .postedDetails(postedDetails(projection.getPostedDate(), projection.getPostedBy(),
                                             projection.getPostedByName()))
                .type(MinorCreditorHistoryItemHistory.TypeEnum.FINANCIAL)
                .details(new CreditorTransactionDetailsHistory()
                             .transactionType(creditorTransactionType(projection.getTransactionType()))
                             .paymentReference(projection.getPaymentReference())
                             .status(creditorTransactionStatus(projection.getStatus()))
                             .statusDate(projection.getStatusDate())
                             .associatedRecordType(projection.getAssociatedRecordType())
                             .associatedRecordId(projection.getAssociatedRecordId())
                             .accountNumber(projection.getAccountNumber())
                             .defendantAccountNumber(projection.getDefendantAccountNumber())
                             .defendantAccountId(projection.getDefendantAccountId()))
                .amount(projection.getTransactionAmount())
        );
    }

    private PostedDetailsCommon postedDetails(LocalDateTime postedDate, String postedBy, String postedByName) {
        return new PostedDetailsCommon()
            .postedDate(postedDate.toLocalDate())
            .postedBy(postedBy)
            .postedByName(postedByName);
    }

    private CreditorTransactionTypeReferenceCommon creditorTransactionType(String transactionType) {
        return new CreditorTransactionTypeReferenceCommon()
            .transactionType(CreditorTransactionTypeReferenceCommon.TransactionTypeEnum.fromValue(transactionType))
            .transactionTypeDisplayName(transactionType);
    }

    private CreditorTransactionStatusReferenceCommon creditorTransactionStatus(String status) {
        if (status == null) {
            return null;
        }
        return new CreditorTransactionStatusReferenceCommon()
            .creditorTransactionStatus(
                CreditorTransactionStatusReferenceCommon.CreditorTransactionStatusEnum.fromValue(status))
            .creditorTransactionStatusDisplayName(status);
    }

    private record MappedMinorCreditorHistoryItem(
        MinorCreditorHistoryItemType sourceType,
        Long sourceId,
        LocalDateTime postedDate,
        MinorCreditorHistoryItemHistory historyItem) {

        private static final Comparator<MappedMinorCreditorHistoryItem> ORDERING =
            Comparator.comparing(MappedMinorCreditorHistoryItem::postedDate).reversed()
                .thenComparing(MappedMinorCreditorHistoryItem::sourceType)
                .thenComparing(MappedMinorCreditorHistoryItem::sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId) {
        log.debug(":getMinorCreditorAtAGlance (Opal): minorCreditorId={}", minorCreditorId);

        MinorCreditorAccountAtAGlanceEntity minorCreditorEntity =
            minorCreditorAccountAtAGlanceRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorId
            ));
        PartyEntity partyEntity = partyRepository.findById(minorCreditorEntity.getPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found: " + minorCreditorEntity.getPartyId()
            ));

        GetMinorCreditorAccountAtAGlanceResponse response =
            atAGlanceResponseMapper.toDto(minorCreditorEntity, partyEntity);

        if (minorCreditorEntity.getVersionNumber() != null) {
            response.setVersion(BigInteger.valueOf(minorCreditorEntity.getVersionNumber()));
        }

        return response;
    }

    @Override
    public GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(Long minorCreditorAccountId) {
        log.debug(":getHeaderSummary (Opal): minorCreditorAccountId={}", minorCreditorAccountId);

        MinorCreditorAccountHeaderEntity entity =
            minorCreditorAccountHeaderRepository.findById(minorCreditorAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Minor creditor account not found: " + minorCreditorAccountId
                ));

        long partyId = entity.getPartyId();

        PartyEntity partyEntity =
            partyRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Minor creditor party not found: " + partyId
                ));
        return headerSummaryMapper.toResponse(entity, partyEntity);
    }

    @Override
    @Transactional
    public MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger ifMatch,
        String postedBy,
        String postedByName,
        Short businessUnitId) {
        log.debug(":updateMinorCreditorAccount (Opal): id={}", minorCreditorAccountId);

        CreditorAccountEntity creditorAccount = creditorAccountRepository
            .findByCreditorAccountIdAndBusinessUnitId(minorCreditorAccountId, businessUnitId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Minor creditor account not found: " + minorCreditorAccountId));

        if (creditorAccount.getCreditorAccountType() == null || !creditorAccount.getCreditorAccountType()
            .isMinorCreditor()) {
            throw new EntityNotFoundException("Account is not a minor creditor account: " + minorCreditorAccountId);
        }
        if (creditorAccount.getVersion() == null) {
            throw new ResourceConflictException("CreditorAccount", minorCreditorAccountId,
                "Current account version is missing", null);
        }
        VersionUtils.verifyIfMatch(creditorAccount, ifMatch, minorCreditorAccountId, "updateMinorCreditorAccount");

        PartyEntity party = partyRepository.findById(creditorAccount.getMinorCreditorPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found for minor creditor account: " + creditorAccount.getCreditorAccountId()));

        validatePartyId(request.getPartyDetails().getPartyId(), party.getPartyId());

        amendmentService.auditInitialiseStoredProc(minorCreditorAccountId, RecordType.CREDITOR_ACCOUNTS);

        updateMapper.updateParty(request.getPartyDetails(), request.getAddress(), party);

        creditorAccount.setBankAccountName(request.getPayment().getAccountName());
        creditorAccount.setBankSortCode(request.getPayment().getSortCode());
        creditorAccount.setBankAccountNumber(request.getPayment().getAccountNumber());
        creditorAccount.setBankAccountReference(request.getPayment().getAccountReference());
        creditorAccount.setPayByBacs(request.getPayment().getPayByBacs());
        creditorAccount.setHoldPayout(request.getPayment().getHoldPayment());
        creditorAccount.setLastChangedDate(LocalDateTime.now());

        partyRepository.save(party);
        creditorAccountRepository.save(creditorAccount);

        em.lock(creditorAccount, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        em.flush();

        amendmentService.auditFinaliseStoredProc(
            minorCreditorAccountId,
            RecordType.CREDITOR_ACCOUNTS,
            creditorAccount.getBusinessUnitId(),
            postedBy,
            postedByName,
            null,
            "ACCOUNT_ENQUIRY"
        );

        MinorCreditorAccountResponse response = responseMapper.toMinorCreditorAccountResponse(creditorAccount, party);
        response.setVersion(creditorAccount.getVersion());
        return response;
    }

    private CreditorAccountDto toCreditorAccountDto(MinorCreditorEntity entity) {
        return CreditorAccountDto.builder()
            .creditorAccountId(String.valueOf(entity.getCreditorId()))
            .accountNumber(entity.getAccountNumber())
            .organisation(entity.isOrganisation())
            .organisationName(entity.getOrganisationName())
            .firstnames(entity.getForenames())
            .surname(entity.getSurname())
            .addressLine1(entity.getAddressLine1())
            .postcode(entity.getPostCode())
            .businessUnitName(entity.getBusinessUnitName())
            .businessUnitId(String.valueOf(entity.getBusinessUnitId()))
            .accountBalance(java.util.Optional.ofNullable(entity.getCreditorAccountBalance())
                                .map(BigDecimal::valueOf)
                                .orElse(BigDecimal.ZERO))
            .defendant(toDefendantDto(entity))
            .build();
    }

    private DefendantDto toDefendantDto(MinorCreditorEntity entity) {
        return DefendantDto.builder()
            .defendantAccountId(entity.getDefendantAccountId() != null
                                    ? String.valueOf(entity.getDefendantAccountId()) : null)
            .organisation(entity.isOrganisation())
            .organisationName(entity.getDefendantOrganisationName())
            .firstnames(entity.getDefendantFornames())
            .surname(entity.getDefendantSurname())
            .build();
    }

    private PostMinorCreditorAccountsSearchResponse toResponse(List<MinorCreditorEntity> entities) {
        List<CreditorAccountDto> accounts = entities.stream()
            .map(this::toCreditorAccountDto)
            .toList();

        return PostMinorCreditorAccountsSearchResponse.builder()
            .count(accounts.size())
            .creditorAccounts(accounts.isEmpty() ? null : accounts)
            .build();
    }

    private void validatePartyId(String requestPartyId, Long existingPartyId) {
        if (requestPartyId == null || requestPartyId.isBlank()) {
            throw new IllegalArgumentException("party_details.party_id must be provided");
        }
        try {
            if (!Long.valueOf(requestPartyId).equals(existingPartyId)) {
                throw new IllegalArgumentException("party_details.party_id does not match account");
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid party_details.party_id format", ex);
        }
    }
}
