package uk.gov.hmcts.opal.service.opal;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.mapper.request.PaymentTermsMapper;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.opal.controllers.util.UserStateUtil.allPermissionsUser;

@ExtendWith(MockitoExtension.class)
public class OpalDefendantAccountEnforcementServiceTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 1001L;
    private static final Short BUSINESS_UNIT_ID = 2002;
    private static final String BUSINESS_UNIT_USER_ID = "bu-user-123";
    private static final Long IF_MATCH = 7L;
    private static final String AUTH_HEADER = "Bearer token";
    private static final String USER_NAME = "test.user";
    private static final String PROSECUTOR_CASE_REFERENCE = "PCR-12345";
    private static final Long ENFORCEMENT_ID = 9876L;
    private static final String RESULT_ID_AS_STRING = "ABDC";
    @Mock
    private AmendmentService amendmentService;

    @Mock
    private ReportEntryService reportEntryService;

    @Mock
    private EnforcementRepositoryService enforcementRepositoryService;

    @Mock
    private NotesProxy notesProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;

    @Mock
    private EnforcerRepositoryService enforcerRepositoryService;

    @Mock
    private PaymentTermsService paymentTermsService;

    @Mock
    private PaymentTermsMapper paymentTermsMapper;

    @Mock
    private DebtorDetailRepositoryService debtorDetailRepositoryService;

    @Mock
    private ResultRepositoryService resultRepositoryService;

    @InjectMocks
    private OpalDefendantAccountEnforcementService service;


    @BeforeEach
    void setUp() {
        UserState userState = mock(UserState.class);
        when(userState.getUserName()).thenReturn(USER_NAME);
        when(userStateService.checkForAuthorisedUser(AUTH_HEADER)).thenReturn(userState);

        DefendantAccountEntity defendant = mock(DefendantAccountEntity.class);
        when(defendant.getProsecutorCaseReference()).thenReturn(PROSECUTOR_CASE_REFERENCE);
        when(defendantAccountRepositoryService.findById(DEFENDANT_ACCOUNT_ID)).thenReturn(defendant);

        when(enforcementRepositoryService.addDefendantAccountEnforcement(
            anyString(),
            anyLong(),
            anyShort(),
            anyString(),
            anyString(),
            nullable(Integer.class),
            anyString(),
            anyString(),
            nullable(String.class),
            nullable(Long.class),
            anyString(),
            nullable(LocalDateTime.class),
            anyLong()
        )).thenReturn(ENFORCEMENT_ID);

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
    public void testAddEnforcement_whenGivenAllFields_createsEnforcement() throws JsonProcessingException {
        List<ResultResponse> responses = List.of(
            ResultResponse.builder().parameterName("reason").response("test reason").build(),
            ResultResponse.builder().parameterName("jail_days").response("14").build(),
            ResultResponse.builder().parameterName("enforcer_id").response("55").build(),
            ResultResponse.builder().parameterName("earliest_release_date").response("2026-05-01T00:00:00").build()
        );

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(responses)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq((Integer) 14),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq("test reason"),
            eq(55L),
            eq(responses.toString()),
            eq(LocalDateTime.of(2026,5,1,0,0,0)),
            eq(IF_MATCH));

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenReason_createsEnforcement() throws JsonProcessingException {
        List<ResultResponse> responses = List.of(
            ResultResponse.builder().parameterName("reason").response("test reason").build()
        );

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(responses)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq("test reason"),
            eq(null),
            eq(responses.toString()),
            eq(null),
            eq(IF_MATCH)
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenJailDays_createsEnforcement() throws JsonProcessingException {
        List<ResultResponse> responses = List.of(
            ResultResponse.builder().parameterName("jail_days").response("14").build()
        );

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(responses)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq((Integer) 14),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq(null),
            eq(null),
            eq(responses.toString()),
            eq(null),
            eq(IF_MATCH)
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenEnforcer_createsEnforcement() throws JsonProcessingException {
        List<ResultResponse> responses = List.of(
            ResultResponse.builder().parameterName("enforcer_id").response("55").build()
        );

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(responses)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq(null),
            eq(55L),
            eq(responses.toString()),
            eq(null),
            eq(IF_MATCH)
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenReleaseDate_createsEnforcement() throws JsonProcessingException {
        List<ResultResponse> responses = List.of(
            ResultResponse.builder().parameterName("earliest_release_date").response("2026-05-01T00:00:00").build()
        );

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(responses)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq(null),
            eq(null),
            eq(responses.toString()),
            eq(LocalDateTime.of(2026,5,1,0,0,0)),
            eq(IF_MATCH)
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenPaymentTerms_callsPaymentTermsService() throws JsonProcessingException {
        PaymentTerms paymentTerms = PaymentTerms.builder()
            .daysInDefault(7)
            .dateDaysInDefaultImposed(LocalDate.of(2026, 4, 12))
            .extension(true)
            .reasonForExtension("test reason")
            .paymentTermsType(PaymentTermsType.fromCode("B"))
            .effectiveDate(LocalDate.of(2026, 5, 28))
            .instalmentPeriod(uk.gov.hmcts.opal.dto.common.InstalmentPeriod.builder()
                                  .instalmentPeriodCode(InstalmentPeriod.InstalmentPeriodCode.W)
                                  .build())
            .lumpSumAmount(BigDecimal.valueOf(100000))
            .instalmentAmount(BigDecimal.valueOf(0.50))
            .postedDetails(null)
            .build();

        PaymentTermsEntity paymentTermsEntity = PaymentTermsEntity.builder()
            .termsTypeCode(TermsTypeCode.fromCode("B"))
            .effectiveDate(LocalDate.of(2026, 5, 28))
            .instalmentPeriod(uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod.fromCode("W"))
            .instalmentAmount(BigDecimal.valueOf(0.50))
            .instalmentLumpSum(BigDecimal.valueOf(100000))
            .extension(true)
            .reasonForExtension("test reason")
            .build();

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(Collections.emptyList())
            .paymentTerms(paymentTerms)
            .build();

        when(paymentTermsMapper.toEntity(paymentTerms)).thenReturn(paymentTermsEntity);

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
            AUTH_HEADER,
            request
        );

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_NAME),
            eq(null),
            eq(null),
            eq("[]"),
            eq(null),
            eq(IF_MATCH)
        );

        assertCommonResponse(response);
        verify(paymentTermsMapper).toEntity(paymentTerms);
        verify(paymentTermsService).addPaymentTerm(paymentTermsEntity);

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
        ArgumentCaptor<AddNoteRequest> addNoteRequestCaptor = ArgumentCaptor.forClass(AddNoteRequest.class);

        when(userStateService.checkForAuthorisedUser(authHeader)).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);
        when(defendantAccountRepositoryService.saveAndFlush(defendantEntity)).thenReturn(defendantEntity);

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

            verify(userStateService).checkForAuthorisedUser(authHeader);
            verify(defendantAccountRepositoryService).findById(defendantAccountId);
            verify(amendmentService).auditInitialiseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS
            );
            verify(defendantAccountRepositoryService).saveAndFlush(defendantEntity);
            verify(notesProxy).addNote(
                addNoteRequestCaptor.capture(),
                eq(updatedIfMatch),
                eq(userState),
                eq(defendantEntity)
            );
            verify(reportEntryService).createRemoveEnforcementHoldReportEntry(defendantAccountId, businessUnitId);
            verify(amendmentService).auditFinaliseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS,
                businessUnitId,
                businessUnitUserId,
                null,
                "Remove Enforcement Hold"
            );

            assertNotNull(addNoteRequestCaptor.getValue());
            Note capturedNote = addNoteRequestCaptor.getValue().getActivityNote();
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
    void removeEnforcementHold_whenSaveThrowsOptimisticLockingException_bubblesUpException() {
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
            .saveAndFlush(defendantEntity);

        try (MockedStatic<VersionUtils> versionUtils = mockStatic(VersionUtils.class)) {
            versionUtils.when(() -> VersionUtils.verifyIfMatch(
                defendantEntity,
                ifMatch,
                defendantAccountId,
                "removeEnforcementHold"
            )).thenAnswer(invocation -> null);

            ObjectOptimisticLockingFailureException ex = assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> service.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    authHeader,
                    request
                )
            );

            assertNotNull(ex);
            assertEquals(defendantEntity.getClass().getName(), ex.getPersistentClassName());
            assertEquals(defendantAccountId, ex.getIdentifier());

            verify(userStateService).checkForAuthorisedUser(authHeader);
            verify(defendantAccountRepositoryService).findById(defendantAccountId);
            verify(amendmentService).auditInitialiseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS
            );
            verify(defendantAccountRepositoryService).saveAndFlush(defendantEntity);
            verify(amendmentService, org.mockito.Mockito.never()).auditFinaliseStoredProc(
                eq(defendantAccountId),
                eq(uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS),
                eq(businessUnitId),
                eq(businessUnitUserId),
                isNull(),
                eq("Remove Enforcement Hold")
            );
            verifyNoInteractions(reportEntryService, notesProxy);
        }
    }

    private void assertCommonResponse(AddEnforcementResponse response) {
        assertEquals(String.valueOf(DEFENDANT_ACCOUNT_ID), response.getDefendantAccountId());
        assertEquals(Math.toIntExact(IF_MATCH), response.getVersion());
        assertEquals(String.valueOf(ENFORCEMENT_ID), response.getEnforcementId());
    }
}
