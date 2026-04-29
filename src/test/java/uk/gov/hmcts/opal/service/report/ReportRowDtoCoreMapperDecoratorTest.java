package uk.gov.hmcts.opal.service.report;

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
import uk.gov.hmcts.opal.dto.PdplIdentifierType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ReportRowDtoCoreMapperDecoratorTest {

    @Mock
    private ReportRowDtoCoreMapper delegate;

    @Mock
    private DebtorDetailRepositoryService debtorService;

    @Mock
    private EnforcementRepositoryService enforcementService;

    @Mock
    private ReportMetadataContext context;

    @InjectMocks
    private ReportRowDtoCoreMapperDecorator decorator;

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
        DefendantAccountEntity entity = new DefendantAccountEntity();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        PartyEntity party = new PartyEntity();
        party.setPartyId(1L);
        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setAssociationType(AssociationType.DEFENDANT);
        link.setParty(party);
        entity.setParties(List.of(link));
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        verify(delegate).mapParty(party, dto);
    }

    @Test
    void shouldApplyDebtorMappingWhenFound() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        PartyEntity party = new PartyEntity();
        party.setPartyId(10L);
        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setAssociationType(AssociationType.DEFENDANT);
        link.setParty(party);
        entity.setParties(List.of(link));
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        when(delegate.map(entity, context)).thenReturn(dto);
        when(debtorService.findByPartyId(10L)).thenReturn(Optional.of(debtor));

        decorator.map(entity, context);

        verify(delegate).mapDebtor(debtor, dto);
    }

    @Test
    void shouldApplyLatestEnforcement() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(100L);
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        EnforcementEntity latest = mock(EnforcementEntity.class);
        LocalDateTime posted = LocalDateTime.of(2024, 1, 1, 10, 0);
        when(delegate.map(entity, context)).thenReturn(dto);
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
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(1L);
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        EnforcementEntity latest = mock(EnforcementEntity.class);
        LocalDateTime hearingDate = LocalDateTime.of(2024, 2, 1, 9, 0);
        when(delegate.map(entity, context)).thenReturn(dto);
        when(enforcementService.getEnforcementMostRecent(1L))
            .thenReturn(Optional.of(latest));
        when(latest.getResultId()).thenReturn("PRIS");
        when(latest.getHearingDate()).thenReturn(hearingDate);

        decorator.map(entity, context);

        assertThat(dto.getEarliestReleaseDate()).isEqualTo(hearingDate.toLocalDate());
    }

    @Test
    void shouldApplyParentGuardianFlag() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        DefendantAccountPartiesEntity partiesEntity = new DefendantAccountPartiesEntity();
        partiesEntity.setAssociationType(AssociationType.PARENT_GUARDIAN);
        entity.setParties(List.of(partiesEntity));
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(dto.getParentOrGuardian()).isEqualTo("Y");
    }

    @Test
    void shouldApplyFallbacks() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setProsecutorCaseReference("PCR123");
        entity.setJailDays(5);
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(dto.getParentOrGuardian()).isEqualTo("PCR123");
        assertThat(dto.getJailDays()).isEqualTo(5);
    }

    @Test
    void shouldHandleNoPartiesGracefully() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setParties(null);
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(dto.getParentOrGuardian()).isNull();
    }

    @Test
    void shouldAddDefendantAccountParticipant() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(100L);
        ReportMetadataContext context = new ReportMetadataContext();
        EnforcementReportRowDto dto = new EnforcementReportRowDto();
        when(delegate.map(entity, context)).thenReturn(dto);

        decorator.map(entity, context);

        assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .contains("100");
    }

    @Test
    void shouldAddDebtorParticipant_whenDebtorExists() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(1L);
        PartyEntity party = new PartyEntity();
        party.setPartyId(10L);
        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setAssociationType(AssociationType.DEFENDANT);
        link.setParty(party);
        entity.setParties(List.of(link));
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setPartyId(10L);
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
        DefendantAccountEntity entity = new DefendantAccountEntity();
        DefendantAccountPartiesEntity pg1 = new DefendantAccountPartiesEntity();
        pg1.setAssociationType(AssociationType.PARENT_GUARDIAN);
        pg1.setDefendantAccountPartyId(200L);
        DefendantAccountPartiesEntity pg2 = new DefendantAccountPartiesEntity();
        pg2.setAssociationType(AssociationType.PARENT_GUARDIAN);
        pg2.setDefendantAccountPartyId(300L);
        entity.setParties(List.of(pg1, pg2));
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
        DefendantAccountEntity entity = new DefendantAccountEntity();
        PartyEntity party = new PartyEntity();
        party.setPartyId(10L);
        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setAssociationType(AssociationType.DEFENDANT);
        link.setParty(party);
        entity.setParties(List.of(link));
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