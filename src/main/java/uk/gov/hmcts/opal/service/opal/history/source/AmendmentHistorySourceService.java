package uk.gov.hmcts.opal.service.opal.history.source;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.mapper.history.AmendmentEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;

@Service
@RequiredArgsConstructor
public class AmendmentHistorySourceService extends HistorySourceSpecificationSupport
    implements AccountHistorySource {

    private final AmendmentRepository amendmentRepository;
    private final AmendmentEntityHistoryMapper amendmentEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public HistoryItemType getItemType() {
        return HistoryItemType.AMENDMENT;
    }

    @Override
    public List<DefendantAccountHistoryItem> fetch(AccountHistoryContext context,
                                                   DefendantAccountHistoryFilter filter) {
        Long defendantAccountId = context.getAccountId();
        return amendmentRepository.findAll(allOf(
                amendmentForDefendantAccount(defendantAccountId),
                amendmentDateFrom(filter.getDateFrom()),
                amendmentDateTo(filter.getDateTo())
            )).stream()
            .map(amendmentEntityHistoryMapper::toHistoryItem)
            .toList();
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
}
