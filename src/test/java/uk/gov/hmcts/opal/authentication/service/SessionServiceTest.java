package uk.gov.hmcts.opal.authentication.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.opal.authentication.component.SessionCache;
import uk.gov.hmcts.opal.authentication.model.Session;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionCache sessionCache;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void testGetSession() {
        String sessionId = "123";
        Session expectedSession = new Session(sessionId, "Token123", 3600);

        when(sessionCache.get(sessionId)).thenReturn(expectedSession);

        Session result = sessionService.getSession(sessionId);

        assertSame(expectedSession, result);
        verify(sessionCache).get(sessionId);
    }

    @Test
    void testPutSession() {
        String sessionId = "456";
        Session session = new Session(sessionId, "Token123", 3600);

        sessionService.putSession(sessionId, session);

        verify(sessionCache).put(sessionId, session);
    }

    @Test
    void testDropSession() {
        String sessionId = "789";
        Session session = new Session(sessionId, "Token123", 3600);

        when(sessionCache.remove(sessionId)).thenReturn(session);

        Session result = sessionService.dropSession(sessionId);

        assertSame(session, result);
        verify(sessionCache).remove(sessionId);
    }
}
