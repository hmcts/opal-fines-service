package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.hmcts.opal.authorisation.model.UserState;
import uk.gov.hmcts.opal.dto.AddNoteRequest;
import uk.gov.hmcts.opal.dto.Note;
import uk.gov.hmcts.opal.dto.RecordType;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyAddNoteResponse;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyNote;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;

@ExtendWith(MockitoExtension.class)
class LegacyNotesServiceTest {

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
            eq("LIBRA.add_note"),
            eq(LegacyAddNoteResponse.class),
            reqCap.capture(),
            isNull(String.class)
        )).thenReturn(resp);

        AddNoteRequest req = addReq("77", "hello");
        DefendantAccountEntity account = accountWithBu((short) 1);
        when(user.getUserId()).thenReturn(999L);

        String id = service.addNote(req, "1", user, account);
        assertEquals("77", id);

        LegacyAddNoteRequest sent = reqCap.getValue();
        assertEquals("1", sent.getBusinessUnitId());
        assertEquals("999", sent.getBusinessUnitUserId());
        assertEquals(1L, sent.getVersion());

        LegacyNote sentNote = sent.getActivityNote();
        assertNotNull(sentNote);
        assertEquals("77", sentNote.getRecordId());
        assertEquals("hello", sentNote.getNoteText());
        assertEquals("AA", sentNote.getNoteType());
        assertEquals(RecordType.DEFENDANT_ACCOUNTS, sentNote.getRecordType());

        verify(gatewayService).postToGateway(
            eq("LIBRA.add_note"),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_errorWithException_stillReturnsRecordId() {

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
        DefendantAccountEntity account = accountWithBu((short) 5);
        when(user.getUserId()).thenReturn(1L);

        String id = service.addNote(req, "1", user, account);
        assertEquals("77", id);

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_errorLegacyFailure_stillReturnsRecordId() {

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
        DefendantAccountEntity account = accountWithBu((short) 9);
        when(user.getUserId()).thenReturn(42L);

        String id = service.addNote(req, "1", user, account);
        assertEquals("77", id);

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    @Test
    void addNote_errorGeneric_stillReturnsRecordId() {

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
        DefendantAccountEntity account = accountWithBu((short) 3);
        when(user.getUserId()).thenReturn(5L);

        String id = service.addNote(req, "7", user, account);
        assertEquals("77", id);

        verify(gatewayService).postToGateway(
            anyString(),
            eq(LegacyAddNoteResponse.class),
            any(LegacyAddNoteRequest.class),
            isNull(String.class)
        );
        verifyNoMoreInteractions(gatewayService);
    }

    // ---------- helpers ----------

    private static BusinessUnitEntity bu(short id) {
        return BusinessUnitEntity.builder().businessUnitId(id).build();
    }

    private static DefendantAccountEntity accountWithBu(short buId) {
        DefendantAccountEntity acc = new DefendantAccountEntity();
        acc.setBusinessUnit(bu(buId));
        return acc;
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
