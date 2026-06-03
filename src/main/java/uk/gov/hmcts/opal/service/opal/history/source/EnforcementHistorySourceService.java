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
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.mapper.history.EnforcementEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

@Service
@RequiredArgsConstructor
public class EnforcementHistorySourceService {

    private final EnforcementRepository enforcementRepository;
    private final EnforcementEntityHistoryMapper enforcementEntityHistoryMapper;

    @Transactional(readOnly = true)
    public List<DefendantAccountHistoryItem> fetch(Long defendantAccountId, DefendantAccountHistoryFilter filter) {
        if (!filter.includes(HistoryItemType.ENFORCEMENT)) {
            return List.of();
        }

        return enforcementRepository.findAll(allOf(
                enforcementForDefendantAccount(defendantAccountId),
                enforcementDateFrom(filter.getDateFrom()),
                enforcementDateTo(filter.getDateTo())
            )).stream()
            .map(enforcementEntityHistoryMapper::toHistoryItem)
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
