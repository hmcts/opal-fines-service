package uk.gov.hmcts.opal.authentication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.authentication.model.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionCacheTest {

    private static final String DUMMY_SESSION_ID = "9D65049E1787A924E269747222F60CAA";

    private SessionCache sessionCache;

    @BeforeEach
    void setUp() {
        sessionCache = new SessionCache();
    }

    @Test
    void putShouldAddEntryToCache() {
        Session session = createSession();

        sessionCache.put(DUMMY_SESSION_ID, session);

        Session retrievedSession = sessionCache.get(DUMMY_SESSION_ID);
        assertEquals(retrievedSession, session);
    }

    @Test
    void putShouldThrowExceptionWhenProvidedWithNullKey() {
        assertThrows(NullPointerException.class, () -> sessionCache.put(null, createSession()));
    }

    @Test
    void putShouldThrowExceptionWhenProvidedWithNullValue() {
        assertThrows(IllegalArgumentException.class, () -> sessionCache.put(DUMMY_SESSION_ID, null));
    }

    @Test
    void getShouldThrowExceptionWhenProvidedWithNullKey() {
        assertThrows(NullPointerException.class, () -> sessionCache.get(null));
    }

    @Test
    void removeShouldRemoveAndReturnExistingSession() {
        Session existingSession = createSession();
        sessionCache.put(DUMMY_SESSION_ID, existingSession);

        Session removedSession = sessionCache.remove(DUMMY_SESSION_ID);

        assertEquals(existingSession, removedSession);

        Session session = sessionCache.get(DUMMY_SESSION_ID);
        assertNull(session);
    }

    @Test
    void removeShouldReturnNullWhenNoSessionExists() {
        Session removedSession = sessionCache.remove(DUMMY_SESSION_ID);

        assertNull(removedSession);
    }

    private Session createSession() {
        return new Session(null, null, 0);
    }

}
