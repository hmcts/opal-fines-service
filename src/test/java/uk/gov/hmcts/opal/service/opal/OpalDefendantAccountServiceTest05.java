package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.common.DefendantAccountParty;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.entity.AliasEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.repository.AliasRepository;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceTest05 {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private AliasRepository aliasRepo;

    @Mock
    private DebtorDetailRepository debtorRepo;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void getDefendantAccountParty_builds_individual_aliases_only() {
        // Build a Party (individual)
        PartyEntity party = PartyEntity.builder()
            .partyId(10L).organisation(false).title("Mr").forenames("John").surname("Doe")
            .build();

        // Link party into DefendantAccountPartiesEntity
        DefendantAccountPartiesEntity dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L)
            .associationType("DEFENDANT")
            .debtor(true)
            .party(party)
            .build();

        // Account with that party
        DefendantAccountEntity account = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(dap))
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        // AliasEntity rows: only surname populated should be considered for individual path;
        // blanks and org-only names ignored; ensure sorting by sequenceNumber.
        AliasEntity a1 = AliasEntity.builder()
            .aliasId(200L).sequenceNumber(2).surname("Smith").forenames("Alice").build();
        AliasEntity a2 = AliasEntity.builder()
            .aliasId(201L).sequenceNumber(1).surname("Jones").forenames("Bob").build();
        AliasEntity blank = AliasEntity.builder()
            .aliasId(202L).sequenceNumber(3).surname("   ").forenames("X").build(); // ignored
        AliasEntity orgOnly = AliasEntity.builder()
            .aliasId(203L).sequenceNumber(4).organisationName("Wayne Ent") // ignored for individual
            .build();

        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(a1, a2, blank, orgOnly));
        when(debtorRepo.findByPartyId(10L)).thenReturn(Optional.empty());

        // --- Act
        GetDefendantAccountPartyResponse resp = service.getDefendantAccountParty(1L, 100L);

        assertNotNull(resp);
        DefendantAccountParty partyDto = resp.getDefendantAccountParty();
        assertNotNull(partyDto.getPartyDetails().getIndividualDetails());
        List<IndividualAlias> indAliases = partyDto.getPartyDetails().getIndividualDetails().getIndividualAliases();
        assertNotNull(indAliases);
        assertEquals(2, indAliases.size(), "should include only valid surname-bearing aliases");

        assertEquals("201", indAliases.get(0).getAliasId());
        assertEquals(Integer.valueOf(1), indAliases.get(0).getSequenceNumber());
        assertEquals("Bob", indAliases.get(0).getForenames());
        assertEquals("Jones", indAliases.get(0).getSurname());

        assertEquals("200", indAliases.get(1).getAliasId());
        assertEquals(Integer.valueOf(2), indAliases.get(1).getSequenceNumber());
        assertEquals("Alice", indAliases.get(1).getForenames());
        assertEquals("Smith", indAliases.get(1).getSurname());

        // Organisation aliases must be null/empty for an individual party
        OrganisationDetails orgAliases = partyDto.getPartyDetails().getOrganisationDetails();
        if (orgAliases != null) {
            assertTrue(orgAliases.getOrganisationAliases() == null || orgAliases.getOrganisationAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_builds_organisation_aliases_only() {
        var party = PartyEntity.builder()
            .partyId(20L).organisation(true).organisationName("Wayne Enterprises")
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(200L)
            .associationType("DEFENDANT")
            .debtor(true)
            .party(party)
            .build();

        var account = DefendantAccountEntity.builder()
            .defendantAccountId(2L).parties(List.of(dap))
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L).build();

        when(defendantAccountRepository.findById(2L)).thenReturn(Optional.of(account));

        // Only org-name-bearing aliases should be mapped; blanks ignored; sorted by seq.
        var o1 = AliasEntity.builder()
            .aliasId(300L).sequenceNumber(2).organisationName("Wayne Group").build();
        var o2 = AliasEntity.builder()
            .aliasId(301L).sequenceNumber(1).organisationName("Wayne Ent Ltd").build();
        var blank = AliasEntity.builder()
            .aliasId(302L).sequenceNumber(3).organisationName("   ").build(); // ignored
        var personOnly = AliasEntity.builder()
            .aliasId(303L).sequenceNumber(4).surname("Jones").forenames("Bob").build(); // ignored for org

        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(o1, o2, blank, personOnly));
        when(debtorRepo.findByPartyId(20L)).thenReturn(Optional.empty());

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(service, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(service, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = service.getDefendantAccountParty(2L, 200L);

        assertNotNull(resp);
        var partyDto = resp.getDefendantAccountParty();
        assertTrue(partyDto.getPartyDetails().getOrganisationFlag());

        var orgDetails = partyDto.getPartyDetails().getOrganisationDetails();
        assertNotNull(orgDetails);
        var orgAliases = orgDetails.getOrganisationAliases();
        assertNotNull(orgAliases);
        assertEquals(2, orgAliases.size());

        // Sorted: o2 (seq=1) then o1 (seq=2)
        assertEquals("301", orgAliases.get(0).getAliasId());
        assertEquals(Integer.valueOf(1), orgAliases.get(0).getSequenceNumber());
        assertEquals("Wayne Ent Ltd", orgAliases.get(0).getOrganisationName());

        assertEquals("300", orgAliases.get(1).getAliasId());
        assertEquals(Integer.valueOf(2), orgAliases.get(1).getSequenceNumber());
        assertEquals("Wayne Group", orgAliases.get(1).getOrganisationName());

        // Individual aliases must be null/empty for an organisation party
        var indDetails = partyDto.getPartyDetails().getIndividualDetails();
        if (indDetails != null) {
            assertTrue(indDetails.getIndividualAliases() == null || indDetails.getIndividualAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_individual_with_no_aliases_sets_individualAliases_null() {
        // Party (individual)
        var party = PartyEntity.builder()
            .partyId(10L).organisation(false).title("Ms").forenames("Anna").surname("Graham")
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(100L).associationType("DEFENDANT").debtor(true).party(party)
            .build();

        var account = DefendantAccountEntity.builder()
            .defendantAccountId(1L)
            .parties(List.of(dap))
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        // No valid aliases: empty list OR rows that don't have surname populated are ignored
        when(aliasRepo.findByParty_PartyId(10L)).thenReturn(List.of(
            AliasEntity.builder().aliasId(1L)
                .sequenceNumber(1).surname("   ").forenames("X").build(),
            AliasEntity.builder().aliasId(2L)
                .sequenceNumber(2).organisationName("Some Org").build()
        ));
        when(debtorRepo.findByPartyId(10L)).thenReturn(Optional.empty());

        try {
            var f1 = OpalDefendantAccountService.class.getDeclaredField("aliasRepository");
            f1.setAccessible(true);
            f1.set(service, aliasRepo);
            var f2 = OpalDefendantAccountService.class.getDeclaredField("debtorDetailRepository");
            f2.setAccessible(true);
            f2.set(service, debtorRepo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var resp = service.getDefendantAccountParty(1L, 100L);

        assertNotNull(resp);
        var ind = resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails();
        assertNotNull(ind, "individual details should be present for an individual party");
        assertNull(ind.getIndividualAliases(), "no valid individual aliases → should be null");

        // also ensure org details/aliases are absent or empty
        var org = resp.getDefendantAccountParty().getPartyDetails().getOrganisationDetails();
        if (org != null) {
            assertTrue(org.getOrganisationAliases() == null || org.getOrganisationAliases().isEmpty());
        }
    }

    @Test
    void getDefendantAccountParty_organisation_with_no_aliases_sets_organisationAliases_null() {
        // Party (organisation)
        var party = PartyEntity.builder()
            .partyId(20L).organisation(true).organisationName("TechCorp Solutions Ltd")
            .build();

        var dap = DefendantAccountPartiesEntity.builder()
            .defendantAccountPartyId(200L).associationType("DEFENDANT").debtor(true).party(party)
            .build();

        var account = DefendantAccountEntity.builder()
            .defendantAccountId(2L)
            .parties(List.of(dap))
            .businessUnit(BusinessUnitFullEntity.builder()
                .businessUnitId((short) 1).build())
            .versionNumber(0L)
            .build();

        when(defendantAccountRepository.findById(2L)).thenReturn(Optional.of(account));

        // No valid org aliases: empty list OR rows with blank org names are ignored;
        // person-only rows are ignored for org parties
        when(aliasRepo.findByParty_PartyId(20L)).thenReturn(List.of(
            AliasEntity.builder().aliasId(10L)
                .sequenceNumber(1).organisationName("   ").build(),
            AliasEntity.builder().aliasId(11L)
                .sequenceNumber(2).surname("Jones").forenames("Bob").build()
        ));
        when(debtorRepo.findByPartyId(20L)).thenReturn(Optional.empty());

        var resp = service.getDefendantAccountParty(2L, 200L);

        assertNotNull(resp);
        var org = resp.getDefendantAccountParty().getPartyDetails().getOrganisationDetails();
        assertNotNull(org, "organisation details should be present for an organisation party");
        assertNull(org.getOrganisationAliases(), "no valid organisation aliases → should be null");

        // also ensure individual alias list is absent/empty
        var ind = resp.getDefendantAccountParty().getPartyDetails().getIndividualDetails();
        if (ind != null) {
            assertTrue(ind.getIndividualAliases() == null || ind.getIndividualAliases().isEmpty());
        }
    }

}
