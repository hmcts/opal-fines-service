package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.amendment.RecordType;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.repository.CourtRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountServiceUpdateTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CourtRepository courtRepo;

    @Mock
    private LocalJusticeAreaRepository ljaRepo;

    @Mock
    private EnforcerRepository enforcerRepo;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ResultRepository resultRepo;

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void updateDefendantAccount_happyPath_updatesAllGroups_andReturnsRepresentation() {
        // ---------- Arrange ----------
        Long id = 1L;

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(bu)
            .build();

        // If-Match must match this (@Version)
        entity.setVersionNumber(1L);

        // Stubs
        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));
        // Echo the saved entity (so assertions see updated values)
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CourtEntity.Lite court = CourtEntity.Lite.builder()
            .courtId(100L)
            .name("Central Magistrates")
            .build();

        when(courtRepo.findById(100L)).thenReturn(Optional.of(court));

        // Reference entities: stub getters so the service can copy IDs onto the account
        ResultEntity.Lite eor = mock(ResultEntity.Lite.class);
        when(eor.getResultId()).thenReturn("EO-1");
        when(resultRepo.findById("EO-1")).thenReturn(Optional.of(eor));

        EnforcerEntity enforcer = mock(EnforcerEntity.class);
        when(enforcer.getEnforcerId()).thenReturn(22L);
        when(enforcerRepo.findById(22L)).thenReturn(Optional.of(enforcer));

        LocalJusticeAreaEntity lja = mock(LocalJusticeAreaEntity.class);
        when(lja.getLocalJusticeAreaId()).thenReturn((short) 33);
        when(ljaRepo.findById((short) 33)).thenReturn(Optional.of(lja));
        when(noteRepository.save(any())).thenReturn(null);
        doNothing().when(entityManager).lock(any(), any());

        // Request DTO
        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder()
                .accountNotesAccountComments("acc comment")
                .accountNotesFreeTextNote1("n1")
                .accountNotesFreeTextNote2("n2")
                .accountNotesFreeTextNote3("n3")
                .build())
            .enforcementCourt(UpdateDefendantAccountRequest.EnforcementCourtRequest.builder()
                .enforcingCourtId(100)
                .courtName("Central Magistrates")
                .build())
            .collectionOrder(UpdateDefendantAccountRequest.CollectionOrderRequest.builder()
                .collectionOrder(true)
                .collectionOrderDate("2025-01-01")
                .build())
            .enforcementOverride(UpdateDefendantAccountRequest.EnforcementOverrideRequest.builder()
                .enforcementOverrideResultId("EO-1")
                .enforcementOverrideEnforcerId(22)
                .enforcementOverrideTfoLjaId(33)
                .build())
            .build();

        // ---------- Act ----------
        final String buHeader = "10"; // near first use for Checkstyle
        // If-Match = "1" to match entity.setVersion(1L)
        var resp = service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST");

        // ---------- Assert ----------
        verify(defendantAccountRepository).save(entity);
        assertEquals(id, resp.getId());

        assertNotNull(resp.getCommentsAndNotes());
        assertEquals("acc comment", resp.getCommentsAndNotes().getAccountNotesAccountComments());
        assertEquals("n1", resp.getCommentsAndNotes().getAccountNotesFreeTextNote1());
        assertEquals("n2", resp.getCommentsAndNotes().getAccountNotesFreeTextNote2());
        assertEquals("n3", resp.getCommentsAndNotes().getAccountNotesFreeTextNote3());

        assertNotNull(resp.getEnforcementCourt());
        assertEquals(100, resp.getEnforcementCourt().getEnforcingCourtId());
        assertEquals("Central Magistrates", resp.getEnforcementCourt().getCourtName());

        assertNotNull(resp.getCollectionOrder());
        assertEquals(Boolean.TRUE, resp.getCollectionOrder().getCollectionOrder());
        assertEquals("2025-01-01", resp.getCollectionOrder().getCollectionOrderDate());

        assertNotNull(resp.getEnforcementOverride());
        assertEquals("EO-1", resp.getEnforcementOverride().getEnforcementOverrideResultId());
        assertEquals(22, resp.getEnforcementOverride().getEnforcementOverrideEnforcerId());
        assertEquals(33, resp.getEnforcementOverride().getEnforcementOverrideTfoLjaId());

        // Verify entity was updated as expected
        assertEquals(court, entity.getEnforcingCourt());
        assertTrue(entity.getCollectionOrder());
        assertEquals(LocalDate.parse("2025-01-01"), entity.getCollectionOrderEffectiveDate());
        assertEquals("EO-1", entity.getEnforcementOverrideResultId());
        assertEquals(Long.valueOf(22), entity.getEnforcementOverrideEnforcerId());
        assertEquals(Short.valueOf((short) 33), entity.getEnforcementOverrideTfoLjaId());
    }

    @Test
    void updateDefendantAccount_throwsWhenNoUpdateGroupsProvided() {
        Long id = 1L;
        String buHeader = "10";

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder().build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        assertTrue(ex.getMessage().contains("At least one update group"));
        verifyNoInteractions(defendantAccountRepository);
    }

    @Test
    void updateDefendantAccount_throwsWhenBusinessUnitMismatch() {
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 77)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST")
        );
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenCollectionOrderDateInvalid() {
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitFullEntity bu = BusinessUnitFullEntity.builder()
            .businessUnitId(Short.valueOf(buHeader))
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .collectionOrder(UpdateDefendantAccountRequest.CollectionOrderRequest.builder()
                .collectionOrder(true)
                .collectionOrderDate("not-a-date")
                .build())
            .build();

        assertThrows(IllegalArgumentException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "1", "UNIT_TEST"));
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_throwsWhenEntityNotFound() {
        when(defendantAccountRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.updateDefendantAccount(99L, "10", req, "1", "UNIT_TEST")
        );
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_missingIfMatch_throwsPrecondition() {
        Long id = 77L;
        String bu = "10";

        BusinessUnitFullEntity buEnt = BusinessUnitFullEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(buEnt)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build())
            .build();

        // Expect whatever your VersionUtils throws on missing/invalid If-Match
        assertThrows(
            uk.gov.hmcts.opal.exception.ResourceConflictException.class,
            () -> service.updateDefendantAccount(id, bu, req, /*ifMatch*/ null, "UNIT_TEST")
        );
    }

    @Test
    void updateDefendantAccount_versionMismatch_throwsResourceConflict() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(5L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));

        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("x").build()).build();

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> service.updateDefendantAccount(77L, "78", req, "\"0\"", "tester"));
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_callsAuditProcs() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L).businessUnit(bu).versionNumber(0L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));
        when(noteRepository.save(any())).thenReturn(null);
        doNothing().when(entityManager).lock(any(), any());


        var req = UpdateDefendantAccountRequest.builder()
            .commentsAndNotes(CommentsAndNotes.builder().accountNotesAccountComments("hello").build()).build();

        service.updateDefendantAccount(77L, "78", req, "0", "11111111A");

        verify(amendmentService).auditInitialiseStoredProc(77L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentService).auditFinaliseStoredProc(
            eq(77L), eq(RecordType.DEFENDANT_ACCOUNTS), eq((short) 78),
            eq("11111111A"), any(), eq("ACCOUNT_ENQUIRY"));
    }

    @Test
    void updateDefendantAccount_enforcementOverrideLookupsMissing_areNull() {
        var bu = BusinessUnitFullEntity.builder().businessUnitId((short) 78).build();
        var entity = DefendantAccountEntity.builder().defendantAccountId(77L).businessUnit(bu)
            .versionNumber(0L).build();
        when(defendantAccountRepository.findById(77L)).thenReturn(Optional.of(entity));
        doNothing().when(entityManager).lock(any(), any());


        var req = UpdateDefendantAccountRequest.builder()
            .enforcementOverride(UpdateDefendantAccountRequest.EnforcementOverrideRequest.builder()
                .enforcementOverrideResultId("NOPE")
                .enforcementOverrideEnforcerId(999999)
                .enforcementOverrideTfoLjaId(9999)
                .build())
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "0", "tester");
        assertNotNull(resp.getEnforcementOverride());
    }

}
