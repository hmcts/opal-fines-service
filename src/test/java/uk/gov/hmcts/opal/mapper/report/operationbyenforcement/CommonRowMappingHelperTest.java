package uk.gov.hmcts.opal.mapper.report.operationbyenforcement;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.report.operationbyenforcement.OperationByEnforcementSummaryReportRowDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.logging.integration.dto.ParticipantIdentifier;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.report.ReportMetadataContext;
import uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper.CommonRowMappingHelper;

@ExtendWith(MockitoExtension.class)
class CommonRowMappingHelperTest {

    public static final String MAPPED = "mapped";
    public static final String DEBTOR_MAPPED = "debtor-mapped";
    @Mock
    private DebtorDetailRepositoryService debtorService;

    private CommonRowMappingHelper helper;

    @BeforeEach
    void setUp() {
        helper = new CommonRowMappingHelper(debtorService);
    }

    @Test
    void pickPrimaryParty_returnsDefendantPartyWhenPresent() {
        PartyEntity defendantParty = PartyEntity.builder().partyId(1L).build();
        PartyEntity otherParty = PartyEntity.builder().partyId(2L).build();

        DefendantAccountPartiesEntity link1 = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .party(otherParty)
            .build();
        DefendantAccountPartiesEntity link2 = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(defendantParty)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(link1, link2))
            .build();

        assertThat(helper.pickPrimaryParty(entity)).isSameAs(defendantParty);
    }

    @Test
    void pickPrimaryParty_returnsDebtorFallbackWhenNoDefendantParty() {
        PartyEntity debtorParty = PartyEntity.builder().partyId(10L).build();

        DefendantAccountPartiesEntity link = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .debtor(true)
            .party(debtorParty)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(link))
            .build();

        assertThat(helper.pickPrimaryParty(entity)).isSameAs(debtorParty);
    }

    @Test
    void pickPrimaryParty_returnsAnyPartyFallbackWhenNoDefendantOrDebtorParty() {
        PartyEntity party = PartyEntity.builder().partyId(10L).build();

        DefendantAccountPartiesEntity link = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .party(party)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(link))
            .build();

        assertThat(helper.pickPrimaryParty(entity)).isSameAs(party);
    }

    @Test
    void applyParty_mapsPartyAndDebtorAndAddsParticipant() {
        PartyEntity party = PartyEntity.builder().partyId(10L).build();
        DebtorDetailEntity debtor = DebtorDetailEntity.builder().partyId(10L).build();

        DefendantAccountPartiesEntity link = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.DEFENDANT)
            .party(party)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(link))
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        OperationByEnforcementSummaryReportRowDto dto = new OperationByEnforcementSummaryReportRowDto();

        when(debtorService.findByPartyId(10L)).thenReturn(Optional.of(debtor));

        helper.applyParty(
            entity,
            dto,
            context,
            (p, d) -> d.setDefendantName(MAPPED),
            (d, row) -> row.setUser(DEBTOR_MAPPED)
        );

        assertThat(dto.getDefendantName()).isEqualTo(MAPPED);
        assertThat(dto.getUser()).isEqualTo(DEBTOR_MAPPED);
        Assertions.assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .contains("10");
    }

    @Test
    void applyParty_doesNothingWhenNoPrimaryPartyExists() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(null)
            .build();

        ReportMetadataContext context = new ReportMetadataContext();
        OperationByEnforcementSummaryReportRowDto dto = new OperationByEnforcementSummaryReportRowDto();

        helper.applyParty(
            entity,
            dto,
            context,
            (p, d) -> d.setDefendantName(MAPPED),
            (d, row) -> row.setUser(DEBTOR_MAPPED)
        );

        assertThat(dto.getDefendantName()).isNull();
        assertThat(dto.getUser()).isNull();
        Assertions.assertThat(context.getParticipants()).isEmpty();
        verify(debtorService, never()).findByPartyId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void applyParentGuardian_addsParticipantsAndReturnsTrueWhenFound() {
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

        boolean result = helper.applyParentGuardian(entity, context::addParticipant);

        assertThat(result).isTrue();
        Assertions.assertThat(context.getParticipants())
            .extracting(ParticipantIdentifier::getIdentifier)
            .containsExactlyInAnyOrder("200", "300");
    }

    @Test
    void applyParentGuardian_returnsFalseWhenNoParentGuardianExists() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of())
            .build();

        ReportMetadataContext context = new ReportMetadataContext();

        boolean result = helper.applyParentGuardian(entity, context::addParticipant);

        assertThat(result).isFalse();
        Assertions.assertThat(context.getParticipants()).isEmpty();
    }

    @Test
    void parentGuardianValue_returnsYWhenParentGuardianExists() {
        DefendantAccountPartiesEntity pg = DefendantAccountPartiesEntity.builder()
            .associationType(AssociationType.PARENT_GUARDIAN)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of(pg))
            .build();

        assertThat(helper.parentGuardianValue(entity)).isEqualTo("Y");
    }

    @Test
    void parentGuardianValue_returnsNWhenNoParentGuardianExists() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .parties(List.of())
            .build();

        assertThat(helper.parentGuardianValue(entity)).isEqualTo("N");
    }
}
