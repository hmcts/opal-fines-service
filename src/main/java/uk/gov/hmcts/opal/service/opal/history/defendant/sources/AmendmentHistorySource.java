package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.mapper.history.AmendmentEntityHistoryMapper;
import uk.gov.hmcts.opal.repository.jpa.AmendmentSpecs;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItem;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryItemType;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistorySource;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;
import uk.gov.hmcts.opal.service.opal.history.defendant.DefendantAccountHistoryModelAdapter;
import uk.gov.hmcts.opal.service.persistence.AmendmentRepositoryService;
import uk.gov.hmcts.opal.service.persistence.AuditAmendmentFieldRepositoryService;

@Service
@RequiredArgsConstructor
public class AmendmentHistorySource extends HistorySourceSpecificationSupport
    implements AccountHistorySource {

    private final AmendmentRepositoryService amendmentRepositoryService;
    private final AuditAmendmentFieldRepositoryService auditAmendmentFieldRepositoryService;
    private final AmendmentEntityHistoryMapper amendmentEntityHistoryMapper;

    @Transactional(readOnly = true)
    @Override
    public boolean supports(AccountHistoryContext context) {
        return AccountHistoryType.DEFENDANT == context.getAccountType();
    }

    @Override
    public AccountHistoryItemType getItemType() {
        return AccountHistoryItemType.AMENDMENT;
    }

    @Override
    public List<AccountHistoryItem> fetch(AccountHistoryContext context, AccountHistoryFilter filter) {
        Long defendantAccountId = context.getAccountId();
        List<AmendmentEntity> amendments = amendmentRepositoryService.findAll(allOf(
            amendmentForDefendantAccount(defendantAccountId),
            amendmentDateFrom(filter.getDateFrom()),
            amendmentDateTo(filter.getDateTo())
        ));
        Map<Short, String> attributeNamesByFieldCode = auditAmendmentFieldRepositoryService.findAllById(
            amendments.stream()
                .map(AmendmentEntity::getFieldCode)
                .distinct()
                .toList()
        ).stream().collect(Collectors.toMap(
            amendmentField -> amendmentField.getFieldCode(),
            amendmentField -> amendmentField.getDataItem()
        ));

        return amendments.stream()
            .map(amendment -> toHistoryItem(amendment, attributeNamesByFieldCode))
            .map(DefendantAccountHistoryModelAdapter::toCoreItem)
            .toList();
    }

    private DefendantAccountHistoryItem toHistoryItem(
        AmendmentEntity amendment,
        Map<Short, String> attributeNamesByFieldCode
    ) {
        DefendantAccountHistoryItem historyItem = amendmentEntityHistoryMapper.toHistoryItem(amendment);
        if (historyItem.getDetails() instanceof AmendmentDetails details) {
            details.setAttributeName(attributeNamesByFieldCode.getOrDefault(
                amendment.getFieldCode(),
                amendment.getFieldCode() == null ? null : amendment.getFieldCode().toString()
            ));
        }
        return historyItem;
    }

    private Specification<AmendmentEntity> amendmentForDefendantAccount(Long defendantAccountId) {
        AssociatedRecordType associatedRecordType = AssociatedRecordType.DEFENDANT_ACCOUNTS;
        return allOf(
            AmendmentSpecs.equalsAssociatedRecordType(associatedRecordType),
            AmendmentSpecs.equalsAssociatedRecordId(defendantAccountId.toString())
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
