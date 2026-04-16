package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

public class ReportEnrichmentServiceImplTest {

    private DebtorDetailRepository debtorRepo;
    private EnforcementRepository enforcementRepo;
    private ReportEnrichmentServiceImpl service;

    @BeforeEach
    public void setup() {
        debtorRepo = mock(DebtorDetailRepository.class);
        enforcementRepo = mock(EnforcementRepository.class);
        service = new ReportEnrichmentServiceImpl(debtorRepo, enforcementRepo);
    }

    @Test
    public void prefetchForAccounts_loadsDebtorsAndLatestEnforcements() {
        // party and link setup
        PartyEntity party = new PartyEntity();
        party.setPartyId(11L);

        DefendantAccountPartiesEntity link = new DefendantAccountPartiesEntity();
        link.setParty(party);

        DefendantAccountEntity account = new DefendantAccountEntity();
        account.setDefendantAccountId(101L);
        account.setParties(List.of(link));

        // repo return objects
        DebtorDetailEntity debtor = new DebtorDetailEntity();
        debtor.setPartyId(11L);
        debtor.setVehicleRegistration("REG1");

        EnforcementEntity.Lite enf = new EnforcementEntity.Lite();
        enf.setDefendantAccountId(101L);
        enf.setPostedDate(LocalDateTime.now().minusDays(1));
        enf.setWarrantReference("W1");

        // NOTE: repositories expect Sets of IDs in this codebase — use Set.of(...)
        when(debtorRepo.findByPartyIdIn(Set.of(11L))).thenReturn(List.of(debtor));
        when(enforcementRepo.findByDefendantAccountIdIn(Set.of(101L))).thenReturn(List.of(enf));

        // exercise
        service.prefetchForAccounts(List.of(account));

        // assertions
        Optional<DebtorDetailEntity> foundDebtor = service.getDebtorForParty(11L);
        assertThat(foundDebtor).isPresent();
        assertThat(foundDebtor.get().getVehicleRegistration()).isEqualTo("REG1");

        Optional<EnforcementEntity.Lite> foundEnf = service.getLatestEnforcementForAccount(101L);
        assertThat(foundEnf).isPresent();
        assertThat(foundEnf.get().getWarrantReference()).isEqualTo("W1");

        // verify the repos were called with Sets
        verify(debtorRepo).findByPartyIdIn(Set.of(11L));
        verify(enforcementRepo).findByDefendantAccountIdIn(Set.of(101L));
    }

    @Test
    public void getDebtorForParty_returnsEmpty_whenNotPrefetched() {
        service.prefetchForAccounts(List.of());
        assertThat(service.getDebtorForParty(999L)).isEmpty();
    }
}