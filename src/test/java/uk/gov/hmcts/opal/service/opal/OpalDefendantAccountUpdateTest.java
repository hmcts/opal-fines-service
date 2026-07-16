package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.dto.RecordType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementCourtDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideResultDefendantAccount;
import uk.gov.hmcts.opal.generated.model.EnforcerDefendantAccount;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaDefendantAccount;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;
import uk.gov.hmcts.opal.mapper.common.EnforcerDefendantAccountMapper;
import uk.gov.hmcts.opal.repository.CourtLiteRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.repository.EnforcerRepository;
import uk.gov.hmcts.opal.repository.LocalJusticeAreaRepository;
import uk.gov.hmcts.opal.repository.NoteRepository;
import uk.gov.hmcts.opal.repository.ResultRepository;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountUpdateTest {

    @Mock
    private DefendantAccountRepository defendantAccountRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CourtLiteRepository courtLiteRepo;

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

    @Mock
    private EnforcerDefendantAccountMapper enforcerDefendantAccountMapper;

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-05-07T10:15:00Z"), ZoneOffset.UTC);

    // Service under test
    @InjectMocks
    private OpalDefendantAccountService service;

    @Test
    void updateDefendantAccount_happyPath_updatesAllGroups_andReturnsRepresentation() {
        // ---------- Arrange ----------
        Long id = 1L;

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 10)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .defendantAccountId(id)
            .businessUnit(bu)
            .build();

        // If-Match must match this (@Version)
        entity.setVersionNumber(1L);

        // Stubs
        when(defendantAccountRepositoryService.findById(id)).thenReturn(entity);
        // Echo the saved entity (so assertions see updated values)
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CourtEntity court = CourtEntity.builder()
            .courtId(100L)
            .name("Central Magistrates")
            .build();

        when(courtLiteRepo.findById(100L)).thenReturn(Optional.of(court));

        // Reference entities: stub getters so the service can copy IDs onto the account
        ResultEntity eor = mock(ResultEntity.class);
        when(eor.getResultId()).thenReturn("EO-1");
        when(resultRepo.findById("EO-1")).thenReturn(Optional.of(eor));

        EnforcerEntity enforcer = mock(EnforcerEntity.class);
        when(enforcer.getEnforcerId()).thenReturn(22L);
        when(enforcerRepo.findById(22L)).thenReturn(Optional.of(enforcer));
        when(enforcerDefendantAccountMapper.toDto(enforcer)).thenReturn(EnforcerDefendantAccount.builder()
            .enforcerId(22L)
            .build());

        LocalJusticeAreaEntity lja = mock(LocalJusticeAreaEntity.class);
        when(lja.getLocalJusticeAreaId()).thenReturn((short) 33);
        when(ljaRepo.findById((short) 33)).thenReturn(Optional.of(lja));
        when(noteRepository.save(any())).thenReturn(null);
        doNothing().when(entityManager).lock(any(), any());

        // Request DTO
        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(
                    CommentsAndNotesCommon.builder()
                        .accountComment("acc comment")
                        .freeTextNote1("n1")
                        .freeTextNote2("n2")
                        .freeTextNote3("n3")
                        .build())
                .enforcementCourt(EnforcementCourtDefendantAccount.builder()
                    .courtId(100L)
                    .build())
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(true)
                    .collectionOrderDate(LocalDate.parse("2025-01-01"))
                    .build())
                .enforcementOverride(EnforcementOverrideDefendantAccount.builder()
                    .enforcementOverrideResult(EnforcementOverrideResultDefendantAccount.builder()
                        .enforcementOverrideResultId("EO-1")
                        .build())
                    .enforcer(EnforcerDefendantAccount.builder()
                        .enforcerId(22L)
                        .build())
                    .lja(LocalJusticeAreaDefendantAccount.builder()
                        .ljaId((short) 33)
                        .build())
                    .build())
                .build())
            .version(BigInteger.valueOf(1))
            .build();


        // ---------- Act ----------
        final String buHeader = "10"; // near first use for Checkstyle
        var resp = service.updateDefendantAccount(id, buHeader, req, "tester");

        // ---------- Assert ----------
        verify(defendantAccountRepository).save(entity);
        assertEquals(id, resp.getPayload().getId());

        assertNotNull(resp.getPayload().getCommentAndNotes());
        assertEquals("acc comment", resp.getPayload().getCommentAndNotes().getAccountComment());
        assertEquals("n1", resp.getPayload().getCommentAndNotes().getFreeTextNote1());
        assertEquals("n2", resp.getPayload().getCommentAndNotes().getFreeTextNote2());
        assertEquals("n3", resp.getPayload().getCommentAndNotes().getFreeTextNote3());

        assertNotNull(resp.getPayload().getEnforcementCourt());
        assertEquals(100, resp.getPayload().getEnforcementCourt().getCourtId());

        assertNotNull(resp.getPayload().getCollectionOrder());
        assertEquals(Boolean.TRUE, resp.getPayload().getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.parse("2025-01-01"), resp.getPayload().getCollectionOrder().getCollectionOrderDate());

        assertNotNull(resp.getPayload().getEnforcementOverride());
        EnforcementOverrideDefendantAccount enforcementOverride = resp.getPayload().getEnforcementOverride();
        assertNotNull(enforcementOverride.getEnforcementOverrideResult());
        assertEquals("EO-1", enforcementOverride.getEnforcementOverrideResult().getEnforcementOverrideResultId());
        assertNotNull(enforcementOverride.getEnforcer());
        assertEquals(22, enforcementOverride.getEnforcer().getEnforcerId());
        assertNotNull(enforcementOverride.getLja());
        assertEquals((short) 33, enforcementOverride.getLja().getLjaId());

        // Verify entity was updated as expected
        assertEquals(court, entity.getEnforcingCourt());
        assertTrue(entity.getCollectionOrder());
        assertEquals(LocalDate.parse("2025-01-01"), entity.getCollectionOrderEffectiveDate());
        assertEquals("EO-1", entity.getEnforcementOverrideResultId());
        assertEquals(Long.valueOf(22), entity.getEnforcementOverrideEnforcerId());
        assertEquals(Short.valueOf((short) 33), entity.getEnforcementOverrideTfoLjaId());

        ArgumentCaptor<NoteEntity> noteCaptor = ArgumentCaptor.forClass(NoteEntity.class);
        verify(noteRepository).save(noteCaptor.capture());
        assertEquals(LocalDateTime.of(2026, 5, 7, 10, 15), noteCaptor.getValue().getPostedDate());
    }

    @Test
    void updateDefendantAccount_throwsWhenBusinessUnitMismatch() {
        Long id = 1L;
        String buHeader = "10";

        BusinessUnitEntity bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 77)
            .build();

        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .businessUnit(bu)
            .versionNumber(1L)
            .build();

        when(defendantAccountRepositoryService.findById(id)).thenReturn(entity);

        UpdateDefendantAccountRequest req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder().accountComment("x").build())
                .build())
            .version(BigInteger.valueOf(1))
            .build();

        assertThrows(EntityNotFoundException.class, () ->
            service.updateDefendantAccount(id, buHeader, req, "tester")
        );
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_versionMismatch_throwsResourceConflict() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(5L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("x")
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        assertThrows(ObjectOptimisticLockingFailureException.class,
            () -> service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name"));
        verify(defendantAccountRepository, never()).save(any());
    }

    @Test
    void updateDefendantAccount_callsAuditProcs() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(0L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        when(noteRepository.save(any())).thenReturn(null);
        doNothing().when(entityManager).lock(any(), any());

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .commentAndNotes(CommentsAndNotesCommon.builder()
                    .accountComment("hello")
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        service.updateDefendantAccount(77L, "78", req, "11111111A", "Tester Name");

        verify(amendmentService).auditInitialiseStoredProc(77L, RecordType.DEFENDANT_ACCOUNTS);
        verify(amendmentService).auditFinaliseStoredProc(
            eq(77L), eq(RecordType.DEFENDANT_ACCOUNTS), eq((short) 78),
            eq("11111111A"), eq("Tester Name"), any(), eq("ACCOUNT_ENQUIRY"));
    }

    @Test
    void updateDefendantAccount_enforcementOverrideLookupsMissing_areNull() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(0L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        doNothing().when(entityManager).lock(any(), any());

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .enforcementOverride(EnforcementOverrideDefendantAccount.builder()
                    .enforcementOverrideResult(EnforcementOverrideResultDefendantAccount.builder()
                        .enforcementOverrideResultId("NOPE")
                        .build())
                    .enforcer(EnforcerDefendantAccount.builder()
                        .enforcerId(999999L)
                        .build())
                    .lja(LocalJusticeAreaDefendantAccount.builder()
                        .ljaId((short) 9999)
                        .build())
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name");
        assertNotNull(resp.getPayload().getEnforcementOverride());
    }

    void updateDefendantAccount_collectionOrderDefaultsDateWhenFlagTrueAndDateMissing() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(0L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).lock(any(), any());

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(true)
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name");

        assertEquals(Boolean.TRUE, resp.getPayload().getCollectionOrder().getCollectionOrderFlag());
        assertEquals(LocalDate.now(), resp.getPayload().getCollectionOrder().getCollectionOrderDate());
        assertEquals(LocalDate.now(), entity.getCollectionOrderEffectiveDate());
    }

    @Test
    void updateDefendantAccount_enforcementOverrideResultNull_clearsEntireOverride() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(0L)
            .build();
        entity.setEnforcementOverrideResultId("FWEC");
        entity.setEnforcementOverrideEnforcerId(21L);
        entity.setEnforcementOverrideTfoLjaId((short) 240);

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        doNothing().when(entityManager).lock(any(), any());

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .enforcementOverride(EnforcementOverrideDefendantAccount.builder()
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name");

        assertNull(resp.getPayload().getEnforcementOverride());
        assertNull(entity.getEnforcementOverrideResultId());
        assertNull(entity.getEnforcementOverrideEnforcerId());
        assertNull(entity.getEnforcementOverrideTfoLjaId());
    }

    @Test
    void updateDefendantAccount_collectionOrderClearsDateWhenFlagFalse() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .collectionOrder(true)
            .collectionOrderEffectiveDate(LocalDate.of(2025, 1, 1))
            .versionNumber(0L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).lock(any(), any());

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .collectionOrder(CollectionOrderCommon.builder()
                    .collectionOrderFlag(false)
                    .collectionOrderDate(LocalDate.of(2025, 2, 2))
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name");

        assertEquals(Boolean.FALSE, resp.getPayload().getCollectionOrder().getCollectionOrderFlag());
        assertNull(resp.getPayload().getCollectionOrder().getCollectionOrderDate());
        assertNull(entity.getCollectionOrderEffectiveDate());
    }

    @Test
    void updateDefendantAccount_enforcementCourtAcceptsLongCourtId() {
        var bu = BusinessUnitEntity.builder()
            .businessUnitId((short) 78)
            .build();

        var entity = DefendantAccountEntity.builder()
            .defendantAccountId(77L)
            .businessUnit(bu)
            .versionNumber(0L)
            .build();

        when(defendantAccountRepositoryService.findById(77L)).thenReturn(entity);
        when(defendantAccountRepository.save(any(DefendantAccountEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(entityManager).lock(any(), any());

        long courtId = 780000000185L;
        CourtEntity court = CourtEntity.builder()
            .courtId(courtId)
            .name("Test Court")
            .build();
        when(courtLiteRepo.findById(courtId)).thenReturn(Optional.of(court));

        var req = UpdateDefendantAccountRequest.builder()
            .payload(UpdateDefendantAccountRequestPayload.builder()
                .enforcementCourt(EnforcementCourtDefendantAccount.builder()
                    .courtId(courtId)
                    .build())
                .build())
            .version(BigInteger.ZERO)
            .build();

        var resp = service.updateDefendantAccount(77L, "78", req, "tester", "Tester Name");

        assertEquals(courtId, resp.getPayload().getEnforcementCourt().getCourtId());
    }
}
