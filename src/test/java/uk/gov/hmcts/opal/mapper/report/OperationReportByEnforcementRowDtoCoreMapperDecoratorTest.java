package uk.gov.hmcts.opal.mapper.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.dto.report.EnforcementReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.mapper.OperationReportByEnforcementRowDtoCoreMapper;
import uk.gov.hmcts.opal.service.report.mapper.OperationReportByEnforcementRowDtoCoreMapperDecorator;

@ExtendWith(MockitoExtension.class)
class OperationReportByEnforcementRowDtoCoreMapperDecoratorTest {

    @Mock
    private OperationReportByEnforcementRowDtoCoreMapper delegate;

    @Mock
    private DebtorDetailRepositoryService debtorService;

    @Mock
    private EnforcementRepositoryService enforcementService;

    @Mock
    private ReportMetadataContext context;

    @InjectMocks
    private OperationReportByEnforcementRowDtoCoreMapperDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator.setDelegate(delegate);
    }

    @Test
    void shouldCallDelegateMap() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        EnforcementReportRowDto result = decorator.map(entity, context);

        assertThat(result).isSameAs(dto);
        verify(delegate).map(entity, context);
    }

    @Test
    void shouldApplyPartyMapping() {
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        PartyEntity party = PartyEntity.builder()
            .partyId(1L)
            .build();
        DefendantAccountPartiesEntity partiesEntity = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(party)
            .build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(partiesEntity))
            .build();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        verify(delegate).mapParty(party, dto);
    }

    @Test
    void shouldApplyDebtorMappingWhenFound() {
        PartyEntity party = PartyEntity.builder()
            .partyId(10L)
            .build();
        DefendantAccountPartiesEntity partiesEntity = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(party)
            .build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(partiesEntity))
            .build();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        when(debtorService.findByPartyId(10L)).thenReturn(Optional.of(debtor));

        decorator.map(entity, context);

        verify(delegate).mapDebtor(debtor, dto);
    }

    @Test
    void shouldApplyLatestEnforcement() {
        LocalDateTime posted = LocalDateTime.of(2024, 1, 1, 10, 0);
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(100L)
            .build();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);
        EnforcementEntity latest = mock(EnforcementEntity.class);
        when(enforcementService.getEnforcementMostRecent(100L))
            .thenReturn(Optional.of(latest));
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
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);
        EnforcementEntity latest = mock(EnforcementEntity.class);
        when(enforcementService.getEnforcementMostRecent(1L))
            .thenReturn(Optional.of(latest));
        when(latest.getResultId()).thenReturn("PRIS");
        LocalDateTime hearingDate = LocalDateTime.of(2024, 2, 1, 9, 0);
        when(latest.getHearingDate()).thenReturn(hearingDate);

        decorator.map(entity, context);

        assertThat(dto.getEarliestReleaseDate()).isEqualTo(hearingDate.toLocalDate());
    }

    @Test
    void shouldApplyParentGuardianFlag() {
        DefendantAccountPartiesEntity partiesEntity = new DefendantAccountPartiesEntity();
        partiesEntity.setAssociationType(AssociationType.PARENT_GUARDIAN);
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(partiesEntity))
            .build();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(dto.getParentOrGuardian()).isEqualTo("Y");
    }

    @Test
    void shouldHandleNoPartiesGracefully() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(null)
            .build();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);
        decorator.map(entity, context);
        assertThat(dto.getParentOrGuardian()).isNull();
    }

    @Test
    void shouldAddDefendantAccountParticipant() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(100L)
            .build();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        ReportMetadataContext context = new ReportMetadataContext();
        when(delegate.map(entity, context)).thenReturn(dto);
        decorator.map(entity, context);

        assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .contains("100");
    }

    @Test
    void shouldAddDebtorParticipant_whenDebtorExists() {
        PartyEntity party = PartyEntity.builder()
            .partyId(10L)
            .build();
        DefendantAccountPartiesEntity partiesEntity = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(party)
            .build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(partiesEntity))
            .build();
        DebtorDetailEntity debtor = DebtorDetailEntity.builder()
            .partyId(10L)
            .build();
        when(debtorService.findByPartyId(10L)).thenReturn(Optional.of(debtor));
        ReportMetadataContext context = new ReportMetadataContext();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .contains("10");
    }

    @Test
    void shouldAddParentGuardianParticipants() {
        DefendantAccountPartiesEntity pg1 = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .defendantAccountPartyId(200L)
            .build();
        DefendantAccountPartiesEntity pg2 = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .defendantAccountPartyId(300L)
            .build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(pg1, pg2))
            .build();
        ReportMetadataContext context = new ReportMetadataContext();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(context.getParticipants())
            .filteredOn("type", PdplIdentifierType.PARENT_GUARDIAN)
            .extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyInAnyOrder("200", "300");
    }

    @Test
    void shouldNotAddDebtorParticipant_whenNoDebtorFound() {
        PartyEntity party = PartyEntity.builder()
            .partyId(10L)
            .build();
        DefendantAccountPartiesEntity partiesEntity = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(party)
            .build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(partiesEntity))
            .build();
        when(debtorService.findByPartyId(10L)).thenReturn(Optional.empty());
        ReportMetadataContext context = new ReportMetadataContext();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .doesNotContain("10");
    }
}