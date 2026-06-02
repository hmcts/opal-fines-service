package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.mapper.history.AmendmentEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.DefendantTransactionEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.EnforcementEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.NoteEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.PaymentTermsEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.OpalDefendantAccountHistoryService")
public class OpalDefendantAccountHistoryService {

    private static final String DEFENDANT_ACCOUNT_NOT_FOUND = "Defendant Account not found with id: ";

    private final DefendantAccountRepository defendantAccountRepository;

    private final AmendmentRepository amendmentRepository;

    private final EnforcementRepository enforcementRepository;

    private final NoteRepository noteRepository;

    private final PaymentTermsRepository paymentTermsRepository;

    private final DefendantTransactionRepository defendantTransactionRepository;

    private final ImpositionRepository impositionRepository;

    private final AmendmentEntityHistoryMapper amendmentEntityHistoryMapper;

    private final EnforcementEntityHistoryMapper enforcementEntityHistoryMapper;

    private final NoteEntityHistoryMapper noteEntityHistoryMapper;

    private final PaymentTermsEntityHistoryMapper paymentTermsEntityHistoryMapper;

    private final DefendantTransactionEntityHistoryMapper defendantTransactionEntityHistoryMapper;

    @Transactional(readOnly = true)
    public DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        log.debug(":getHistorySources: Opal mode - ID: {}", defendantAccountId);

        DefendantAccountEntity defendantAccount = defendantAccountRepository
            .findByDefendantAccountId(defendantAccountId)
            .orElseThrow(() -> new EntityNotFoundException(DEFENDANT_ACCOUNT_NOT_FOUND + defendantAccountId));

        DefendantAccountHistorySources sources = DefendantAccountHistorySources.builder()
            .amendments(getAmendments(defendantAccountId, filter))
            .enforcements(getEnforcements(defendantAccountId, filter))
            .notes(getNotes(defendantAccountId, filter))
            .paymentTerms(getPaymentTerms(defendantAccountId, filter))
            .transactions(getTransactions(defendantAccountId, filter))
            .build();

        return DefendantAccountHistoryResponse.builder()
            .version(defendantAccount.getVersion())
            .historyItems(toHistoryItems(sources))
            .build();
    }

    private List<DefendantAccountHistoryItem> toHistoryItems(DefendantAccountHistorySources sources) {
        DefendantTransactionHistoryAssociations transactionAssociations =
            getTransactionHistoryAssociations(sources.getTransactions());

        return Stream.of(
                sources.getAmendments().stream().map(amendmentEntityHistoryMapper::toHistoryItem),
                sources.getEnforcements().stream().map(enforcementEntityHistoryMapper::toHistoryItem),
                sources.getNotes().stream().map(noteEntityHistoryMapper::toHistoryItem),
                sources.getPaymentTerms().stream().map(paymentTermsEntityHistoryMapper::toHistoryItem),
                sources.getTransactions().stream()
                    .map(transaction -> toTransactionHistoryItem(transaction, transactionAssociations))
            )
            .flatMap(stream -> stream)
            .sorted(newestFirstHistoryItemComparator())
            .toList();
    }

    private DefendantTransactionHistoryAssociations getTransactionHistoryAssociations(
        List<DefendantTransactionEntity> transactions) {

        Set<Long> defendantAccountIds = getAssociatedRecordIds(transactions, AssociatedRecordType.DEFENDANT_ACCOUNTS);
        Set<Long> impositionIds = getAssociatedRecordIds(transactions, AssociatedRecordType.IMPOSITIONS);

        return DefendantTransactionHistoryAssociations.builder()
            .defendantAccounts(defendantAccountRepository.findAllById(defendantAccountIds).stream()
                .collect(Collectors.toMap(DefendantAccountEntity::getDefendantAccountId, Function.identity())))
            .impositions(impositionRepository.findAllById(impositionIds).stream()
                .collect(Collectors.toMap(ImpositionEntity::getImpositionId, Function.identity())))
            .build();
    }

    private Set<Long> getAssociatedRecordIds(List<DefendantTransactionEntity> transactions,
                                             AssociatedRecordType associatedRecordType) {
        return transactions.stream()
            .filter(transaction -> associatedRecordType.equals(transaction.getAssociatedRecordType()))
            .map(transaction -> parseAssociatedRecordId(transaction.getAssociatedRecordId()))
            .flatMap(Optional::stream)
            .collect(Collectors.toSet());
    }

    private DefendantAccountHistoryItem toTransactionHistoryItem(DefendantTransactionEntity transaction,
                                                                 DefendantTransactionHistoryAssociations associations) {
        DefendantAccountHistoryItem historyItem = defendantTransactionEntityHistoryMapper.toHistoryItem(transaction);

        if (historyItem.getDetails() instanceof DefendantTransactionDetails details) {
            enrichTransactionDetails(transaction, details, associations);
        }

        return historyItem;
    }

    private void enrichTransactionDetails(DefendantTransactionEntity transaction, DefendantTransactionDetails details,
                                          DefendantTransactionHistoryAssociations associations) {
        AssociatedRecordType associatedRecordType = transaction.getAssociatedRecordType();
        Optional<Long> associatedRecordId = parseAssociatedRecordId(transaction.getAssociatedRecordId());

        if (associatedRecordType == null || associatedRecordId.isEmpty()) {
            return;
        }

        switch (associatedRecordType) {
            case DEFENDANT_ACCOUNTS -> enrichDefendantAccountTransactionDetails(associatedRecordId.get(), details,
                associations.getDefendantAccounts());
            case IMPOSITIONS -> enrichImpositionTransactionDetails(associatedRecordId.get(), details,
                associations.getImpositions());
            default -> {
            }
        }
    }

    private void enrichDefendantAccountTransactionDetails(Long defendantAccountId,
                                                          DefendantTransactionDetails details,
                                                          Map<Long, DefendantAccountEntity> defendantAccounts) {
        Optional.ofNullable(defendantAccounts.get(defendantAccountId)).ifPresent(defendantAccount -> {
            details.setAccountNumber(defendantAccount.getAccountNumber());
            details.setSendingCourt(defendantAccount.getOriginatorName());
        });
    }

    private void enrichImpositionTransactionDetails(Long impositionId, DefendantTransactionDetails details,
                                                    Map<Long, ImpositionEntity> impositions) {
        Optional.ofNullable(impositions.get(impositionId)).ifPresent(imposition -> {
            details.setImpositionDate(imposition.getPostedDate().toLocalDate());
            details.setImpositionCode(imposition.getResultId());
            details.setAmountImposed(imposition.getImposedAmount());
        });
    }

    private LocalDateTime toLocalDateTime(ImpositionEntity imposition) {
        return imposition.getPostedDate();
    }

    private Optional<Long> parseAssociatedRecordId(String associatedRecordId) {
        try {
            return associatedRecordId == null ? Optional.empty() : Optional.of(Long.valueOf(associatedRecordId));
        } catch (NumberFormatException ex) {
            log.debug(":parseAssociatedRecordId: unable to parse associated record id '{}'", associatedRecordId);
            return Optional.empty();
        }
    }

    private Comparator<DefendantAccountHistoryItem> newestFirstHistoryItemComparator() {
        return Comparator.comparing(
                DefendantAccountHistoryItem::getEventDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
            )
            .thenComparing(DefendantAccountHistoryItem::getType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(DefendantAccountHistoryItem::getSourceId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private List<AmendmentEntity> getAmendments(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.AMENDMENT)) {
            return List.of();
        }

        return amendmentRepository.findAll(allOf(
            amendmentForDefendantAccount(defendantAccountId),
            amendmentDateFrom(filter.getDateFrom()),
            amendmentDateTo(filter.getDateTo())
        ));
    }

    private List<EnforcementEntity> getEnforcements(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.ENFORCEMENT)) {
            return List.of();
        }

        return enforcementRepository.findAll(allOf(
            enforcementForDefendantAccount(defendantAccountId),
            enforcementDateFrom(filter.getDateFrom()),
            enforcementDateTo(filter.getDateTo())
        ));
    }

    private List<NoteEntity> getNotes(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.NOTE)) {
            return List.of();
        }

        return noteRepository.findDefendantAccountHistoryNotes(
            defendantAccountId.toString(),
            filter.getDateFrom(),
            filter.getDateTo()
        );
    }

    private List<PaymentTermsEntity> getPaymentTerms(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.PAYMENT_TERMS)) {
            return List.of();
        }

        return paymentTermsRepository.findAll(allOf(
            paymentTermsForDefendantAccount(defendantAccountId),
            paymentTermsDateFrom(filter.getDateFrom()),
            paymentTermsDateTo(filter.getDateTo())
        ));
    }

    private List<DefendantTransactionEntity> getTransactions(Long defendantAccountId,
                                                            DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.FINANCIAL)) {
            return List.of();
        }

        return defendantTransactionRepository.findAll(allOf(
            transactionForDefendantAccount(defendantAccountId),
            transactionDateFrom(filter.getDateFrom()),
            transactionDateTo(filter.getDateTo())
        ));
    }

    private Specification<AmendmentEntity> amendmentForDefendantAccount(Long defendantAccountId) {
        return (root, query, builder) -> builder.and(
            builder.equal(root.get("associatedRecordType"), AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel()),
            builder.equal(root.get("associatedRecordId"), defendantAccountId.toString())
        );
    }

    private Specification<AmendmentEntity> amendmentDateFrom(LocalDate dateFrom) {
        return dateFrom == null ? null
            : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("amendedDate"), atStartOfDay(dateFrom));
    }

    private Specification<AmendmentEntity> amendmentDateTo(LocalDate dateTo) {
        return dateTo == null ? null
            : (root, query, builder) -> builder.lessThan(root.get("amendedDate"), dayAfterStart(dateTo));
    }

    private Specification<EnforcementEntity> enforcementForDefendantAccount(Long defendantAccountId) {
        return (root, query, builder) -> builder.equal(root.get("defendantAccountId"), defendantAccountId);
    }

    private Specification<EnforcementEntity> enforcementDateFrom(LocalDate dateFrom) {
        return dateFrom == null ? null
            : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postedDate"), atStartOfDay(dateFrom));
    }

    private Specification<EnforcementEntity> enforcementDateTo(LocalDate dateTo) {
        return dateTo == null ? null
            : (root, query, builder) -> builder.lessThan(root.get("postedDate"), dayAfterStart(dateTo));
    }

    private Specification<PaymentTermsEntity> paymentTermsForDefendantAccount(Long defendantAccountId) {
        return (root, query, builder) -> builder.equal(
            root.get("defendantAccount").get("defendantAccountId"), defendantAccountId);
    }

    private Specification<PaymentTermsEntity> paymentTermsDateFrom(LocalDate dateFrom) {
        return dateFrom == null ? null
            : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postedDate"), atStartOfDay(dateFrom));
    }

    private Specification<PaymentTermsEntity> paymentTermsDateTo(LocalDate dateTo) {
        return dateTo == null ? null
            : (root, query, builder) -> builder.lessThan(root.get("postedDate"), dayAfterStart(dateTo));
    }

    private Specification<DefendantTransactionEntity> transactionForDefendantAccount(Long defendantAccountId) {
        return (root, query, builder) -> builder.equal(root.get("defendantAccountId"), defendantAccountId);
    }

    private Specification<DefendantTransactionEntity> transactionDateFrom(LocalDate dateFrom) {
        return dateFrom == null ? null
            : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postedDate"), atStartOfDay(dateFrom));
    }

    private Specification<DefendantTransactionEntity> transactionDateTo(LocalDate dateTo) {
        return dateTo == null ? null
            : (root, query, builder) -> builder.lessThan(root.get("postedDate"), dayAfterStart(dateTo));
    }

    private LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime dayAfterStart(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    @SafeVarargs
    private <T> Specification<T> allOf(Specification<T>... specifications) {
        return Specification.allOf(Stream.of(specifications)
            .filter(Objects::nonNull)
            .toList());
    }

    @Value
    @Builder
    public static class DefendantAccountHistorySources {

        List<AmendmentEntity> amendments;

        List<EnforcementEntity> enforcements;

        List<NoteEntity> notes;

        List<PaymentTermsEntity> paymentTerms;

        List<DefendantTransactionEntity> transactions;
    }

    @Value
    @Builder
    public static class DefendantTransactionHistoryAssociations {

        Map<Long, DefendantAccountEntity> defendantAccounts;

        Map<Long, ImpositionEntity> impositions;
    }
}
