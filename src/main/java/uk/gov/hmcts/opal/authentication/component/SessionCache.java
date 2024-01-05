package uk.gov.hmcts.opal.authentication.component;

import uk.gov.hmcts.opal.authentication.model.Session;

public interface SessionCache {

    void put(String sessionId, Session session);

    Session get(String sessionId);

    Session remove(String sessionId);
}
