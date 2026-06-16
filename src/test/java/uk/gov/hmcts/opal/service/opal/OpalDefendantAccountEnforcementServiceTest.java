package uk.gov.hmcts.opal.service.opal;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddDefendantAccountEnforcementRequest;
import uk.gov.hmcts.opal.dto.AddEnforcementResponse;
import uk.gov.hmcts.opal.dto.EnforcementStatus;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.ResultId;
import uk.gov.hmcts.opal.dto.ResultResponse;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldRequest;
import uk.gov.hmcts.opal.dto.RemoveDefendantAccountEnforcementHoldResponse;
import uk.gov.hmcts.opal.dto.request.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.result.ResultEntity;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private static final String IF_MATCH = "7";
    private static final String USER_DISPLAY_NAME = "Test User";
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
    private DebtorDetailRepositoryService debtorDetailRepositoryService;

    @Mock
    private ResultRepositoryService resultRepositoryService;

    @Mock
    private EnforcerRepositoryService enforcerRepositoryService;

    @Mock
    private LocalJusticeAreaRepositoryService localJusticeAreaRepositoryService;

    @Mock
    private NotesProxy notesProxy;

    @Mock
    private UserStateService userStateService;

    @Mock
    private DefendantAccountRepositoryService defendantAccountRepositoryService;

    @Mock
    private OpalDefendantAccountService opalDefendantAccountService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Spy
    private Clock clock = Clock.fixed(
        Instant.parse("2026-04-22T00:00:00Z"),
        ZoneOffset.UTC
    );

    @InjectMocks
    private OpalDefendantAccountEnforcementService service;

    @Test
    void buildEnforcementOverride_whenOverrideIdsPresent_buildsMappedOverride() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder()
            .enforcementOverrideResultId("FWEC")
            .enforcementOverrideEnforcerId(55L)
            .enforcementOverrideTfoLjaId((short) 66)
            .build();

        ResultEntity result = ResultEntity.builder()
            .resultId("FWEC")
            .resultTitle("Witness Expenses")
            .build();

        EnforcerEntity enforcer = EnforcerEntity.builder()
            .enforcerId(55L)
            .name("North East Enforcement")
            .build();

        LocalJusticeAreaEntity lja = LocalJusticeAreaEntity.builder()
            .localJusticeAreaId((short) 66)
            .ljaCode("L066")
            .name("Tyne & Wear LJA")
            .build();

        when(resultRepositoryService.getResultById("FWEC")).thenReturn(java.util.Optional.of(result));
        when(enforcerRepositoryService.findById(55L)).thenReturn(java.util.Optional.of(enforcer));
        when(localJusticeAreaRepositoryService.getLjaById((short) 66)).thenReturn(java.util.Optional.of(lja));

        EnforcementOverride override = service.buildEnforcementOverride(entity);

        assertNotNull(override);
        assertNotNull(override.getEnforcementOverrideResult());
        assertEquals("FWEC", override.getEnforcementOverrideResult().getEnforcementOverrideId());
        assertNotNull(override.getEnforcer());
        assertEquals(55L, override.getEnforcer().getEnforcerId());
        assertNotNull(override.getLja());
        assertEquals((short) 66, override.getLja().getLjaId());
    }

    @Test
    void buildEnforcementOverride_whenAllOverrideIdsNull_returnsNull() {
        DefendantAccountEntity entity = DefendantAccountEntity.builder().build();

        EnforcementOverride override = service.buildEnforcementOverride(entity);

        assertNull(override);
        verifyNoInteractions(resultRepositoryService, enforcerRepositoryService, localJusticeAreaRepositoryService);
    }

    @Test
    void addEnforcement_whenResultResponsesAreNull_persistsEmptyResultResponses() throws JsonProcessingException {
        mockAuthorisedUser();
        mockDefendantAccount();
        mockCreatedEnforcement();

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(null)
            .paymentTerms(null)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
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
            eq(USER_DISPLAY_NAME),
            eq(null),
            eq(null),
            eq("{}"),
            eq(null),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    void toResultResponsesMap_whenResponsesNull_returnsEmptyMap() throws Exception {
        Map<String, String> result = invokeToResultResponsesMap(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toResultResponsesMap_skipsNullEntriesAndEntriesWithoutParameterName() throws Exception {
        List<ResultResponse> responses = new ArrayList<>();
        responses.add(null);
        responses.add(ResultResponse.builder().parameterName(null).response("ignored").build());
        responses.add(ResultResponse.builder().parameterName("reason").response("a").build());

        Map<String, String> result = invokeToResultResponsesMap(responses);

        assertEquals(1, result.size());
        assertEquals("a", result.get("reason"));
    }

    @Test
    public void testAddEnforcement_whenGivenAllFields_createsEnforcement() throws JacksonException {
        mockAuthorisedUser();
        mockDefendantAccount();
        mockCreatedEnforcement();

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
            request
        );

        String responsesJson = objectMapper.writeValueAsString(resultResponsesMap(
            "reason", "test reason",
            "jail_days", "14",
            "enforcer_id", "55",
            "earliest_release_date", "2026-05-01T00:00:00"
        ));

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq((Integer) 14),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq("test reason"),
            eq(55L),
            eq(responsesJson),
            eq(LocalDateTime.of(2026,5,1,0,0,0)),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenOnlyGivenReason_createsEnforcement() throws JacksonException {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

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
            request
        );

        String responsesJson = objectMapper.writeValueAsString(resultResponsesMap(
            "reason", "test reason"
        ));

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq("test reason"),
            eq(null),
            eq(responsesJson),
            eq(null),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenOnlyGivenJailDays_createsEnforcement() throws JacksonException {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

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
            request
        );

        String responsesJson = objectMapper.writeValueAsString(resultResponsesMap(
            "jail_days", "14"
        ));

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq((Integer) 14),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq(null),
            eq(null),
            eq(responsesJson),
            eq(null),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenOnlyGivenEnforcer_createsEnforcement() throws JacksonException {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

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
            request
        );

        String responsesJson = objectMapper.writeValueAsString(resultResponsesMap(
            "enforcer_id", "55"
        ));

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq(null),
            eq(55L),
            eq(responsesJson),
            eq(null),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenOnlyGivenReleaseDate_createsEnforcement() throws JacksonException {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

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
            request
        );

        String responsesJson = objectMapper.writeValueAsString(resultResponsesMap(
            "earliest_release_date", "2026-05-01T00:00:00"
        ));

        verify(enforcementRepositoryService).addDefendantAccountEnforcement(
            eq(RESULT_ID_AS_STRING),
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID),
            eq(PROSECUTOR_CASE_REFERENCE),
            eq("ACCOUNT_ENQUIRY"),
            eq(null),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq(null),
            eq(null),
            eq(responsesJson),
            eq(LocalDateTime.of(2026,5,1,0,0,0)),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);
    }

    @Test
    public void testAddEnforcement_whenGivenPaymentTerms_callsPaymentTermsService() throws JacksonException {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);

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

        when(defendant.getVersion()).thenReturn(VersionUtils.extractBigInteger(IF_MATCH));

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

        AddDefendantAccountEnforcementRequest request = AddDefendantAccountEnforcementRequest.builder()
            .resultId(ResultId.ABDC)
            .enforcementResultResponses(Collections.emptyList())
            .paymentTerms(paymentTerms)
            .build();

        AddEnforcementResponse response = service.addEnforcement(
            DEFENDANT_ACCOUNT_ID,
            BUSINESS_UNIT_ID,
            BUSINESS_UNIT_USER_ID,
            IF_MATCH,
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
            eq(USER_DISPLAY_NAME),
            eq(null),
            eq(null),
            eq("{}"),
            eq(null),
            eq(VersionUtils.extractBigInteger(IF_MATCH).longValue())
        );

        assertCommonResponse(response);

        verify(opalDefendantAccountService).addPaymentTermsPreservingLastEnforcement(
            eq(DEFENDANT_ACCOUNT_ID),
            eq(BUSINESS_UNIT_ID.toString()),
            eq(BUSINESS_UNIT_USER_ID),
            eq(USER_DISPLAY_NAME),
            eq(IF_MATCH),
            ArgumentMatchers.any(AddDefendantAccountPaymentTermsRequest.class)
        );
    }

    @Test
    void removeEnforcementHold_whenValidRequest_clearsHoldAddsNoteAndReturnsResponse() {
        // arrange
        Long defendantAccountId = 77L;
        Short businessUnitId = 10;
        String businessUnitUserId = "BU-USER-1";
        String ifMatch = "\"7\"";
        String updatedIfMatch = "\"7\"";

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

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
                service.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    request
                );

            // assert
            assertNotNull(result);
            assertEquals(String.valueOf(defendantAccountId), result.getDefendantAccountId());
            assertEquals(BigInteger.valueOf(7L), result.getVersion());
            assertNull(defendantEntity.getLastEnforcement());
            assertEquals(expectedLastMovementDate, defendantEntity.getLastMovementDate());

            verify(userStateService).getUserStateV1FromSecurityContext();
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
            verifyNoInteractions(reportEntryService);
            verify(amendmentService).auditFinaliseStoredProc(
                defendantAccountId,
                uk.gov.hmcts.opal.dto.RecordType.DEFENDANT_ACCOUNTS,
                businessUnitId,
                businessUnitUserId,
                userState.getUserName(),
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

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);

        ResourceConflictException ex = assertThrows(
            ResourceConflictException.class,
            () -> service.removeEnforcementHold(
                defendantAccountId,
                businessUnitId,
                businessUnitUserId,
                "   ",
                request
            )
        );

        assertNotNull(ex);
        assertEquals("Defendant Account", ex.getResourceType());
        assertEquals(String.valueOf(defendantAccountId), ex.getResourceId());
        assertEquals("If-Match header is required", ex.getConflictReason());
        assertSame(defendantEntity, ex.getVersioned());

        verify(userStateService).getUserStateV1FromSecurityContext();
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

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
                () -> service.removeEnforcementHold(
                    defendantAccountId,
                    businessUnitId,
                    businessUnitUserId,
                    ifMatch,
                    request
                )
            );

            assertNotNull(ex);
            assertEquals("Defendant Account", ex.getResourceType());
            assertEquals(String.valueOf(defendantAccountId), ex.getResourceId());
            assertEquals("No enforcement hold to remove", ex.getConflictReason());
            assertSame(defendantEntity, ex.getVersioned());

            verify(userStateService).getUserStateV1FromSecurityContext();
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

        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
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
                    request
                )
            );

            assertNotNull(ex);
            assertEquals(defendantEntity.getClass().getName(), ex.getPersistentClassName());
            assertEquals(defendantAccountId, ex.getIdentifier());

            verify(userStateService).getUserStateV1FromSecurityContext();
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
                eq(userState.getUserName()),
                isNull(),
                eq("Remove Enforcement Hold")
            );
            verifyNoInteractions(reportEntryService, notesProxy);
        }
    }

    @Test
    void getEnforcementStatus_whenNoRecentEnforcement_returnsStatusWithoutLastEnforcementAction() {
        Long defendantAccountId = 77L;

        PartyEntity party = PartyEntity.builder()
            .partyId(88L)
            .organisation(false)
            .birthDate(LocalDate.of(1990, 1, 1))
            .build();

        DefendantAccountPartiesEntity defendantParty = DefendantAccountPartiesEntity.builder()
            .party(party)
            .associationType(AssociationType.DEFENDANT)
            .build();

        DefendantAccountEntity defendantEntity = DefendantAccountEntity.builder()
            .defendantAccountId(defendantAccountId)
            .accountStatus(DefendantAccountStatus.LIVE)
            .jailDays(12)
            .parties(List.of(defendantParty))
            .build();

        when(defendantAccountRepositoryService.findById(defendantAccountId)).thenReturn(defendantEntity);
        when(enforcementRepositoryService.getEnforcementMostRecent(defendantAccountId, null))
            .thenReturn(java.util.Optional.empty());
        when(debtorDetailRepositoryService.findByPartyId(88L)).thenReturn(java.util.Optional.of(
            DebtorDetailEntity.builder().partyId(88L).build()
        ));

        EnforcementStatus status = service.getEnforcementStatus(defendantAccountId);

        assertNotNull(status);
        assertNull(status.getLastEnforcementAction());
        assertNull(status.getEnforcementOverride());
        assertNull(status.getNextEnforcementActionData());
    }

    private void assertCommonResponse(AddEnforcementResponse response) {
        assertEquals(String.valueOf(DEFENDANT_ACCOUNT_ID), response.getDefendantAccountId());
        assertEquals(0, response.getVersion());
        assertEquals(String.valueOf(ENFORCEMENT_ID), response.getEnforcementId());
    }

    private void mockAuthorisedUser() {
        UserState userState = mock(UserState.class);
        when(userState.getDisplayName()).thenReturn(USER_DISPLAY_NAME);
        when(userStateService.getUserStateV1FromSecurityContext()).thenReturn(userState);
    }

    private void mockDefendantAccount() {
        DefendantAccountEntity defendant = mock(DefendantAccountEntity.class);
        when(defendant.getProsecutorCaseReference()).thenReturn(PROSECUTOR_CASE_REFERENCE);
        when(defendantAccountRepositoryService.findById(DEFENDANT_ACCOUNT_ID)).thenReturn(defendant);
    }

    private void mockCreatedEnforcement() {
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
    }

    private Map<String, String> resultResponsesMap(String... keyValues) {
        Map<String, String> resultResponses = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            resultResponses.put(keyValues[i], keyValues[i + 1]);
        }
        return resultResponses;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> invokeToResultResponsesMap(List<ResultResponse> responses) throws Exception {
        Method method = OpalDefendantAccountEnforcementService.class
            .getDeclaredMethod("toResultResponsesMap", List.class);
        method.setAccessible(true);
        return (Map<String, String>) method.invoke(service, responses);
    }

}
