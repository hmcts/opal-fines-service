package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.mapper.history.PaymentTermsEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.PaymentTermsRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryModelAdapter;

@Service
@RequiredArgsConstructor
public class PaymentTermsHistorySource extends HistorySourceSpecificationSupport
    implements AccountHistorySource {

    private final PaymentTermsRepository paymentTermsRepository;
    private final PaymentTermsEntityHistoryMapper paymentTermsEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public AccountHistoryItemType getItemType() {
        return AccountHistoryItemType.PAYMENT_TERMS;
    }

    @Override
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        Long defendantAccountId = context.getAccountId();
        return paymentTermsRepository.findAll(allOf(
                paymentTermsForDefendantAccount(defendantAccountId),
                paymentTermsDateFrom(filter.getDateFrom()),
                paymentTermsDateTo(filter.getDateTo())
            )).stream()
            .map(paymentTermsEntityHistoryMapper::toHistoryItem)
            .map(DefendantAccountHistoryModelAdapter::toCoreItem)
            .toList();
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
}
