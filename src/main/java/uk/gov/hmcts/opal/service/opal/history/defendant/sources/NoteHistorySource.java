package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.NoteType;
import uk.gov.hmcts.opal.mapper.history.NoteEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryModelAdapter;

@Service
@RequiredArgsConstructor
public class NoteHistorySource extends HistorySourceSpecificationSupport implements AccountHistorySource {

    private final NoteRepository noteRepository;
    private final NoteEntityHistoryMapper noteEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public AccountHistoryItemType getItemType() {
        return AccountHistoryItemType.NOTE;
    }

    @Override
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        return noteRepository.findAll(allOf(
                noteForDefendantAccount(context.getAccountId()),
                noteTypeAa(),
                noteDateFrom(filter.getDateFrom()),
                noteDateTo(filter.getDateTo())
            )).stream()
            .map(noteEntityHistoryMapper::toHistoryItem)
            .map(DefendantAccountHistoryModelAdapter::toCoreItem)
            .toList();
    }

    private Specification<NoteEntity> noteForDefendantAccount(Long defendantAccountId) {
        return (root, query, builder) -> builder.and(
            builder.equal(
                root.get("associatedRecordType").as(String.class),
                AssociatedRecordType.DEFENDANT_ACCOUNTS.getLabel()
            ),
            builder.equal(root.get("associatedRecordId"), defendantAccountId.toString())
        );
    }

    private Specification<NoteEntity> noteTypeAa() {
        return (root, query, builder) -> builder.equal(root.get("noteType").as(String.class), NoteType.AA.name());
    }

    private Specification<NoteEntity> noteDateFrom(LocalDate dateFrom) {
        return dateFrom == null ? null
            : (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postedDate"), atStartOfDay(dateFrom));
    }

    private Specification<NoteEntity> noteDateTo(LocalDate dateTo) {
        return dateTo == null ? null
            : (root, query, builder) -> builder.lessThan(root.get("postedDate"), dayAfterStart(dateTo));
    }
}
