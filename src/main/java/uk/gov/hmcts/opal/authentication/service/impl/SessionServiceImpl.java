package uk.gov.hmcts.opal.authentication.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.authentication.component.SessionCache;
import uk.gov.hmcts.opal.authentication.model.Session;
import uk.gov.hmcts.opal.authentication.service.SessionService;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionCache sessionCache;

    @Override
    public Session getSession(String sessionId) {
        return sessionCache.get(sessionId);
    }

    @Override
    public void putSession(String sessionId, Session session) {
        sessionCache.put(sessionId, session);
    }

    @Override
    public Session dropSession(String sessionId) {
        return sessionCache.remove(sessionId);
    }

}
