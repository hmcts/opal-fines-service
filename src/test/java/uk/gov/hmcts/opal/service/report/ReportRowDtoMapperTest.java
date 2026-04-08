package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@ExtendWith(MockitoExtension.class)
class ReportRowDtoMapperTest {

    private final ReportRowDtoMapper mapper = Mappers.getMapper(ReportRowDtoMapper.class);

    @Mock
    private ReportEnrichmentService enrichment;

    @Test
    void pickPrimaryParty_handles_null_and_empty() {
        assertThat(mapper.pickPrimaryParty(null)).isNull();

        DefendantAccountEntity acc = new DefendantAccountEntity();
        acc.setParties(Collections.emptyList());
        assertThat(mapper.pickPrimaryParty(acc)).isNull();
    }

    @Test
    void pickPrimaryParty_prefers_defendant_assoc_then_debtor_then_first_non_null() {

        PartyEntity p1 = new PartyEntity();
        p1.setPartyId(1L);
        p1.setSurname("One");

        DefendantAccountPartiesEntity link1 = new DefendantAccountPartiesEntity();
        link1.setParty(p1);

        PartyEntity p2 = new PartyEntity();
        p2.setPartyId(2L);
        p2.setSurname("Two");

        DefendantAccountPartiesEntity link2 = new DefendantAccountPartiesEntity();
        link2.setAssociationType(AssociationType.DEFENDANT);
        link2.setParty(p2);

        DefendantAccountEntity acc = new DefendantAccountEntity();

        acc.setParties(Arrays.asList(link1, link2));
        assertThat(mapper.pickPrimaryParty(acc)).isSameAs(p2);

        // if no DEFENDANT, prefer debtor == true
        link2.setAssociationType(null);
        link1.setDebtor(Boolean.TRUE);
        assertThat(mapper.pickPrimaryParty(acc)).isSameAs(p1);

        // if neither, pick first non-null party
        link1.setDebtor(Boolean.FALSE);
        acc.setParties(Arrays.asList(link1, link2));
        assertThat(mapper.pickPrimaryParty(acc)).isSameAs(p1);
    }

    @Test
    void enrich_maps_party_fields_and_truncates_name_and_handles_collectionOrder_and_parentGuardian() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        entity.setDefendantAccountId(99L);

        PartyEntity party = new PartyEntity();
        party.setPartyId(22L);
        party.setOrganisation(false);
        party.setSurname("VeryLongSurnameThatExceedsThirtyFourCharacters");
        party.setForenames("Forename1 Forename2");
        party.setBirthDate(LocalDate.of(1985, 6, 15));
        party.setNiNumber("NI-123");
        party.setAddressLine1("A1");
        party.setAddressLine2("A2");
        party.setAddressLine3("A3");
        party.setPostcode("PC1 1AA");
        party.setMobileTelephoneNumber("07123456789");
        party.setHomeTelephoneNumber("0123456789");
        party.setWorkTelephoneNumber("011234567");
        party.setPrimaryEmailAddress("p1@example.com");
        party.setSecondaryEmailAddress("p2@example.com");

        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setAssociationType(AssociationType.DEFENDANT);
        link.setParty(party);

        // add a parent/guardian link to test PG detection
        DefendantAccountPartiesEntity pgLink = new DefendantAccountPartiesEntity();
        pgLink.setAssociationType(AssociationType.PARENT_GUARDIAN);

        entity.setParties(Arrays.asList(link, pgLink));

        entity.setCollectionOrder(Boolean.TRUE);
        entity.setProsecutorCaseReference("PCR-XYZ");
        entity.setJailDays(5);

        // set enforcing court
        CourtEntity.Lite court = new CourtEntity.Lite();
        court.setName("Enforcing Court Name");
        entity.setEnforcingCourt(court);

        // debtor detail to be returned by enrichment
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setVehicleRegistration("REG-1");
        debtor.setVehicleMake("MakeX");
        debtor.setEmployeeReference("EMP-REF");
        debtor.setEmployerName("ACME Ltd");
        debtor.setEmployerAddressLine1("E1");
        debtor.setEmployerAddressLine2("E2");
        debtor.setEmployerAddressLine3("E3");
        debtor.setEmployerAddressLine4("E4");
        debtor.setEmployerAddressLine5("E5");
        debtor.setEmployerPostcode("EPC");
        debtor.setEmployerTelephone("0900");
        debtor.setEmployerEmail("employer@example.com");

        when(enrichment.getDebtorForParty(22L)).thenReturn(Optional.of(debtor));

        // latest enforcement returned by enrichment
        EnforcementEntity.Lite elite = org.mockito.Mockito.mock(EnforcementEntity.Lite.class);
        LocalDateTime posted = LocalDateTime.of(2024, 2, 2, 10, 30);
        LocalDateTime hearingDate = LocalDateTime.of(2024, 2, 1, 9, 0);
        when(elite.getPostedDate()).thenReturn(posted);
        when(elite.getReason()).thenReturn("ReasonX");
        when(elite.getPostedBy()).thenReturn("user123");
        when(elite.getWarrantReference()).thenReturn("W-100");
        when(elite.getHearingCourtId()).thenReturn(555L);
        when(elite.getResultId()).thenReturn("PRIS");
        when(elite.getHearingDate()).thenReturn(hearingDate);

        when(enrichment.getLatestEnforcementForAccount(99L)).thenReturn(Optional.of(elite));

        EnforcementReportRowDto dto = mapper.map(entity, enrichment);

        // company = "N"
        assertThat(dto.getCompany()).isEqualTo("N");

        // defname should be truncated to 34 chars
        assertThat(dto.getDefname()).hasSizeLessThanOrEqualTo(34);
        assertThat(dto.getDefname()).startsWith("VeryLongSurnameThatExceeds");

        assertThat(dto.getDob()).isEqualTo(LocalDate.of(1985, 6, 15));
        assertThat(dto.getNino()).isEqualTo("NI-123");
        assertThat(dto.getAddress1()).isEqualTo("A1");
        assertThat(dto.getAddress2()).isEqualTo("A2");
        assertThat(dto.getAddress3()).isEqualTo("A3");
        assertThat(dto.getPostcode()).isEqualTo("PC1 1AA");
        assertThat(dto.getMobtel()).isEqualTo("07123456789");
        assertThat(dto.getHometel()).isEqualTo("0123456789");
        assertThat(dto.getBustel()).isEqualTo("011234567");
        assertThat(dto.getEmail1()).isEqualTo("p1@example.com");
        assertThat(dto.getEmail2()).isEqualTo("p2@example.com");

        // imposing court
        assertThat(dto.getImposingCourt()).isEqualTo("Enforcing Court Name");

        // debtor details
        assertThat(dto.getVehicleReg()).isEqualTo("REG-1");
        assertThat(dto.getVehicleMake()).isEqualTo("MakeX");
        assertThat(dto.getEmpRef()).isEqualTo("EMP-REF");
        assertThat(dto.getEmpName()).isEqualTo("ACME Ltd");
        assertThat(dto.getEmpAdd1()).isEqualTo("E1");
        assertThat(dto.getEmpAdd2()).isEqualTo("E2");
        assertThat(dto.getEmpAdd3()).isEqualTo("E3");
        assertThat(dto.getEmpAdd4()).isEqualTo("E4");
        assertThat(dto.getEmpAdd5()).isEqualTo("E5");
        assertThat(dto.getEmpPCode()).isEqualTo("EPC");
        assertThat(dto.getEmpTel()).isEqualTo("0900");
        assertThat(dto.getEmpEmail()).isEqualTo("employer@example.com");

        // latest enforcement mapping
        assertThat(dto.getLeDate()).isEqualTo(LocalDate.from(posted));
        assertThat(dto.getEnfReason()).isEqualTo("ReasonX");
        assertThat(dto.getUser()).isEqualTo("user123");
        assertThat(dto.getWarrNo()).isEqualTo("W-100");
        assertThat(dto.getEnfCrt()).isEqualTo(String.valueOf(555));
        assertThat(dto.getEdrDate()).isEqualTo(LocalDate.from(hearingDate));

        // collection order mapping
        assertThat(dto.getCo()).isEqualTo("Y");

        // parent guardian detection
        assertThat(dto.getPg()).isEqualTo("Y");

        // pcr and did fallback (mapped from entity if not provided by mapping)
        assertThat(dto.getPcr()).isEqualTo("PCR-XYZ");
        assertThat(dto.getDid()).isEqualTo(5);
    }

    @Test
    void enrich_handles_missing_optional_parts_and_nulls() {
        DefendantAccountEntity entity = new DefendantAccountEntity();
        // no parties, no enforcing court, no enrichment returns -> should not NPE
        EnforcementReportRowDto dto = mapper.map(entity, enrichment);

        // defaults
        assertThat(dto.getCompany()).isNull();
        assertThat(dto.getDefname()).isNull();
        assertThat(dto.getPg()).isNull();

        // collection order null -> co null
        entity.setCollectionOrder(null);
        EnforcementReportRowDto dto2 = mapper.map(entity, enrichment);
        assertThat(dto2.getCo()).isNull();
    }
}