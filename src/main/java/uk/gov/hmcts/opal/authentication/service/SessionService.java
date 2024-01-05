package uk.gov.hmcts.opal.authentication.service;

import uk.gov.hmcts.opal.authentication.model.Session;

public interface SessionService {

    Session getSession(String sessionId);

    void putSession(String sessionId, Session session);

    Session dropSession(String sessionId);
}
