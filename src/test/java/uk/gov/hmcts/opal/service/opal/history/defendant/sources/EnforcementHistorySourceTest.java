package uk.gov.hmcts.opal.service.opal.history.defendant.sources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.mapper.history.EnforcementEntityHistoryMapper;
import uk.gov.hmcts.opal.mapper.history.EnforcementEntityHistoryMapperImpl;
import uk.gov.hmcts.opal.dto.history.AccountHistoryContext;
import uk.gov.hmcts.opal.dto.history.AccountHistoryEnforcementDetails;
import uk.gov.hmcts.opal.dto.history.AccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.AccountHistoryItemType;
import uk.gov.hmcts.opal.dto.history.AccountHistoryType;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;

@ExtendWith(MockitoExtension.class)
class EnforcementHistorySourceTest {

    @Mock
    private EnforcementRepositoryService enforcementRepositoryService;

    private final EnforcementEntityHistoryMapper enforcementEntityHistoryMapper =
        new EnforcementEntityHistoryMapperImpl();

    @Test
    void fetch_returnsEnforcementRowsForDefendantAccountEvenWhenResultIdIsNull() {
        EnforcementEntity enforcement = EnforcementEntity.builder()
            .enforcementId(26221001L)
            .defendantAccountId(262210L)
            .postedDate(LocalDateTime.of(2026, 1, 8, 9, 0))
            .postedBy("hist-user-null-result")
            .postedByUsername("History User Null Result")
            .reason("Null result enforcement")
            .jailDays(11)
            .warrantReference("WR262210")
            .caseReference("CASE-HIST-NULL-RESULT")
            .hearingDate(LocalDateTime.of(2026, 2, 8, 10, 0))
            .build();

        when(enforcementRepositoryService.findHistoryByDefendantAccountId(262210L))
            .thenReturn(List.of(enforcement));

        EnforcementHistorySource source = new EnforcementHistorySource(
            enforcementRepositoryService,
            enforcementEntityHistoryMapper
        );

        var historyItems = source.fetch(
            new AccountHistoryContext(AccountHistoryType.DEFENDANT, 262210L),
            AccountHistoryFilter.builder().build()
        );

        assertThat(historyItems).hasSize(1);
        assertThat(historyItems.get(0).getType()).isEqualTo(AccountHistoryItemType.ENFORCEMENT);
        assertThat(historyItems.get(0).getSourceId()).isEqualTo(26221001L);
        assertThat(historyItems.get(0).getEventDateTime()).isEqualTo(LocalDateTime.of(2026, 1, 8, 9, 0));

        assertThat(historyItems.get(0).getDetails()).isInstanceOf(AccountHistoryEnforcementDetails.class);
        AccountHistoryEnforcementDetails details =
            (AccountHistoryEnforcementDetails) historyItems.get(0).getDetails();
        assertThat(details.getEnforcementAction()).isNull();
        assertThat(details.getReason()).isEqualTo("Null result enforcement");
        assertThat(details.getDaysInDefault()).isEqualTo(11);
        assertThat(details.getWarrantNumber()).isEqualTo("WR262210");
        assertThat(details.getCaseNumber()).isEqualTo("CASE-HIST-NULL-RESULT");

        verify(enforcementRepositoryService).findHistoryByDefendantAccountId(262210L);
    }
}
