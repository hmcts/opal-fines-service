package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.mapper.history.EnforcementEntityHistoryMapper;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryModelAdapter;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;

@Service
@RequiredArgsConstructor
public class EnforcementHistorySource extends HistorySourceSpecificationSupport
    implements AccountHistorySource {

    private final EnforcementRepositoryService enforcementRepositoryService;
    private final EnforcementEntityHistoryMapper enforcementEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public AccountHistoryItemType getItemType() {
        return AccountHistoryItemType.ENFORCEMENT;
    }

    @Override
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        Long defendantAccountId = context.getAccountId();
        return enforcementRepositoryService.findAll(allOf(
                enforcementForDefendantAccount(defendantAccountId),
                enforcementDateFrom(filter.getDateFrom()),
                enforcementDateTo(filter.getDateTo())
            )).stream()
            .map(enforcementEntityHistoryMapper::toHistoryItem)
            .map(DefendantAccountHistoryModelAdapter::toCoreItem)
            .toList();
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
}
