package uk.gov.hmcts.opal.service.opal.history.source;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.DefendantTransactionDetails;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendanttransaction.DefendantTransactionEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.mapper.history.DefendantTransactionEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.DefendantTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DefendantTransactionHistorySourceService")
public class DefendantTransactionHistorySourceService extends HistorySourceSpecificationSupport
    implements AccountHistorySource {

    private final DefendantTransactionRepository defendantTransactionRepository;
    private final DefendantAccountRepository defendantAccountRepository;
    private final ImpositionRepository impositionRepository;
    private final DefendantTransactionEntityHistoryMapper defendantTransactionEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public HistoryItemType getItemType() {
        return HistoryItemType.FINANCIAL;
    }

    @Override
    public List<DefendantAccountHistoryItem> fetch(AccountHistoryContext context,
                                                   DefendantAccountHistoryFilter filter) {
        Long defendantAccountId = context.getAccountId();
        List<DefendantTransactionEntity> transactions = defendantTransactionRepository.findAll(allOf(
            transactionForDefendantAccount(defendantAccountId),
            transactionDateFrom(filter.getDateFrom()),
            transactionDateTo(filter.getDateTo())
        ));

        DefendantTransactionHistoryAssociations transactionAssociations =
            getTransactionHistoryAssociations(transactions);

        return transactions.stream()
            .map(transaction -> toTransactionHistoryItem(transaction, transactionAssociations))
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

    private Optional<Long> parseAssociatedRecordId(String associatedRecordId) {
        try {
            return associatedRecordId == null ? Optional.empty() : Optional.of(Long.valueOf(associatedRecordId));
        } catch (NumberFormatException ex) {
            log.debug(":parseAssociatedRecordId: unable to parse associated record id '{}'", associatedRecordId);
            return Optional.empty();
        }
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

    @Value
    @Builder
    private static class DefendantTransactionHistoryAssociations {

        Map<Long, DefendantAccountEntity> defendantAccounts;

        Map<Long, ImpositionEntity> impositions;
    }
}
