package uk.gov.hmcts.opal.service.opal.history.majorcreditor.sources;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;
import uk.gov.hmcts.opal.dto.history.AccountHistoryType;
import uk.gov.hmcts.opal.entity.CreditorTransactionEntity;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.majorcreditor.MajorCreditorHistoryModelAdapter;
import uk.gov.hmcts.opal.service.persistence.CreditorTransactionRepositoryService;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.MajorCreditorTransactionHistorySource")
public class MajorCreditorTransactionHistorySource implements AccountHistorySource {

    private final CreditorTransactionRepositoryService creditorTransactionRepositoryService;

    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.MAJOR_CREDITOR == context.getAccountType();
    }

    @Override
    public AccountHistoryItemType getItemType() {
        return AccountHistoryItemType.FINANCIAL;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        return creditorTransactionRepositoryService.findAll(allOf(
                transactionForCreditorAccount(context.getAccountId()),
                transactionDateFrom(filter.getDateFrom()),
                transactionDateTo(filter.getDateTo())
            )).stream()
            .map(MajorCreditorHistoryModelAdapter::toCoreItem)
            .toList();
    }

    @SafeVarargs
    private final Specification<CreditorTransactionEntity> allOf(
        Specification<CreditorTransactionEntity>... specifications
    ) {
        return Specification.allOf(Stream.of(specifications)
            .filter(Objects::nonNull)
            .toList());
    }

    private Specification<CreditorTransactionEntity> transactionForCreditorAccount(Long creditorAccountId) {
        return (root, query, builder) -> builder.equal(root.get("creditorAccountId"), creditorAccountId);
    }

    private Specification<CreditorTransactionEntity> transactionDateFrom(LocalDate dateFrom) {
        if (dateFrom == null) {
            return null;
        }
        LocalDateTime postedFromInclusive = dateFrom.atStartOfDay();
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postedDate"), postedFromInclusive);
    }

    private Specification<CreditorTransactionEntity> transactionDateTo(LocalDate dateTo) {
        if (dateTo == null) {
            return null;
        }
        LocalDateTime postedToExclusive = dateTo.plusDays(1).atStartOfDay();
        return (root, query, builder) -> builder.lessThan(root.get("postedDate"), postedToExclusive);
    }
}
