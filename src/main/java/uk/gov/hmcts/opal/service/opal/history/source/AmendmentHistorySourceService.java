package uk.gov.hmcts.opal.service.opal.history.source;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
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

@Service
@RequiredArgsConstructor
public class AmendmentHistorySourceService {

    private final AmendmentRepository amendmentRepository;
    private final AmendmentEntityHistoryMapper amendmentEntityHistoryMapper;

    @Transactional(readOnly = true)
    public List<DefendantAccountHistoryItem> fetch(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.AMENDMENT)) {
            return List.of();
        }

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

    private LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime dayAfterStart(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    @SafeVarargs
    private final <T> Specification<T> allOf(Specification<T>... specifications) {
        return Specification.allOf(Stream.of(specifications)
            .filter(Objects::nonNull)
            .toList());
    }
}
