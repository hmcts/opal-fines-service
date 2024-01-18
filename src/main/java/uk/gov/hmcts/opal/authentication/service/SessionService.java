package uk.gov.hmcts.opal.authentication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.model.Session;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionCache sessionCache;


    public Session getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }


    public void putSession(String sessionId, Session session) {
        sessionCache.put(sessionId, session);
    }


    public Session dropSession(String sessionId) {
        return sessionCache.remove(sessionId);
    }

}
