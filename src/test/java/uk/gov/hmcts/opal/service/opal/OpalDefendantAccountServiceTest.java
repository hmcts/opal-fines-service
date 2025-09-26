package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.CommentAndNotesDto;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.EnforcementOverride;
import uk.gov.hmcts.opal.dto.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.EnforcerReference;
import uk.gov.hmcts.opal.dto.LjaReference;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.DefendantAccountHeaderViewEntity;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.common.PaymentStateSummary;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.AccountStatusReference;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountHeaderViewRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountPaymentTermsRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.jpa.DefendantAccountSpecs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

class OpalDefendantAccountServiceTest {

    // If you need to create the service, mock the repos as needed.
    private final OpalDefendantAccountService service =
        new OpalDefendantAccountService(null, null, null, null, null, null, null, null);

    @Test
    void testNzHelper() {
        assertEquals(BigDecimal.valueOf(10), OpalDefendantAccountService.nz(BigDecimal.valueOf(10)));
        assertEquals(BigDecimal.ZERO, OpalDefendantAccountService.nz(null));
    }

    @Test
    void testCalculateAge() {
        int age = service.calculateAge(LocalDate.now().minusYears(22));
        assertTrue(age == 22 || age == 21); // depending on birthday
        assertEquals(0, service.calculateAge(null));
    }

    @Test
    void testResolveStatusDisplayName() {
        assertEquals("Live", service.resolveStatusDisplayName("L"));
        assertEquals("Completed", service.resolveStatusDisplayName("C"));
        assertEquals("TFO to be acknowledged", service.resolveStatusDisplayName("TO"));
        assertEquals("TFO to NI/Scotland to be acknowledged", service.resolveStatusDisplayName("TS"));
        assertEquals("TFO acknowledged", service.resolveStatusDisplayName("TA"));
        assertEquals("Account consolidated", service.resolveStatusDisplayName("CS"));
        assertEquals("Account written off", service.resolveStatusDisplayName("WO"));
        assertEquals("Unknown", service.resolveStatusDisplayName("nonsense"));
    }

    @Test
    void testBuildPaymentStateSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .imposed(BigDecimal.valueOf(5))
            .arrears(BigDecimal.valueOf(2))
            .paid(BigDecimal.valueOf(3))
            .accountBalance(BigDecimal.valueOf(7))
            .build();

        PaymentStateSummary summary = service.buildPaymentStateSummary(e);
        assertEquals(BigDecimal.valueOf(5), summary.getImposedAmount());
        assertEquals(BigDecimal.valueOf(2), summary.getArrearsAmount());
        assertEquals(BigDecimal.valueOf(3), summary.getPaidAmount());
        assertEquals(BigDecimal.valueOf(7), summary.getAccountBalance());
    }

    @Test
    void testBuildPartyDetails_allFieldsNullSafe() {
        DefendantAccountHeaderViewEntity e = new DefendantAccountHeaderViewEntity();
        PartyDetails details = service.buildPartyDetails(e);
        assertNotNull(details);
        // If needed, add more asserts based on expected default/null behaviour
    }

    @Test
    void testBuildAccountStatusReference() {
        AccountStatusReference ref = service.buildAccountStatusReference("L");
        assertEquals("L", ref.getAccountStatusCode());
        assertEquals("Live", ref.getAccountStatusDisplayName());
    }

    @Test
    void testBuildBusinessUnitSummary() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .businessUnitId((short) 55)
            .businessUnitName("NorthEast")
            .build();

        BusinessUnitSummary summary = service.buildBusinessUnitSummary(e);
        assertEquals("55", summary.getBusinessUnitId());
        assertEquals("NorthEast", summary.getBusinessUnitName());
        assertEquals("N", summary.getWelshSpeaking());
    }

    @Test
    void testMapToDtoCoversFields() {
        DefendantAccountHeaderViewEntity e = DefendantAccountHeaderViewEntity.builder()
            .partyId(123L)
            .parentGuardianAccountPartyId(456L)
            .accountNumber("ACCT100")
            .accountType("Fine")
            .prosecutorCaseReference("PCR1")
            .fixedPenaltyTicketNumber("FPT1")
            .accountStatus("L")
            .businessUnitId((short) 77)
            .businessUnitName("BUName")
            .imposed(BigDecimal.valueOf(11))
            .arrears(BigDecimal.valueOf(22))
            .paid(BigDecimal.valueOf(33))
            .accountBalance(BigDecimal.valueOf(44))
            .organisation(false)
            .organisationName("MyOrg")
            .title("Sir")
            .firstnames("Robo")
            .surname("Cop")
            .birthDate(LocalDate.now().minusYears(10))
            .build();

        DefendantAccountHeaderSummary dto = service.mapToDto(e);
        assertEquals("ACCT100", dto.getAccountNumber());
        assertNotNull(dto.getPartyDetails());
    }

    @Test
    void updateDefendantAccount_happyPath_updatesAllGroups_andReturnsRepresentation() {
        // Arrange - data first
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId(Short.valueOf(buHeader))
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(bu)
            .build();

        // Arrange - only the mocks we need *now* for stubbing
        final DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        final CourtRepository courtRepo = mock(CourtRepository.class);

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        CourtEntity court = CourtEntity.builder()
            .courtId(100L)
            .name("Central Magistrates")
            .build();
        when(courtRepo.findById(100L)).thenReturn(Optional.of(court));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentAndNotes(CommentAndNotesDto.builder()
                .accountComment("acc comment")
                .freeTextNote1("n1")
                .freeTextNote2("n2")
                .freeTextNote3("n3")
                .build())
            .enforcementCourt(CourtReferenceDto.builder()
                .courtId(100)
                .courtName("Central Magistrates")
                .build())
            .collectionOrder(CollectionOrderDto.builder()
                .collectionOrderFlag(true)
                .collectionOrderDate("2025-01-01")
                .build())
            .enforcementOverrides(EnforcementOverride.builder()
                .enforcementOverrideResult(EnforcementOverrideResultReference.builder()
                    .enforcementOverrideResultId("EO-1")
                    .enforcementOverrideResultTitle("Result Title")
                    .build())
                .enforcer(EnforcerReference.builder()
                    .enforcerId(22)
                    .enforcerName("Enforcer A")
                    .build())
                .lja(LjaReference.builder()
                    .ljaId(33)
                    .ljaName("LJA Name")
                    .build())
                .build())
            .build();

        // If-Match must match current @Version on the entity for verifyIfMatch(...)
        entity.setVersion(1L);

        // Arrange - remaining mocks declared immediately before service construction
        final DefendantAccountHeaderViewRepository headerViewRepo = mock(DefendantAccountHeaderViewRepository.class);
        final DefendantAccountSpecs specs = mock(DefendantAccountSpecs.class);
        final DefendantAccountPaymentTermsRepository paymentTermsRepo = mock(DefendantAccountPaymentTermsRepository
            .class);
        final AmendmentService amendmentService = mock(AmendmentService.class);
        final EntityManager em = mock(EntityManager.class);
        final NoteRepository noteRepository = mock(NoteRepository.class);

        final OpalDefendantAccountService svc = new OpalDefendantAccountService(
            headerViewRepo, accountRepo, specs, paymentTermsRepo, courtRepo, amendmentService, em, noteRepository
        );

        // Act
        DefendantAccountResponse resp = svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST");

        // Assert
        verify(accountRepo).save(entity);
        assertEquals(id, resp.getId());

        assertNotNull(resp.getCommentAndNotes());
        assertEquals("acc comment", resp.getCommentAndNotes().getAccountComment());
        assertEquals("n1", resp.getCommentAndNotes().getFreeTextNote1());
        assertEquals("n2", resp.getCommentAndNotes().getFreeTextNote2());
        assertEquals("n3", resp.getCommentAndNotes().getFreeTextNote3());

        assertNotNull(resp.getEnforcementCourt());
        assertEquals(100, resp.getEnforcementCourt().getCourtId());
        assertEquals("Central Magistrates", resp.getEnforcementCourt().getCourtName());

        assertNotNull(resp.getCollectionOrder());
        assertEquals(Boolean.TRUE, resp.getCollectionOrder().getCollectionOrderFlag());
        assertEquals("2025-01-01", resp.getCollectionOrder().getCollectionOrderDate());

        assertNotNull(resp.getEnforcementOverrides());
        assertEquals("EO-1", resp.getEnforcementOverrides().getEnforcementOverrideResult()
            .getEnforcementOverrideResultId());
        assertEquals(22, resp.getEnforcementOverrides().getEnforcer().getEnforcerId());
        assertEquals(33, resp.getEnforcementOverrides().getLja().getLjaId());

        assertEquals(court, entity.getEnforcingCourt());
        assertTrue(entity.isCollectionOrder());
        assertEquals(LocalDate.parse("2025-01-01"), entity.getCollectionOrderEffectiveDate());
        assertEquals("EO-1", entity.getEnforcementOverrideResultId());
        assertEquals(Long.valueOf(22), entity.getEnforcementOverrideEnforcerId());
        assertEquals(Short.valueOf((short) 33), entity.getEnforcementOverrideTfoLjaId());
    }



    @Test
    void updateDefendantAccount_throwsWhenNoUpdateGroupsProvided() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null);

        Long id = 1L;
        String buHeader = "10";

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder().build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        assertTrue(ex.getMessage().contains("At least one update group"));
        verifyNoInteractions(accountRepo);
    }

    @Test
    void updateDefendantAccount_throwsWhenBusinessUnitMismatch() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitEntity bu = BusinessUnitEntity.builder().businessUnitId((short) 77).build(); // mismatch
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .version(1L) // prevent verifyIfMatch null issues if it gets that far
            .build();

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentAndNotes(CommentAndNotesDto.builder().accountComment("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenCollectionOrderDateInvalid() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null);

        Long id = 1L;
        String buHeader = "10";

        BusinessUnitEntity bu = BusinessUnitEntity.builder().businessUnitId(Short.valueOf(buHeader)).build();
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .version(1L) // must match If-Match to reach CO date parsing
            .build();

        when(accountRepo.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .collectionOrder(CollectionOrderDto.builder()
                .collectionOrderFlag(true)
                .collectionOrderDate("not-a-date")
                .build())
            .build();

        assertThrows(NullPointerException.class, () ->
            svc.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST"));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenEntityNotFound() {
        DefendantAccountRepository accountRepo = mock(DefendantAccountRepository.class);
        OpalDefendantAccountService svc = new OpalDefendantAccountService(
            null, accountRepo, null, null, null, null, null, null);

        when(accountRepo.findById(99L)).thenReturn(Optional.empty());

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentAndNotes(CommentAndNotesDto.builder().accountComment("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            svc.updateDefendantAccount(99L, "10", req, "1", "UNIT_TEST")
        );
        verify(accountRepo, never()).save(any());
    }
}
