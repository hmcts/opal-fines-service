package uk.gov.hmcts.opal.authentication.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.authentication.model.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionCache {

    private final Map<String, Session> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void postConstruct() {
        log.warn(
            "### This implementation is intended only for dev and test purposes, and is not intended for production ###"
        );
    }

    public void put(String sessionId, Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Null session is not permitted");
        }

        cache.put(sessionId, session);
        log.debug("Added session to cache: {}:{}", sessionId, session);
    }

    public Session get(String sessionId) {
        return cache.get(sessionId);
    }

    public Session remove(String sessionId) {
        return cache.remove(sessionId);
    }

}
