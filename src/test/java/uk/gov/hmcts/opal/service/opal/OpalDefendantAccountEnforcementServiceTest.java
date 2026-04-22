package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.exception.ResourceConflictException;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.persistence.DebtorDetailRepositoryService;
import uk.gov.hmcts.opal.service.persistence.DefendantAccountRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcementRepositoryService;
import uk.gov.hmcts.opal.service.persistence.EnforcerRepositoryService;
import uk.gov.hmcts.opal.service.persistence.LocalJusticeAreaRepositoryService;
import uk.gov.hmcts.opal.service.persistence.ResultRepositoryService;
import uk.gov.hmcts.opal.service.proxy.NotesProxy;
import uk.gov.hmcts.opal.util.VersionUtils;

@ExtendWith(MockitoExtension.class)
class OpalDefendantAccountEnforcementServiceTest {

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private AmendmentService amendmentService;

    @Mock
    private ReportEntryService reportEntryService;

    @Mock
    private NotesProxy notesProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;

    @Mock
    private EnforcerRepositoryService enforcerRepositoryService;

    @Mock
    private EnforcementRepositoryService enforcementRepositoryService;

    @Mock
    private DebtorDetailRepositoryService debtorDetailRepositoryService;

    @Mock
    private ResultRepositoryService resultRepositoryService;

    private OpalDefendantAccountEnforcementService opalDefendantAccountEnforcementService;

    @BeforeEach
    void setUp() {
        opalDefendantAccountEnforcementService = new OpalDefendantAccountEnforcementService(
            defendantAccountRepositoryService,
            localJusticeAreaRepositoryService,
            enforcerRepositoryService,
            enforcementRepositoryService,
            debtorDetailRepositoryService,
            resultRepositoryService,
            notesProxy,
            userStateService,
            amendmentService,
            reportEntryService,
            Clock.fixed(java.time.Instant.parse("2026-04-22T00:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void removeEnforcementHold_whenValidRequest_clearsHoldAddsNoteCreatesReportAndReturnsResponse() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER-1";
        String ifMatch = "\"7\"";
        String updatedIfMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        DefendantAccountEntity defendantEntity = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .lastEnforcement("NOENF")
            .versionNumber(7L)
            .build();

        UserState userState = allPermissionsUser();
        LocalDate expectedLastMovementDate = LocalDate.of(2026, 4, 22);

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);
        when(defendantAccountRepositoryService.saveAndFlush(defendantEntity)).thenReturn(defendantEntity);
        when(notesProxy.addNote(any(AddNoteRequest.class), eq(updatedIfMatch), eq(userState), eq(defendantEntity)))
            .thenReturn("NOTE-1");

        try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
            versionUtils.when(() -> VersionUtils.verifyIfMatch(
                defendantEntity,
                ifMatch,
                defendantAccountId,
                "removeEnforcementHold"
            )).thenAnswer(invocation -> null);
            versionUtils.when(() -> VersionUtils.createETag(defendantEntity)).thenReturn(updatedIfMatch);

            // act
            RemoveDefendantAccountEnforcementHoldResponse result =
                opalDefendantAccountEnforcementService.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    authHeader,
                    request
                );

            // assert
            assertNotNull(result);
            assertEquals(String.valueOf(defendantAccountId), result.getDefendantAccountId());
            assertEquals(BigInteger.valueOf(7L), result.getVersion());
            assertNull(defendantEntity.getLastEnforcement());
            assertEquals(expectedLastMovementDate, defendantEntity.getLastMovementDate());

            ArgumentCaptor<AddNoteRequest> noteCaptor = ArgumentCaptor.forClass(AddNoteRequest.class);

            verify(userStateService).checkForAuthorisedUser(authHeader);
            verify(defendantAccountRepositoryService).findById(defendantAccountId);
            verify(amendmentService).auditInitialiseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.entity.amendment.RecordType.DEFENDANT_ACCOUNTS
            );
            verify(defendantAccountRepositoryService).saveAndFlush(defendantEntity);
            verify(notesProxy).addNote(noteCaptor.capture(), eq(updatedIfMatch), eq(userState), eq(defendantEntity));
            verify(reportEntryService).createRemoveEnforcementHoldReportEntry(defendantAccountId, businessUnitId);
            verify(amendmentService).auditFinaliseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.entity.amendment.RecordType.DEFENDANT_ACCOUNTS,
                businessUnitId,
                businessUnitUserId,
                null,
                "Remove Enforcement Hold"
            );

            Note capturedNote = noteCaptor.getValue().getActivityNote();
            assertNotNull(capturedNote);
            assertEquals(uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS, capturedNote.getRecordType());
            assertEquals(String.valueOf(defendantAccountId), capturedNote.getRecordId());
            assertEquals("remove hold reason", capturedNote.getNoteText());
            assertEquals("AA", capturedNote.getNoteType());
            versionUtils.verify(() -> VersionUtils.createETag(defendantEntity));
        }
    }

    @Test
    void removeEnforcementHold_whenIfMatchBlank_throwsResourceConflictException() {
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER-1";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        DefendantAccountEntity defendantEntity = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .lastEnforcement("NOENF")
            .versionNumber(7L)
            .build();

        UserState userState = allPermissionsUser();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);

        ResourceConflictException ex = assertThrows(
            ResourceConflictException.class,
            () -> opalDefendantAccountEnforcementService.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                "   ",
                authHeader,
                request
            )
        );

        assertNotNull(ex);
        assertEquals("Defendant Account", ex.getResourceType());
        assertEquals(String.valueOf(defendantAccountId), ex.getResourceId());
        assertEquals("If-Match header is required", ex.getConflictReason());
        assertSame(defendantEntity, ex.getVersioned());

        verify(userStateService).checkForAuthorisedUser(authHeader);
        verify(defendantAccountRepositoryService).findById(defendantAccountId);
        verifyNoInteractions(amendmentService, reportEntryService, notesProxy);
        verifyNoMoreInteractions(defendantAccountRepositoryService);
    }

    @Test
    void removeEnforcementHold_whenNoEnforcementHold_throwsResourceConflictException() {
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER-1";
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        DefendantAccountEntity defendantEntity = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .lastEnforcement(null)
            .versionNumber(7L)
            .build();

        UserState userState = allPermissionsUser();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);

        try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
            versionUtils.when(() -> VersionUtils.verifyIfMatch(
                defendantEntity,
                ifMatch,
                defendantAccountId,
                "removeEnforcementHold"
            )).thenAnswer(invocation -> null);

            ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> opalDefendantAccountEnforcementService.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    authHeader,
                    request
                )
            );

            assertNotNull(ex);
            assertEquals("Defendant Account", ex.getResourceType());
            assertEquals(String.valueOf(defendantAccountId), ex.getResourceId());
            assertEquals("No enforcement hold to remove", ex.getConflictReason());
            assertSame(defendantEntity, ex.getVersioned());

            verify(userStateService).checkForAuthorisedUser(authHeader);
            verify(defendantAccountRepositoryService).findById(defendantAccountId);
            verifyNoInteractions(amendmentService, reportEntryService, notesProxy);
            verifyNoMoreInteractions(defendantAccountRepositoryService);
        }
    }

    @Test
    void removeEnforcementHold_whenSaveThrowsOptimisticLockingException_throwsResourceConflictException() {
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER-1";
        String ifMatch = "\"7\"";
        String authHeader = "Bearer abc";

        RemoveDefendantAccountEnforcementHoldRequest request =
            RemoveDefendantAccountEnforcementHoldRequest.builder()
                .reason("remove hold reason")
                .build();

        DefendantAccountEntity defendantEntity = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .lastEnforcement("NOENF")
            .versionNumber(7L)
            .build();

        UserState userState = allPermissionsUser();

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);
        doThrow(new ObjectOptimisticLockingFailureException(defendantEntity.getClass(), defendantAccountId))
            .when(defendantAccountRepositoryService)
            .saveAndFlush(any(DefendantAccountEntity.class));

        try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
            versionUtils.when(() -> VersionUtils.verifyIfMatch(
                defendantEntity,
                ifMatch,
                defendantAccountId,
                "removeEnforcementHold"
            )).thenAnswer(invocation -> null);

            ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> opalDefendantAccountEnforcementService.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    authHeader,
                    request
                )
            );

            assertNotNull(ex);
            assertEquals("Defendant Account", ex.getResourceType());
            assertEquals(String.valueOf(defendantAccountId), ex.getResourceId());
            assertEquals("Account version has changed", ex.getConflictReason());
            assertSame(defendantEntity, ex.getVersioned());

            verify(userStateService).checkForAuthorisedUser(authHeader);
            verify(defendantAccountRepositoryService).findById(defendantAccountId);
            verify(amendmentService).auditInitialiseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.entity.amendment.RecordType.DEFENDANT_ACCOUNTS
            );
            verify(defendantAccountRepositoryService).saveAndFlush(defendantEntity);
            verify(amendmentService, org.mockito.Mockito.never()).auditFinaliseStoredProc(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            );
            verifyNoInteractions(reportEntryService, notesProxy);
        }
    }
}
