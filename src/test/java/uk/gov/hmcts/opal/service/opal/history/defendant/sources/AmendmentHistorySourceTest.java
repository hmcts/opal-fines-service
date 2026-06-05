package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;
import uk.gov.hmcts.opal.entity.auditamendmentfield.AuditAmendmentFieldEntity;
import uk.gov.hmcts.opal.mapper.history.AmendmentEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.AmendmentEntityHistoryMapperImpl;
import uk.gov.hmcts.opal.repository.AmendmentRepository;
import uk.gov.hmcts.opal.repository.AuditAmendmentFieldRepository;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryContext;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryFilter;
import uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryType;

@ExtendWith(MockitoExtension.class)
class AmendmentHistorySourceTest {

    @Mock
    private AmendmentRepository amendmentRepository;

    @Mock
    private AuditAmendmentFieldRepository auditAmendmentFieldRepository;

    private final AmendmentEntityHistoryMapper amendmentEntityHistoryMapper = new AmendmentEntityHistoryMapperImpl();

    @Test
    void fetch_populatesAttributeNameFromAuditAmendmentFieldLookup() {
        AmendmentEntity amendment = AmendmentEntity.builder()
            .amendmentId(123L)
            .businessUnitId((short) 78)
            .associatedRecordType("DEFENDANT_ACCOUNTS")
            .associatedRecordId("262200")
            .amendedDate(LocalDateTime.of(2026, 1, 1, 10, 15))
            .amendedBy("opal-user")
            .amendedByName("Opal User")
            .fieldCode((short) 1)
            .oldValue("old")
            .newValue("new")
            .build();
        when(amendmentRepository.findAll(org.mockito.ArgumentMatchers.<Specification<AmendmentEntity>>any()))
            .thenReturn(List.of(amendment));
        when(auditAmendmentFieldRepository.findAllById(List.of((short) 1))).thenReturn(List.of(
            AuditAmendmentFieldEntity.builder()
                .fieldCode((short) 1)
                .dataItem("Major Creditor Code")
                .build()
        ));

        AmendmentHistorySource source = new AmendmentHistorySource(
            amendmentRepository,
            auditAmendmentFieldRepository,
            amendmentEntityHistoryMapper
        );

        var historyItems = source.fetch(
            new AccountHistoryContext(AccountHistoryType.DEFENDANT, 262200L),
            AccountHistoryFilter.builder().build()
        );

        assertThat(historyItems).hasSize(1);
        assertThat(historyItems.get(0).getDetails()).isInstanceOf(
            uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryAmendmentDetails.class
        );
        var details = (uk.gov.hmcts.opal.service.opal.history.core.AccountHistoryAmendmentDetails)
            historyItems.get(0).getDetails();
        assertThat(details.getAttributeName()).isEqualTo("Major Creditor Code");
        assertThat(details.getOldValue()).isEqualTo("old");
        assertThat(details.getNewValue()).isEqualTo("new");
    }
}
