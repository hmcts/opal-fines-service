package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.opal.common.legacy.model.ErrorResponse;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService;
import uk.gov.hmcts.opal.common.user.authorisation.exception.PermissionNotAllowedException;
import uk.gov.hmcts.opal.common.user.authorisation.model.BusinessUnitUser;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.service.AccountNoteContext;

@ExtendWith(MockitoExtension.class)
class LegacyNotesServiceTest {

    private static final String LEGACY_VERSION = "835509468493002959816526022013198014020000027379";

    @Mock private GatewayService gatewayService;
    @Mock private UserState user;

    @InjectMocks private LegacyNotesService service;

    @Test
    void addNote_success_returnsRecordId_andBuildsRequest() {

        LegacyAddNoteResponse entity = legacyRespWithNote("77", "hello");

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        // Only stub what is actually called on the success path:
        when(resp.isSuccessful()).thenReturn(true);
        // (isError() remains default false)

        ArgumentCaptor<LegacyAddNoteRequest> reqCap = ArgumentCaptor.forClass(LegacyAddNoteRequest.class);

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            eq("addNote"),
            eq(LegacyAddNoteResponse.class),
            reqCap.capture(),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("77", "hello");
        givenBusinessUnitUser((short) 1, "L001JG");

        String id = service.addNote(req, "1", user, targetWithBu((short) 1));
        assertEquals("77", id);

        LegacyAddNoteRequest sent = reqCap.getValue();
        assertEquals((short) 1, sent.getBusinessUnitId());
        assertEquals("L001JG", sent.getBusinessUnitUserId());
        assertEquals(BigInteger.valueOf(1L), sent.getVersion());

        LegacyNote sentNote = sent.getActivityNote();
        assertNotNull(sentNote);
        assertEquals("77", sentNote.getRecordId());
        assertEquals("hello", sentNote.getNoteText());
        assertEquals("AA", sentNote.getNoteType());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS, sentNote.getRecordType());

        verify(gatewayService).postToGateway(
            eq("addNote"),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_success_acceptsQuotedLegacyETag() {

        LegacyAddNoteResponse entity = legacyRespWithNote("770000004141", "hello");

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isSuccessful()).thenReturn(true);

        ArgumentCaptor<LegacyAddNoteRequest> reqCap = ArgumentCaptor.forClass(LegacyAddNoteRequest.class);

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            eq("addNote"),
            eq(LegacyAddNoteResponse.class),
            reqCap.capture(),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("770000004141", "hello");
        givenBusinessUnitUser((short) 77, "L077JG");

        String id = service.addNote(req, '"' + LEGACY_VERSION + '"', user, targetWithBu((short) 77));

        assertEquals("770000004141", id);
        assertEquals(new BigInteger(LEGACY_VERSION), reqCap.getValue().getVersion());
        assertEquals("L077JG", reqCap.getValue().getBusinessUnitUserId());
    }

    @Test
    void addNote_errorWithException_throwsLegacyGatewayException() {

        LegacyAddNoteResponse entity = legacyRespWithNote("77", "boom");

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isError()).thenReturn(true);
        when(resp.isException()).thenReturn(true);
        // (no stubs for isLegacyFailure/isSuccessful — not called on this path)

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("77", "boom");
        givenBusinessUnitUser((short) 5, "L005JG");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addNote(req, "1", user, targetWithBu((short) 5))
        );
        assertEquals("Legacy gateway exception", exception.getMessage());

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_errorLegacyFailure_throwsLegacyGatewayFailure() {

        LegacyAddNoteResponse entity = legacyRespWithNote("77", "world");

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isError()).thenReturn(true);
        when(resp.isException()).thenReturn(false);
        when(resp.isLegacyFailure()).thenReturn(true);
        // (no stub for isSuccessful — not called on this path)

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("77", "world");
        givenBusinessUnitUser((short) 9, "L009JG");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addNote(req, "1", user, targetWithBu((short) 9))
        );
        assertEquals("Legacy gateway returned failure", exception.getMessage());

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_errorGeneric_throwsLegacyGatewayError() {

        LegacyAddNoteResponse entity = legacyRespWithNote("77", "meh");

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isError()).thenReturn(true);
        when(resp.isException()).thenReturn(false);
        when(resp.isLegacyFailure()).thenReturn(false);
        // (no stub for isSuccessful — not called)

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("77", "meh");
        givenBusinessUnitUser((short) 3, "L003JG");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addNote(req, "7", user, targetWithBu((short) 3))
        );
        assertEquals("Legacy gateway error: null", exception.getMessage());

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_successWithErrorResponse_throwsLegacyGatewayFailure() {

        LegacyAddNoteResponse entity = LegacyAddNoteResponse.builder()
            .errorResponse(ErrorResponse.builder()
                .errorCode("-20001")
                .errorMessage("User not found")
                .build())
            .build();

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isSuccessful()).thenReturn(true);

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("770000004141", "test");
        givenBusinessUnitUser((short) 77, "L077JG");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addNote(req, '"' + LEGACY_VERSION + '"', user, targetWithBu((short) 77))
        );

        assertEquals("Legacy gateway returned failure: -20001 User not found", exception.getMessage());
    }

    @Test
    void addNote_errorResponseOnErrorPath_includesLegacyErrorDetails() {

        LegacyAddNoteResponse entity = LegacyAddNoteResponse.builder()
            .errorResponse(ErrorResponse.builder()
                .errorCode("-20001")
                .errorMessage("User not found")
                .build())
            .build();

        @SuppressWarnings("unchecked")
        GatewayService.Response<LegacyAddNoteResponse> resp = mock(GatewayService.Response.class);
        ReflectionTestUtils.setField(resp, "responseEntity", entity);
        when(resp.isError()).thenReturn(true);

        when(gatewayService.<LegacyAddNoteResponse>postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("770000004141", "test");
        givenBusinessUnitUser((short) 77, "L077JG");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addNote(req, '"' + LEGACY_VERSION + '"', user, targetWithBu((short) 77))
        );

        assertEquals("Legacy gateway returned failure: -20001 User not found", exception.getMessage());
    }

    @Test
    void addNote_missingBusinessUnitUserId_throwsForbiddenBeforeGateway() {

        AddNoteRequest req = addReq("770000004141", "test");
        when(user.getBusinessUnitUserForBusinessUnit((short) 77)).thenReturn(Optional.empty());

        assertThrows(
            PermissionNotAllowedException.class,
            () -> service.addNote(req, '"' + LEGACY_VERSION + '"', user, targetWithBu((short) 77))
        );

        verifyNoInteractions(gatewayService);
    }

    @Test
    void addNote_blankBusinessUnitUserId_throwsForbiddenBeforeGateway() {

        AddNoteRequest req = addReq("770000004141", "test");
        givenBusinessUnitUser((short) 77, " ");

        assertThrows(
            PermissionNotAllowedException.class,
            () -> service.addNote(req, '"' + LEGACY_VERSION + '"', user, targetWithBu((short) 77))
        );

        verifyNoInteractions(gatewayService);
    }

    // ---------- helpers ----------

    private static AccountNoteContext targetWithBu(short buId) {
        return new AccountNoteContext(
            DefendantAccountEntity.class,
            77L,
            buId,
            AssociatedRecordType.DEFENDANT_ACCOUNTS
        );
    }

    private void givenBusinessUnitUser(short businessUnitId, String businessUnitUserId) {
        BusinessUnitUser businessUnitUser = BusinessUnitUser.builder()
            .businessUnitUserId(businessUnitUserId)
            .build();
        when(user.getBusinessUnitUserForBusinessUnit(businessUnitId)).thenReturn(Optional.of(businessUnitUser));
    }

    private static AddNoteRequest addReq(String recordId, String text) {
        Note n = new Note();
        n.setRecordId(recordId);
        n.setRecordType(RecordType.DEFENDANT_ACCOUNTS);
        n.setNoteText(text);
        n.setNoteType("AA");
        AddNoteRequest r = new AddNoteRequest();
        r.setActivityNote(n);
        return r;
    }

    private static LegacyAddNoteResponse legacyRespWithNote(String recordId, String text) {
        LegacyAddNoteResponse resp = new LegacyAddNoteResponse();
        LegacyNote ln = LegacyNote.builder()
            .recordId(recordId)
            .recordType(RecordType.DEFENDANT_ACCOUNTS)
            .noteText(text)
            .noteType("AA")
            .build();
        try {
            resp.setNote(ln); // if you have a setter
        } catch (Throwable ignore) {
            ReflectionTestUtils.setField(resp, "note", ln); // fallback
        }
        return resp;
    }
}
