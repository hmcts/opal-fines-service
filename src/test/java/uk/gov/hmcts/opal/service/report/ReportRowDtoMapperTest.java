package uk.gov.hmcts.opal.service.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

public class ReportRowDtoMapperTest {

    private ReportRowDtoMapper mapper;
    private ReportEnrichmentService enrichment;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ReportRowDtoMapper.class);
        enrichment = mock(ReportEnrichmentService.class);
    }

    @Test
    void map_shouldPopulateFields_andDetectParentGuardian_andPrisEdrDate() {

        PartyEntity party = new PartyEntity();
        party.setPartyId(1L);
        party.setOrganisation(false);
        party.setSurname("Jones");
        party.setForenames("Alice");
        party.setBirthDate(LocalDate.of(1990, 1, 1));
        party.setNiNumber("AB123456C");
        party.setAddressLine1("1 High St");
        party.setPostcode("ZZ1 1ZZ");
        party.setMobileTelephoneNumber("07123456789");
        party.setHomeTelephoneNumber("0123456789");
        party.setWorkTelephoneNumber("0111222333");
        party.setPrimaryEmailAddress("a@example.com");

        DefendantAccountPartiesEntity defendantLink = new DefendantAccountPartiesEntity();
        defendantLink.setParty(party);
        defendantLink.setAssociationType("Defendant");
        defendantLink.setDebtor(false);

        DefendantAccountPartiesEntity parentLink = new DefendantAccountPartiesEntity();
        parentLink.setParty(party);
        parentLink.setAssociationType("Parent/Guardian");
        parentLink.setDebtor(false);

        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(42L);
        account.setAccountNumber("00000001A");
        account.setAmountImposed(BigDecimal.valueOf(100));
        account.setAmountPaid(BigDecimal.valueOf(25));
        account.setAccountBalance(BigDecimal.valueOf(75));
        account.setCollectionOrder(true);
        account.setJailDays(5);
        account.setParties(List.of(defendantLink, parentLink));
        account.setLastEnforcement("PRIS");

        EnforcementEntity.Lite enforcement = new EnforcementEntity.Lite();
        enforcement.setDefendantAccountId(42L);
        enforcement.setPostedDate(LocalDateTime.now().minusDays(2));
        enforcement.setResultId("PRIS");
        enforcement.setHearingDate(LocalDateTime.now().minusDays(1));
        enforcement.setWarrantReference("W1");

        when(enrichment.getDebtorForParty(1L)).thenReturn(Optional.empty());
        when(enrichment.getLatestEnforcementForAccount(42L)).thenReturn(Optional.of(enforcement));

        EnforcementReportRowDto dto = mapper.map(account, enrichment);

        assertNotNull(dto);
        assertEquals("N", dto.getCompany());
        assertTrue(dto.getDefname().startsWith("Jones"));
        assertEquals(BigDecimal.valueOf(100), dto.getImposed());
        assertEquals(BigDecimal.valueOf(25), dto.getPaidsf());
        assertEquals(BigDecimal.valueOf(75), dto.getBalance());
        assertEquals("Y", dto.getCo());

        // Parent guardian detection
        assertEquals("Y", dto.getPg());

        // PRIS enforcement should populate EDR date
        assertNotNull(dto.getEdrDate());

        // enforcement fields
        assertEquals("W1", dto.getWarrNo());

        verify(enrichment).getLatestEnforcementForAccount(42L);
        verify(enrichment).getDebtorForParty(1L);
    }

    @Test
    void defname_shouldBeTruncatedTo34Characters() {

        PartyEntity party = new PartyEntity();
        party.setPartyId(2L);
        party.setOrganisation(false);
        party.setSurname("VeryLongSurnameThatExceedsThirtyFourCharacters");
        party.setForenames("Forenames");

        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setParty(party);
        link.setAssociationType("Defendant");

        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(100L);
        account.setAccountNumber("ACC1");
        account.setParties(List.of(link));
        account.setCollectionOrder(false);

        when(enrichment.getDebtorForParty(2L)).thenReturn(Optional.empty());
        when(enrichment.getLatestEnforcementForAccount(100L)).thenReturn(Optional.empty());

        EnforcementReportRowDto dto = mapper.map(account, enrichment);

        assertNotNull(dto);
        assertTrue(dto.getDefname().length() <= 34);
    }
}