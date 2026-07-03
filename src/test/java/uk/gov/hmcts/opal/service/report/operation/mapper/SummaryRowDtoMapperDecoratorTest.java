package uk.gov.hmcts.opal.service.report.operation.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operation.OperationReportBaseRowDto;
import uk.gov.hmcts.opal.dto.report.operation.SummaryOperationReportRowDto;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;

@ExtendWith(MockitoExtension.class)
class SummaryRowDtoMapperDecoratorTest {

    @Mock
    private SummaryRowDtoCoreMapper delegate;

    @Mock
    private CommonRowMappingHelper helper;

    @Mock
    private EnforcementRepositoryService enforcementService;

    @InjectMocks
    private SummaryRowDtoMapperDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator.setDelegate(delegate);
    }

    @Test
    void shouldCallDelegateMap() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        SummaryOperationReportRowDto dto = new SummaryOperationReportRowDto();
        ReportMetadataContext context = new ReportMetadataContext();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn(null);

        OperationReportBaseRowDto result = decorator.map(entity, context);

        assertThat(result).isSameAs(dto);
        verify(delegate).map(entity, context);
    }

    @Test
    void shouldUseHelperForPartyAndGuardianEnrichment() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        SummaryOperationReportRowDto dto = new SummaryOperationReportRowDto();
        ReportMetadataContext context = new ReportMetadataContext();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn("N");

        decorator.map(entity, context);

        verify(helper).applyParty(eq(entity), eq(dto), eq(context), any(), any());
        verify(helper).parentGuardianValue(entity);
        verify(helper).applyParentGuardian(eq(entity), any());
    }

    @Test
    void shouldApplyLatestEnforcement() {
        LocalDateTime posted = LocalDateTime.of(2024, 1, 1, 10, 0);
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(100L)
            .build();
        SummaryOperationReportRowDto dto = new SummaryOperationReportRowDto();
        ReportMetadataContext context = new ReportMetadataContext();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn(null);

        EnforcementEntity latest = mock(EnforcementEntity.class);
        when(enforcementService.getEnforcementMostRecent(100L)).thenReturn(Optional.of(latest));
        when(latest.getPostedDate()).thenReturn(posted);
        when(latest.getReason()).thenReturn("ReasonX");
        when(latest.getPostedBy()).thenReturn("user123");
        when(latest.getWarrantReference()).thenReturn("W123");
        when(latest.getHearingCourtId()).thenReturn(999L);

        decorator.map(entity, context);

        assertThat(dto.getLastEnforcementDate()).isEqualTo(posted.toLocalDate());
        assertThat(dto.getEnforcementReason()).isEqualTo("ReasonX");
        assertThat(dto.getUser()).isEqualTo("user123");
        assertThat(dto.getWarrantRef()).isEqualTo("W123");
        assertThat(dto.getEnforcingCourtCode()).isEqualTo("999");
    }

    @Test
    void shouldSetEarliestReleaseDateWhenPrisonResult() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .build();
        SummaryOperationReportRowDto dto = new SummaryOperationReportRowDto();
        ReportMetadataContext context = new ReportMetadataContext();

        when(delegate.map(entity, context)).thenReturn(dto);
        when(helper.parentGuardianValue(entity)).thenReturn(null);

        EnforcementEntity latest = mock(EnforcementEntity.class);
        when(enforcementService.getEnforcementMostRecent(1L)).thenReturn(Optional.of(latest));
        when(latest.getResultId()).thenReturn("PRIS");
        LocalDateTime hearingDate = LocalDateTime.of(2024, 2, 1, 9, 0);
        when(latest.getHearingDate()).thenReturn(hearingDate);

        decorator.map(entity, context);

        assertThat(dto.getEarliestReleaseDate()).isEqualTo(hearingDate.toLocalDate());
    }
}