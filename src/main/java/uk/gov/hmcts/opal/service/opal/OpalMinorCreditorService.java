package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItem;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryItemType;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.generated.model.GetMinorCreditorHistory200Response;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchDefendantMinorCreditor;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountSearchResultMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.PostMinorCreditorAccountSearchRequestMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PostMinorCreditorAccountsSearchResponseMinorCreditor;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountHeaderEntityMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountResponseMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorAccountUpdateMapper;
import uk.gov.hmcts.opal.mapper.MinorCreditorHistoryItemMapper;
import uk.gov.hmcts.opal.mapper.response.MinorCreditorAccountAtAGlanceResponseMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountAtAGlanceRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorAccountHeaderRepository;
import uk.gov.hmcts.opal.repository.MinorCreditorRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.MinorCreditorSpecs;
import uk.gov.hmcts.opal.service.iface.MinorCreditorServiceInterface;
import uk.gov.hmcts.opal.util.VersionUtils;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class OpalMinorCreditorService implements MinorCreditorServiceInterface {

    private static final LocalDateTime MIN_HISTORY_POSTED_DATE = LocalDateTime.of(1, Month.JANUARY, 1, 0, 0);
    private static final LocalDateTime MAX_HISTORY_POSTED_DATE = LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 59, 59);
    private static final String MINOR_CREDITOR_ACCOUNT_NOT_FOUND = "Minor creditor account not found: ";

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
    private final MinorCreditorHistoryItemMapper historyItemMapper;
    private final MinorCreditorAccountAtAGlanceResponseMapper atAGlanceResponseMapper;
    private final EntityManager em;
    private final MinorCreditorSpecs specs = new MinorCreditorSpecs();

    @Override
    public PostMinorCreditorAccountsSearchResponseMinorCreditor searchMinorCreditors(
        PostMinorCreditorAccountSearchRequestMinorCreditor criteria) {
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
                MINOR_CREDITOR_ACCOUNT_NOT_FOUND + minorCreditorAccountId
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
                MINOR_CREDITOR_ACCOUNT_NOT_FOUND + minorCreditorAccountId
            ));

        if (creditorAccount.getCreditorAccountType() == null || !creditorAccount.getCreditorAccountType()
            .isMinorCreditor()) {
            throw new EntityNotFoundException("Account is not a minor creditor account: " + minorCreditorAccountId);
        }

        List<MinorCreditorHistoryItem> historyItems = getMinorCreditorHistoryItems(minorCreditorAccountId, filters);

        return GetMinorCreditorHistoryResponse.builder()
            .payload(new GetMinorCreditorHistory200Response().historyItems(historyItems.stream()
                .sorted(MinorCreditorHistoryItem.ORDERING)
                .map(MinorCreditorHistoryItem::responseItem)
                .toList()))
            .version(creditorAccount.getVersion())
            .build();
    }

    private List<MinorCreditorHistoryItem> getMinorCreditorHistoryItems(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters) {

        List<MinorCreditorHistoryItem> historyItems = new ArrayList<>();
        LocalDateTime postedFromInclusive = postedFromInclusive(filters);
        LocalDateTime postedToExclusive = postedToExclusive(filters);
        if (filters.includes(MinorCreditorHistoryItemType.AMENDMENT)) {
            amendmentRepository.findMinorCreditorHistory(
                String.valueOf(minorCreditorAccountId),
                postedFromInclusive,
                postedToExclusive
            ).stream().map(historyItemMapper::toHistoryItem).forEach(historyItems::add);
        }
        if (filters.includes(MinorCreditorHistoryItemType.NOTE)) {
            noteRepository.findMinorCreditorHistory(
                String.valueOf(minorCreditorAccountId),
                postedFromInclusive,
                postedToExclusive
            ).stream().map(historyItemMapper::toHistoryItem).forEach(historyItems::add);
        }
        if (filters.includes(MinorCreditorHistoryItemType.FINANCIAL)) {
            creditorTransactionRepository.findMinorCreditorHistory(
                minorCreditorAccountId,
                postedFromInclusive,
                postedToExclusive
            ).stream().map(historyItemMapper::toHistoryItem).forEach(historyItems::add);
        }
        return historyItems;
    }

    private LocalDateTime postedFromInclusive(MinorCreditorHistoryFilters filters) {
        return filters.postedFromInclusive() == null ? MIN_HISTORY_POSTED_DATE : filters.postedFromInclusive();
    }

    private LocalDateTime postedToExclusive(MinorCreditorHistoryFilters filters) {
        return filters.postedToExclusive() == null ? MAX_HISTORY_POSTED_DATE : filters.postedToExclusive();
    }

    @Override
    @Transactional(readOnly = true)
    public MinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId) {
        log.debug(":getMinorCreditorAtAGlance (Opal): minorCreditorId={}", minorCreditorId);

        MinorCreditorAccountAtAGlanceEntity minorCreditorEntity =
            minorCreditorAccountAtAGlanceRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException(
                MINOR_CREDITOR_ACCOUNT_NOT_FOUND + minorCreditorId
            ));
        PartyEntity partyEntity = partyRepository.findById(minorCreditorEntity.getPartyId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Party not found: " + minorCreditorEntity.getPartyId()
            ));

        MinorCreditorAccountAtAGlanceResponse response =
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
                    MINOR_CREDITOR_ACCOUNT_NOT_FOUND + minorCreditorAccountId
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
                MINOR_CREDITOR_ACCOUNT_NOT_FOUND + minorCreditorAccountId));

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

    private MinorCreditorAccountSearchResultMinorCreditor toCreditorAccountDto(MinorCreditorEntity entity) {
        return MinorCreditorAccountSearchResultMinorCreditor.builder()
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

    private MinorCreditorAccountSearchDefendantMinorCreditor toDefendantDto(MinorCreditorEntity entity) {
        return MinorCreditorAccountSearchDefendantMinorCreditor.builder()
            .defendantAccountId(entity.getDefendantAccountId() != null
                                    ? String.valueOf(entity.getDefendantAccountId()) : null)
            .organisation(entity.isOrganisation())
            .organisationName(entity.getDefendantOrganisationName())
            .firstnames(entity.getDefendantFornames())
            .surname(entity.getDefendantSurname())
            .build();
    }

    private PostMinorCreditorAccountsSearchResponseMinorCreditor toResponse(List<MinorCreditorEntity> entities) {
        List<MinorCreditorAccountSearchResultMinorCreditor> accounts = entities.stream()
            .map(this::toCreditorAccountDto)
            .toList();

        return PostMinorCreditorAccountsSearchResponseMinorCreditor.builder()
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
